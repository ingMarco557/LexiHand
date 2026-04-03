package com.example.lexihand

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.channels.FileChannel
import java.util.*

class GuanteManager(private val context: Context, private val listener: GuanteListener) {

    interface GuanteListener {
        fun onStatusChanged(conectado: Boolean, bateria: Int)
        fun onLetraDetectada(letra: String)
        fun onRawDataReceived(data: String)
    }

    private var bluetoothGatt: BluetoothGatt? = null
    private var tflite: Interpreter? = null
    private var isScanning = false

    private val SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
    private val CHARACTERISTIC_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
    private val DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private val etiquetas = listOf("A", "B", "C", "D", "E", "F", "G", "H", "S")
    private var letraTemporal = ""
    private var tiempoInicioEstabilidad = 0L
    private val MARGEN_ESTABILIDAD = 500L

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
                // VERIFICA QUE EL NOMBRE COINCIDA CON TU ESP32
                if (result.device.name == "ESP32_GUANTE" || result.device.name == "ESP32_SyncApp") {
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
                gatt.discoverServices()
                listener.onStatusChanged(true, 100)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                listener.onStatusChanged(false, 0)
                bluetoothGatt?.close()
                bluetoothGatt = null
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
            val rawData = characteristic.getStringValue(0) ?: ""
            processData(rawData)
        }
    }

    private fun processData(linea: String) {
        if (linea.isEmpty() || !linea.contains("|")) return

        try {
            val lineaLimpia = linea.trim().replace("\n", "").replace("\r", "")
            val datosStrings = lineaLimpia.replace("|", ",").split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            if (datosStrings.size >= 15) {
                val inputIA = FloatArray(15)
                for (i in 0 until 15) {
                    inputIA[i] = datosStrings[i].toFloatOrNull() ?: 0f
                }

                // --- CORRECCIÓN AQUÍ: Formato simple S1:V,S2:V ---
                val s1 = inputIA[0].toInt()
                val s2 = inputIA[3].toInt()
                val s3 = inputIA[6].toInt()
                val s4 = inputIA[9].toInt()
                val s5 = inputIA[12].toInt()

                val debugMsg = "S1:$s1,S2:$s2,S3:$s3,S4:$s4,S5:$s5"

                val intentRaw = Intent("GUANTE_RAW_DATA").apply {
                    putExtra("data", debugMsg)
                    setPackage(context.packageName)
                }
                context.sendBroadcast(intentRaw)

                // --- INFERENCIA ---
                val outputIA = Array(1) { FloatArray(etiquetas.size) }
                tflite?.run(arrayOf(inputIA), outputIA)

                val maxIndex = outputIA[0].indices.maxByOrNull { outputIA[0][it] } ?: -1
                val confianzaCalculada = if (maxIndex != -1) (outputIA[0][maxIndex] * 100).toInt() else 0
                val letraDetectada = if (maxIndex != -1) etiquetas[maxIndex] else "--"

                val intentAI = Intent("GUANTE_AI_DATA").apply {
                    putExtra("letra", letraDetectada)
                    putExtra("confianza", confianzaCalculada)
                    setPackage(context.packageName)
                }
                context.sendBroadcast(intentAI)

                // Log para que veas en el Logcat si está procesando
                Log.d("GuanteManager", "Enviado a UI: $debugMsg | IA: $letraDetectada")

            }
        } catch (e: Exception) {
            Log.e("GuanteManager", "Error: ${e.message}")
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