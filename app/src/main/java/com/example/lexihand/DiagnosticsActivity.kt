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

        txtTerminal.text = "> INICIANDO RECEPCIÓN..."
    }

    private fun actualizarUI(rawData: String) {
        if (rawData.isEmpty()) return

        runOnUiThread {
            // 1. Mostrar siempre el texto crudo en la terminal (arriba)
            val actual = txtTerminal.text.toString().take(200)
            txtTerminal.text = "> $rawData\n$actual"

            try {
                // 2. Parsear formato "S1:val,S2:val..."
                // Limpiamos por si acaso hay espacios o caracteres raros
                val limpio = rawData.replace(" ", "")
                val partes = limpio.split(",")

                for (parte in partes) {
                    if (parte.contains(":")) {
                        val id = parte.substringBefore(":")
                        val valor = parte.substringAfter(":")

                        // Actualizamos la fila correspondiente (S1, S2, etc.)
                        actualizarFila(id, valor)
                    }
                }
            } catch (e: Exception) {
                Log.e("DIAG", "Error parseando UI: ${e.message}")
            }
        }
    }

    private fun actualizarFila(id: String, valor: String) {
        if (sensorViews.containsKey(id)) {
            val tv = sensorViews[id]
            tv?.text = "$id: [ $valor ]"
            val num = valor.toIntOrNull() ?: 0
            // Colores: AMARILLO si está en extremos (sensor sin contacto), VERDE si está en rango normal
            tv?.setTextColor(if (num <= 50 || num >= 4050) Color.YELLOW else Color.parseColor("#00FF00"))
        } else {
            val newTv = TextView(this).apply {
                text = "$id: [ $valor ]"
                setTextColor(Color.parseColor("#00FF00"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setPadding(0, 10, 0, 10)
                typeface = Typeface.MONOSPACE
            }
            layoutSensors.addView(newTv)
            sensorViews[id] = newTv
        }
    }

    private fun actualizarPanelIA(letra: String, confianza: Int) {
        runOnUiThread {
            txtIAResultado.text = "IA: $letra"
            txtIAConfianza.text = "CONFIANZA: $confianza%"
            pbConfianza.progress = confianza
            // Verde si confianza >= 75%, Rojo si es menor
            txtIAResultado.setTextColor(if (confianza >= 75) Color.GREEN else Color.RED)
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter().apply {
            addAction("GUANTE_RAW_DATA")
            addAction("GUANTE_AI_DATA")
        }
        // RECEIVER_EXPORTED permite recibir datos de GuanteManager
        ContextCompat.registerReceiver(this, dataReceiver, filter, ContextCompat.RECEIVER_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(dataReceiver)
    }
}