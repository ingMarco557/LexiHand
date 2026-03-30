package com.example.lexihand

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val sliderSens = findViewById<Slider>(R.id.sliderSensitivity)
        val btnSave = findViewById<Button>(R.id.btnSaveSettings)

        // 1. Cargar valores guardados anteriormente
        val sharedPref = getSharedPreferences("LexiSettings", Context.MODE_PRIVATE)
        val savedSens = sharedPref.getFloat("sensitivity", 0.5f)
        sliderSens.value = savedSens

        // 2. Guardar nuevos valores
        btnSave.setOnClickListener {
            val newValue = sliderSens.value

            with(sharedPref.edit()) {
                putFloat("sensitivity", newValue)
                apply()
            }

            Toast.makeText(this, "Configuración aplicada", Toast.LENGTH_SHORT).show()
            finish() // Regresa al menú
        }
    }
}