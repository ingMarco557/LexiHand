package com.example.lexihand

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Referencias
        val spinnerLang = findViewById<Spinner>(R.id.spinnerLanguage)
        val sliderFont = findViewById<Slider>(R.id.sliderFontSize)
        val switchAuto = findViewById<MaterialSwitch>(R.id.switchAutocorrect)
        val sliderConf = findViewById<Slider>(R.id.sliderConfidence)
        val sliderDelay = findViewById<Slider>(R.id.sliderDelay)
        val tvTestDelay = findViewById<TextView>(R.id.tvTestDelay)
        val btnTest = findViewById<Button>(R.id.btnTestDelay)
        val btnSave = findViewById<Button>(R.id.btnSaveSettings)
        val btnRecalibrate = findViewById<Button>(R.id.btnRecalibrate)

        // Configurar Spinner de Idiomas
        val languages = arrayOf("Español (MX)", "English (US)", "Français")
        spinnerLang.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)

        // 1. CARGAR PREFERENCIAS
        val prefs = getSharedPreferences("LexiSettings", Context.MODE_PRIVATE)
        sliderFont.value = prefs.getFloat("font_size", 20f)
        switchAuto.isChecked = prefs.getBoolean("autocorrect", true)
        sliderConf.value = prefs.getFloat("confidence", 80f)
        sliderDelay.value = prefs.getFloat("delay", 1.5f)
        spinnerLang.setSelection(prefs.getInt("language_idx", 0))

        // 2. PRUEBA VISUAL DE DELAY
        btnTest.setOnClickListener {
            val delayMs = (sliderDelay.value * 1000).toLong()
            tvTestDelay.text = "Detectando seña..."
            tvTestDelay.alpha = 0.5f

            // Simula la espera de la IA
            Handler(Looper.getMainLooper()).postDelayed({
                tvTestDelay.text = "¡HOLA! (Simulado)"
                tvTestDelay.alpha = 1.0f
                Toast.makeText(this, "Delay de ${sliderDelay.value}s aplicado", Toast.LENGTH_SHORT).show()
            }, delayMs)
        }

        // 3. RE-CALIBRACIÓN (Simulación de zona de calibración)
        btnRecalibrate.setOnClickListener {
            Toast.makeText(this, "Mantén la mano extendida por 3 segundos...", Toast.LENGTH_LONG).show()
            // Aquí llamarías a tu función de Bluetooth para resetear sensores
        }

        // 4. GUARDAR TODO
        btnSave.setOnClickListener {
            with(prefs.edit()) {
                putFloat("font_size", sliderFont.value)
                putBoolean("autocorrect", switchAuto.isChecked)
                putFloat("confidence", sliderConf.value)
                putFloat("delay", sliderDelay.value)
                putInt("language_idx", spinnerLang.selectedItemPosition)
                apply()
            }
            Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}