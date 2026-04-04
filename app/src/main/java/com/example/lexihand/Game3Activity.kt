package com.example.lexihand

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.channels.FileChannel
import java.util.*

class Game3Activity : AppCompatActivity() {

    // Vistas
    private lateinit var tvProgress: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvStatus: TextView
    private lateinit var ivTarget: ImageView
    private lateinit var tvDetected: TextView
    private lateinit var tvConfidence: TextView

    // Lógica del Juego
    private var questionsList: List<Game3Question> = listOf()
    private var currentPosition: Int = 0
    private var correctAnswersCount: Int = 0
    private var timer: CountDownTimer? = null
    private var isDialogShowing = false // Evita que salgan muchos popups a la vez

    // Lógica IA & BLE
    private lateinit var tflite: Interpreter
    private var bluetoothGatt: BluetoothGatt? = null
    private val SENSOR_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        if (results.all { it.value }) startBleScan() else Toast.makeText(this, "Permisos necesarios para el guante", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game3)

        tvProgress = findViewById(R.id.tv_g3_progress)
        tvTimer = findViewById(R.id.tv_g3_timer)
        tvStatus = findViewById(R.id.tv_connection_status)
        ivTarget = findViewById(R.id.iv_g3_target)
        tvDetected = findViewById(R.id.tv_g3_detected)
        tvConfidence = findViewById(R.id.tv_g3_confidence)

        val gameMode = intent.getStringExtra("GAME_MODE")
        questionsList = if (gameMode == "QUICK") Game3Repository.getQuickModeQuestions() else Game3Repository.getFullModeQuestions()

        initTFLite()
        checkPermissionsAndConnect() // Arranca el bluetooth al entrar
    }

    private fun initTFLite() {
        try {
            val fd = assets.openFd("modelo_guante.tflite")
            val stream = FileInputStream(fd.fileDescriptor)
            tflite = Interpreter(stream.channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength))
        } catch (e: Exception) {
            Toast.makeText(this, "Error cargando IA", Toast.LENGTH_SHORT).show()
        }
    }

    // --- FLUJO DEL JUEGO ---
    private fun setQuestion() {
        isDialogShowing = false
        val question = questionsList[currentPosition]
        tvProgress.text = "${currentPosition + 1}/${questionsList.size}"
        ivTarget.setImageResource(question.imageResId)
        tvDetected.text = "?"
        tvConfidence.text = "0%"

        startTimer()
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvTimer.text = "⏱️ ${millisUntilFinished / 1000}s"
            }
            override fun onFinish() {
                tvTimer.text = "⏱️ 0s"
                if (!isDialogShowing) checkGameCondition("---", 0f, timeOut = true)
            }
        }.start()
    }

    // Se llama cada vez que la IA predice algo
    private fun checkGameCondition(predictedLetter: String, confidence: Float, timeOut: Boolean = false) {
        if (isDialogShowing) return

        val target = questionsList[currentPosition].letterTarget

        if (timeOut) {
            showFeedbackDialog(false, "Qué mal, vamos por la siguiente. Confío en ti.")
            return
        }

        // Si la letra es correcta y la confianza es mayor al 75% (0.75f) -> GANÓ
        if (predictedLetter == target && confidence >= 0.75f) {
            correctAnswersCount++
            showFeedbackDialog(true, "¡Felicidades, vamos por la siguiente!")
        }
    }

    private fun showFeedbackDialog(isCorrect: Boolean, message: String) {
        isDialogShowing = true
        timer?.cancel()

        runOnUiThread {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(if (isCorrect) "¡Correcto! ✅" else "¡Tiempo Agotado! ❌")
            builder.setMessage(message)
            builder.setCancelable(false)
            builder.setPositiveButton("Continuar") { dialog, _ ->
                dialog.dismiss()
                currentPosition++
                if (currentPosition < questionsList.size) setQuestion() else finishGame()
            }
            builder.show()
        }
    }

    private fun finishGame() {
        val intent = Intent(this, Game3ResultActivity::class.java)
        intent.putExtra("TOTAL_QUESTIONS", questionsList.size)
        intent.putExtra("CORRECT_ANSWERS", correctAnswersCount)
        startActivity(intent)
        finish()
    }

    // --- BLUETOOTH & TFLITE (Tu código adaptado) ---
    private fun checkPermissionsAndConnect() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION)
        } else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val missing = permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isEmpty()) startBleScan() else requestPermissionLauncher.launch(missing.toTypedArray())
    }

    @SuppressLint("MissingPermission")
    private fun startBleScan() {
        val adapter = (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
        if (adapter == null || !adapter.isEnabled) {
            tvStatus.text = "⚠️ Enciende el Bluetooth"
            return
        }
        val scanner = adapter.bluetoothLeScanner
        tvStatus.text = "🔵 Buscando ESP32_SyncApp..."

        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                if (result.device.name == "ESP32_SyncApp") {
                    scanner.stopScan(this)
                    tvStatus.text = "🟡 Conectando..."
                    bluetoothGatt = result.device.connectGatt(this@Game3Activity, false, gattCallback)
                }
            }
        }
        scanner.startScan(scanCallback)
    }

    @SuppressLint("MissingPermission")
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.requestMtu(512)
                runOnUiThread {
                    tvStatus.text = "🟢 Guante Conectado"
                    tvStatus.setTextColor(Color.parseColor("#4CAF50"))
                    setQuestion() // ¡Inicia el juego solo cuando se conecta!
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                bluetoothGatt = null
                runOnUiThread { tvStatus.text = "🔴 Desconectado" }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) gatt.discoverServices()
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"))
                val charac = service?.getCharacteristic(SENSOR_UUID)
                gatt.setCharacteristicNotification(charac, true)
                val descriptor = charac?.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                descriptor?.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (characteristic.uuid == SENSOR_UUID && !isDialogShowing) {
                val rawData = String(characteristic.value ?: return, Charsets.UTF_8)
                try {
                    val sensors = rawData.replace("|", ",").split(",").mapNotNull { it.trim().toFloatOrNull() }
                    if (sensors.size >= 15) runInference(sensors)
                } catch (e: Exception) { }
            }
        }
    }

    private fun runInference(values: List<Float>) {
        if (!::tflite.isInitialized) return
        val input = Array(1) { FloatArray(15) }
        values.forEachIndexed { i, v -> if (i < 15) input[0][i] = v }
        val output = Array(1) { FloatArray(9) }
        tflite.run(input, output)

        val maxIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1
        val confidence = if (maxIndex != -1) output[0][maxIndex] else 0f
        val etiquetas = listOf("A", "B", "C", "D", "E", "F", "G", "H", "S")
        val predicted = if (maxIndex != -1) etiquetas[maxIndex] else "?"

        runOnUiThread {
            tvDetected.text = predicted
            tvConfidence.text = "${(confidence * 100).toInt()}%"

            // Colores según confianza
            if (confidence > 0.75f) tvConfidence.setTextColor(Color.GREEN)
            else if (confidence > 0.40f) tvConfidence.setTextColor(Color.parseColor("#FF9800")) // Naranja
            else tvConfidence.setTextColor(Color.RED)

            checkGameCondition(predicted, confidence)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
    }
}