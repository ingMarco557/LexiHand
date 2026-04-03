package com.example.lexihand

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.util.TypedValue
import android.util.Log

class DiagnosticsActivity : AppCompatActivity() {

    private lateinit var txtTerminal: TextView
    private lateinit var layoutSensors: LinearLayout
    private lateinit var txtIAResultado: TextView
    private lateinit var txtIAConfianza: TextView
    private lateinit var pbConfianza: ProgressBar
    private val sensorViews = mutableMapOf<String, TextView>()

    private val dataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                "GUANTE_RAW_DATA" -> {
                    val rawData = intent.getStringExtra("data") ?: ""
                    actualizarUI(rawData)
                }
                "GUANTE_AI_DATA" -> {
                    val letra = intent.getStringExtra("letra") ?: "--"
                    val conf = intent.getIntExtra("confianza", 0)
                    actualizarPanelIA(letra, conf)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diagnostics)

        txtTerminal = findViewById(R.id.txtRawTerminal)
        layoutSensors = findViewById(R.id.layoutSensorsRaw)
        txtIAResultado = findViewById(R.id.txtIAResultado)
        txtIAConfianza = findViewById(R.id.txtIAConfianza)
        pbConfianza = findViewById(R.id.pbConfianza)

        txtTerminal.text = "> INICIANDO RECEPCIÓN BLUETOOTH..."
    }

    private fun actualizarUI(rawData: String) {
        if (rawData.isEmpty()) return

        runOnUiThread {
            val actual = txtTerminal.text.toString().take(150) // Limita el texto para no saturar memoria
            txtTerminal.text = "> $rawData\n$actual"

            try {
                val partes = rawData.split(",")
                for (parte in partes) {
                    if (parte.contains(":")) {
                        val id = parte.substringBefore(":")
                        val valor = parte.substringAfter(":")
                        actualizarFila(id, valor)
                    }
                }
            } catch (e: Exception) {
                Log.e("DIAG", "Error parseando UI: ${e.message}")
            }
        }
    }

    private fun actualizarFila(id: String, valor: String) {
        // Al trabajar con MPU6050, los valores son float ej: 0.98. Pintamos verde por defecto.
        if (sensorViews.containsKey(id)) {
            val tv = sensorViews[id]
            tv?.text = "$id: [ $valor ]"
        } else {
            val newTv = TextView(this).apply {
                text = "$id: [ $valor ]"
                setTextColor(Color.parseColor("#00FF00")) // Verde tipo Matrix
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setPadding(0, 8, 0, 8)
                typeface = Typeface.MONOSPACE
            }
            layoutSensors.addView(newTv)
            sensorViews[id] = newTv
        }
    }

    private fun actualizarPanelIA(letra: String, confianza: Int) {
        runOnUiThread {
            txtIAResultado.text = "Gesto: $letra"
            txtIAConfianza.text = "Confianza: $confianza%"
            pbConfianza.progress = confianza

            // Si la confianza es alta (>= 80%), Verde. Si no, Amarillo.
            txtIAResultado.setTextColor(if (confianza >= 80) Color.GREEN else Color.YELLOW)
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter().apply {
            addAction("GUANTE_RAW_DATA")
            addAction("GUANTE_AI_DATA")
        }
        ContextCompat.registerReceiver(this, dataReceiver, filter, ContextCompat.RECEIVER_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(dataReceiver)
    }
}