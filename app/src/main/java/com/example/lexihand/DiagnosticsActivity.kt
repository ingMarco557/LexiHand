package com.example.lexihand

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DiagnosticsActivity : AppCompatActivity() {

    private lateinit var txtTerminal: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diagnostics)

        txtTerminal = findViewById(R.id.txtRawTerminal)

        // Aquí deberías registrar el receptor de tu Bluetooth
        // Por ahora, simulamos la recepción de la trama que definimos en el ESP32
    }

    /**
     * Procesa la trama cruda del ESP32: "0.12,0.55,0.99|..."
     * Justificación: Separar por '|' para identificar el dedo y por ',' para el eje.
     */
    fun updateDiagnostics(rawData: String) {
        val fingers = rawData.split("|")
        val report = StringBuilder()

        fingers.forEachIndexed { index, data ->
            report.append("Sensor Dedo ${index + 1}: [ $data ]\n")
        }

        runOnUiThread {
            txtTerminal.text = report.toString()
        }
    }
}