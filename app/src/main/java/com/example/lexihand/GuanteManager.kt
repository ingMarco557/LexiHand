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

    // --- VARIABLES PARA ESTABILIDAD Y TIEMPO ---
    private var letraTemporal = ""
    private var tiempoInicioEstabilidad = 0L
    private val MARGEN_ESTABILIDAD = 500L
    private val COOLDOWN_ENTRE_LETRAS = 1500L

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
        if (isScanning) return

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter

        if (adapter == null || !adapter.isEnabled) {
            listener.onStatusChanged(false, 0)
            return
        }

        val scanner = adapter.bluetoothLeScanner
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val deviceName = result.device.name ?: result.scanRecord?.deviceName
                if (deviceName == "ESP32_GUANTE") {
                    isScanning = false
                    scanner.stopScan(this)
                    bluetoothGatt = result.device.connectGatt(context, false, gattCallback)
                }
            }
        }

        isScanning = true
        scanner?.startScan(scanCallback)

        Handler(Looper.getMainLooper()).postDelayed({
            if (isScanning) {
                isScanning = false
                scanner?.stopScan(scanCallback)
                listener.onStatusChanged(false, 0)
            }
        }, 10000)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS || newState == BluetoothProfile.STATE_DISCONNECTED) {
                listener.onStatusChanged(false, 0)
                bluetoothGatt?.close()
                bluetoothGatt = null
                return
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                listener.onStatusChanged(true, 85)
                gatt.discoverServices()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val service = gatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"))
            val characteristic = service?.getCharacteristic(SENSOR_UUID)
            if (characteristic != null) {
                gatt.setCharacteristicNotification(characteristic, true)
                val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor!!)
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val rawData = characteristic.getStringValue(0) ?: ""
            processData(rawData)
        }
    }

    private fun processData(data: String) {
        if (data.isEmpty()) return
        try {
            // 1. Convertimos la trama "100,200,300..." en una lista de números
            val partes = data.split(",")
            val valuesParaIA = FloatArray(15) { 0f }

            for (i in partes.indices) {
                if (i < 15) {
                    valuesParaIA[i] = partes[i].trim().toFloatOrNull() ?: 0f
                }
            }

            // 2. ENVIAR AL DIAGNÓSTICO: Reconstruimos "S1:val,S2:val..." solo para la vista
            val debugData = valuesParaIA.take(5).mapIndexed { i, v ->
                "S${i + 1}:${v.toInt()}"
            }.joinToString(",")

            val intentRaw = Intent("GUANTE_RAW_DATA")
            intentRaw.putExtra("data", debugData)
            context.sendBroadcast(intentRaw)

            listener.onRawDataReceived(debugData)

            // 3. EJECUTAR INFERENCIA (Modelo de 15 entradas)
            val input = Array(1) { valuesParaIA }
            val output = Array(1) { FloatArray(etiquetas.size) }

            tflite?.run(input, output)

            val maxIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1
            val confianza = if (maxIndex != -1) output[0][maxIndex] else 0f
            val letraDetectada = if (maxIndex != -1) etiquetas[maxIndex] else "--"

            // 4. ENVIAR RESULTADO IA AL DIAGNÓSTICO
            val intentAI = Intent("GUANTE_AI_DATA").apply {
                putExtra("letra", letraDetectada)
                putExtra("confianza", (confianza * 100).toInt())
            }
            context.sendBroadcast(intentAI)

            // 5. FILTRO DE ESTABILIDAD (0.5 Segundos)
            if (confianza >= 0.80f) {
                if (letraDetectada == letraTemporal) {
                    if (System.currentTimeMillis() - tiempoInicioEstabilidad >= MARGEN_ESTABILIDAD) {
                        listener.onLetraDetectada(letraDetectada)
                        // Esperar 1.5s para la siguiente letra
                        tiempoInicioEstabilidad = System.currentTimeMillis() + COOLDOWN_ENTRE_LETRAS
                    }
                } else {
                    letraTemporal = letraDetectada
                    tiempoInicioEstabilidad = System.currentTimeMillis()
                }
            } else {
                letraTemporal = ""
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