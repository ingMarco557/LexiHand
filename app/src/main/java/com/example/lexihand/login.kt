package com.example.lexihand

import android.content.Intent
import android.os.Bundle
import android.widget.Button // Importación necesaria para el botón
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class login : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Activamos el modo de pantalla completa
        enableEdgeToEdge()

        // Inflamos el diseño XML
        setContentView(R.layout.activity_login)

        // Configuramos los márgenes para las barras del sistema (Status bar)
        // Buscamos el contenedor principal por su ID "main"
        val mainLayout = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.main)
        if (mainLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainLayout) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // --- FORMA TRADICIONAL ---
        // 1. Declaramos y buscamos el botón manualmente por su ID
        val btnIngresar = findViewById<Button>(R.id.btnLogin)

        // 2. Programamos el clic
        btnIngresar.setOnClickListener {
            // Creamos el "Intent" para ir de esta pantalla a la MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // Cerramos el Login para que no se quede en el historial
            finish()
        }
    }
}