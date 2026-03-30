package com.example.lexihand

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.LinearProgressIndicator

class StatisticsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        // Aquí podrías cargar los datos reales de tu SharedPreferences o SQLite
        setupDummyData()
    }

    private fun setupDummyData() {
        val accuracyText = findViewById<TextView>(R.id.txtGlobalAccuracy)
        val progressA = findViewById<LinearProgressIndicator>(R.id.progressA)
        val progressB = findViewById<LinearProgressIndicator>(R.id.progressB)

        // Simulando datos de la red neuronal
        accuracyText.text = "92.5%"
        progressA.setProgress(92, true)
        progressB.setProgress(78, true)
    }
}