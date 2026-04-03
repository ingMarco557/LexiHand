package com.example.lexihand

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
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

    private val SENSOR_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")
    private val etiquetas = listOf("A", "B", "C", "D", "E", "F", "G", "H", "S")

    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            val fd = context.assets.openFd("modelo_guante.tflite")
            val buffer = FileInputStream(fd.fileDescriptor).channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
            tflite = Interpreter(buffer)
            Log.d("GuanteManager", "Modelo TFLite cargado con éxito")
        } catch (e: Exception) {
            Log.e("GuanteManager", "Error al cargar modelo: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun connect() {
        if (isScanning) return // Evita múltiples escaneos si el usuario pulsa muchas veces

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter

        if (adapter == null || !adapter.isEnabled) {
            listener.onStatusChanged(false, 0)
            return
        }

        val scanner = adapter.bluetoothLeScanner
        Log.d("GuanteManager", "Iniciando búsqueda de ESP32_GUANTE...")

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                // CORRECCIÓN CLAVE: Buscar el nombre de forma segura para Android 12+
                val deviceName = result.device.name ?: result.scanRecord?.deviceName

                // Muestra en la consola todos los dispositivos que va encontrando
                Log.d("GuanteManager", "Dispositivo detectado: $deviceName")

                if (deviceName == "ESP32_GUANTE") {
                    isScanning = false
                    scanner.stopScan(this)
                    Log.d("GuanteManager", "¡Guante encontrado! Intentando conexión GATT...")
                    bluetoothGatt = result.device.connectGatt(context, false, gattCallback)
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Log.e("GuanteManager", "Fallo el escaneo de Bluetooth. Código: $errorCode")
                isScanning = false
                listener.onStatusChanged(false, 0)
            }
        }

        isScanning = true
        scanner?.startScan(scanCallback)

        // TIMER DE SEGURIDAD: Si en 10 segundos no aparece el guante, detenemos la búsqueda
        Handler(Looper.getMainLooper()).postDelayed({
            if (isScanning) {
                isScanning = false
                scanner?.stopScan(scanCallback)
                Log.d("GuanteManager", "Tiempo de búsqueda agotado. No se encontró ESP32_GUANTE.")
                listener.onStatusChanged(false, 0)
            }
        }, 10000)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS || newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.e("GuanteManager", "Error de conexión o dispositivo desconectado (Status: $status)")
                listener.onStatusChanged(false, 0)
                bluetoothGatt?.close()
                bluetoothGatt = null
                return
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("GuanteManager", "Conectado al dispositivo físico exitosamente")
                listener.onStatusChanged(true, 85)
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val service = gatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"))
            val characteristic = service?.getCharacteristic(SENSOR_UUID)
            if (characteristic != null) {
                @SuppressLint("MissingPermission")
                gatt.setCharacteristicNotification(characteristic, true)

                val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                @SuppressLint("MissingPermission")
                gatt.writeDescriptor(descriptor!!)
            } else {
                Log.e("GuanteManager", "No se encontró la característica del UUID en el ESP32.")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val rawData = characteristic.getStringValue(0) ?: ""
            listener.onRawDataReceived(rawData)
            processData(rawData)
        }
    }

    private fun processData(data: String) {
        if (data.isEmpty()) return
        try {
            val values = data.split(",")
                .map { it.substringAfter(":").trim().toFloat() }
                .toFloatArray()

            if (values.size >= 15) {
                val input = Array(1) { values }
                val output = Array(1) { FloatArray(9) }
                tflite?.run(input, output)

                val maxIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1
                if (maxIndex != -1 && output[0][maxIndex] > 0.80f) {
                    listener.onLetraDetectada(etiquetas[maxIndex])
                }
            }
        } catch (e: Exception) {
            Log.e("GuanteManager", "Error en inferencia: ${e.message}")
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