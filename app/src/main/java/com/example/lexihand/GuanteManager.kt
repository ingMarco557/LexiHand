package com.example.lexihand

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.channels.FileChannel
import java.util.*

class GuanteManager(private val context: Context, private val listener: GuanteListener) {

    interface GuanteListener {
        fun onStatusChanged(conectado: Boolean, bateria: Int)
    }

    private var bluetoothGatt: BluetoothGatt? = null
    private var tflite: Interpreter? = null
    private var isScanning = false

    // UUIDs de tu ESP32
    private val SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
    private val CHARACTERISTIC_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
    private val DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    // Tus 9 clases
    private val etiquetas = listOf("A", "B", "C", "D", "E", "F", "G", "H", "S")

    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            val fd = context.assets.openFd("modelo_guante.tflite")
            val buffer = FileInputStream(fd.fileDescriptor).channel.map(
                FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength
            )
            tflite = Interpreter(buffer)
            Log.d("GuanteManager", "Modelo TFLite cargado con éxito")
        } catch (e: Exception) {
            Log.e("GuanteManager", "Error modelo: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun connect() {
        if (isScanning) return
        val adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        val scanner = adapter.bluetoothLeScanner

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                // Buscamos tu ESP32
                if (result.device.name == "ESP32_SyncApp") {
                    isScanning = false
                    scanner.stopScan(this)
                    bluetoothGatt = result.device.connectGatt(context, false, gattCallback)
                }
            }
        }
        isScanning = true
        scanner?.startScan(callback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.requestMtu(512) // Pedimos tubo ancho
                listener.onStatusChanged(true, 100)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                listener.onStatusChanged(false, 0)
                bluetoothGatt?.close()
                bluetoothGatt = null
            }
        }

        @SuppressLint("MissingPermission")
        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt.discoverServices()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val service = gatt.getService(SERVICE_UUID)
            val characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)
            characteristic?.let {
                gatt.setCharacteristicNotification(it, true)
                val desc = it.getDescriptor(DESCRIPTOR_UUID)
                desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(desc)
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val dataBytes = characteristic.value ?: return
            val rawData = String(dataBytes, Charsets.UTF_8)
            processData(rawData)
        }
    }

    private fun processData(rawData: String) {
        try {
            // 1. Limpieza como en el código viejo exitoso
            val parts = rawData.replace("|", ",").split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val sensors = parts.mapNotNull { it.toFloatOrNull() }

            // 2. Verificamos que llegaron los 15 números
            if (sensors.size >= 15) {

                // --- PREPARAMOS DATOS PARA LA INTERFAZ DIAGNOSTICS ---
                // Agarramos el eje X de cada dedo para mostrar (0, 3, 6, 9, 12)
                val debugMsg = String.format(Locale.US, "S1:%.2f,S2:%.2f,S3:%.2f,S4:%.2f,S5:%.2f",
                    sensors[0], sensors[3], sensors[6], sensors[9], sensors[12])

                val intentRaw = Intent("GUANTE_RAW_DATA").apply {
                    putExtra("data", debugMsg)
                    setPackage(context.packageName)
                }
                context.sendBroadcast(intentRaw)

                // --- INFERENCIA TFLITE ---
                if (tflite != null) {
                    val inputIA = Array(1) { FloatArray(15) }
                    for (i in 0 until 15) inputIA[0][i] = sensors[i]

                    val outputIA = Array(1) { FloatArray(9) } // 9 Letras
                    tflite?.run(inputIA, outputIA)

                    val maxIndex = outputIA[0].indices.maxByOrNull { outputIA[0][it] } ?: -1
                    val confianzaCalculada = if (maxIndex != -1) (outputIA[0][maxIndex] * 100).toInt() else 0
                    val letraDetectada = if (maxIndex != -1) etiquetas[maxIndex] else "--"

                    val intentAI = Intent("GUANTE_AI_DATA").apply {
                        putExtra("letra", letraDetectada)
                        putExtra("confianza", confianzaCalculada)
                        setPackage(context.packageName)
                    }
                    context.sendBroadcast(intentAI)
                }
            }
        } catch (e: Exception) {
            Log.e("GuanteManager", "Error procesando datos: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        listener.onStatusChanged(false, 0)
    }
}