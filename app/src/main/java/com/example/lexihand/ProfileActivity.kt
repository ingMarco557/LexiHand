package com.example.lexihand

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        setupToolbar()
        loadUserProfile()
        loadStatistics()

        findViewById<Button>(R.id.btnRevinculate).setOnClickListener {
            reVinculateDevice()
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarProfile)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadUserProfile() {
        // Aquí iría la lógica del UserService / Database
        // Por ahora cargamos los datos del esquema
    }

    private fun loadStatistics() {
        // Aquí cargarías los datos de SharedPreferences o Room
    }

    private fun reVinculateDevice() {
        Toast.makeText(this, "Buscando guante LexiHand...", Toast.LENGTH_SHORT).show()
        // Aquí llamarías al BLEManager
    }
}