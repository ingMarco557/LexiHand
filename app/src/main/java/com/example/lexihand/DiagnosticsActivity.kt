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
                    actualizarSensoresYTerminal(rawData)
                }
                "GUANTE_AI_DATA" -> {
                    val letra = intent.getStringExtra("letra") ?: "--"
                    val confianza = intent.getIntExtra("confianza", 0)
                    actualizarPanelIA(letra, confianza)
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

        txtTerminal.text = "> SISTEMA DIAGNÓSTICO OK\n> BUSCANDO 5 SENSORES..."
    }

    private fun actualizarSensoresYTerminal(rawData: String) {
        // Mostramos la trama tal cual llega para ver dónde se corta
        txtTerminal.text = "> RAW: $rawData\n${txtTerminal.text.toString().take(250)}"

        try {
            // Dividimos la trama por comas
            val listaSensores = rawData.split(",")

            // Forzamos la revisión de los 5 sensores (S1 a S5)
            for (i in 1..5) {
                val ID = "S$i"

                // Buscamos el valor correspondiente en la trama
                val datoEncontrado = listaSensores.find { it.trim().startsWith(ID) }

                val valorLimpio = if (datoEncontrado != null && datoEncontrado.contains(":")) {
                    datoEncontrado.substringAfter(":").trim()
                } else {
                    "PERDIDO ⚠️" // Si no viene en la trama, avisamos
                }

                actualizarFilaSensor(ID, valorLimpio)
            }
        } catch (e: Exception) {
            txtTerminal.append("\n> ERROR PARSEO: ${e.message}")
        }
    }

    private fun actualizarFilaSensor(id: String, valor: String) {
        if (sensorViews.containsKey(id)) {
            val tv = sensorViews[id]
            tv?.text = "$id: [ $valor ]"

            // Si el sensor está perdido, lo ponemos en rojo para que resalte
            if (valor == "PERDIDO ⚠️") {
                tv?.setTextColor(Color.RED)
            } else {
                tv?.setTextColor(Color.parseColor("#00FF00"))
            }
        } else {
            // Crear la fila por primera vez
            val newSensorTxt = TextView(this).apply {
                text = "$id: [ $valor ]"
                setTextColor(if (valor == "PERDIDO ⚠️") Color.RED else Color.parseColor("#00FF00"))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setPadding(0, 10, 0, 10)
                typeface = Typeface.MONOSPACE
            }
            layoutSensors.addView(newSensorTxt)
            sensorViews[id] = newSensorTxt
        }
    }

    private fun actualizarPanelIA(letra: String, confianza: Int) {
        txtIAResultado.text = "LETRA: $letra"
        txtIAConfianza.text = "CONFIANZA: $confianza%"
        pbConfianza.progress = confianza

        when {
            confianza >= 80 -> txtIAResultado.setTextColor(Color.GREEN)
            confianza >= 50 -> txtIAResultado.setTextColor(Color.YELLOW)
            else -> txtIAResultado.setTextColor(Color.RED)
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