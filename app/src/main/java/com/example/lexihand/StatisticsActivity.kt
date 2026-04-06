package com.example.lexihand

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.LinearProgressIndicator

class StatisticsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        cargarDatosReales()
    }

    private fun cargarDatosReales() {
        // 1. Actualizar el Score Global
        val accuracyText = findViewById<TextView>(R.id.txtGlobalAccuracy)
        val globalAcc = LexiDataManager.obtenerPrecisionGlobal(this)
        accuracyText.text = "$globalAcc%"

        // 2. Llenar la lista del abecedario dinámicamente
        val container = findViewById<LinearLayout>(R.id.containerLetrasStats)
        val inflater = LayoutInflater.from(this)

        val abecedario = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

        for (letra in abecedario) {
            // Inflamos nuestra plantilla individual
            val letraView = inflater.inflate(R.layout.item_letra_stat, container, false)

            val txtLetra = letraView.findViewById<TextView>(R.id.txtNombreLetra)
            val txtPorcentaje = letraView.findViewById<TextView>(R.id.txtPorcentajeLetra)
            val progressBar = letraView.findViewById<LinearProgressIndicator>(R.id.progressBarLetra)

            // Consultar datos reales
            val precision = LexiDataManager.obtenerStatsLetra(this, letra)

            txtLetra.text = "Letra '$letra'"
            txtPorcentaje.text = "$precision%"
            progressBar.setProgress(precision, true)

            // Cambiar el color si la precisión es baja (opcional, para dar buen feedback visual)
            if (precision < 50) {
                progressBar.setIndicatorColor(android.graphics.Color.parseColor("#FF5722")) // Naranja/Rojo
            } else if (precision < 80) {
                progressBar.setIndicatorColor(android.graphics.Color.parseColor("#FFC107")) // Amarillo
            } else {
                progressBar.setIndicatorColor(android.graphics.Color.parseColor("#4CAF50")) // Verde
            }

            // Agregamos la barra al contenedor
            container.addView(letraView)
        }
    }
}