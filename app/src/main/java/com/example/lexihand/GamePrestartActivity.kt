package com.example.lexihand

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class GamePrestartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_prestart)

        val btnStart: Button = findViewById(R.id.btn_prestart_start)
        val btnExit: Button = findViewById(R.id.btn_prestart_exit)
        val tvInstructions: TextView = findViewById(R.id.tv_prestart_instructions)

        // Recuperamos el modo que nos pasó la pantalla anterior
        val gameMode = intent.getStringExtra("GAME_MODE")

        // Cambiamos las instrucciones dependiendo del modo
        if (gameMode == "QUICK") {
            tvInstructions.text = "Modo Rápido: Solo 10 preguntas. Tienes 10 segundos por imagen. ¡Mucha suerte!"
        } else {
            tvInstructions.text = "Modo Completo: Todas las preguntas disponibles. Tienes 10 segundos por imagen. ¡Tómate tu tiempo y diviértete!"
        }

        // Si toca COMENEZAR, lo mandamos al juego real
        btnStart.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("GAME_MODE", gameMode) // Le volvemos a pasar el modo
            startActivity(intent)
            finish() // Cerramos esta pantalla para que no se pueda regresar
        }

        // Si toca Salir, regresamos al menú
        btnExit.setOnClickListener {
            finish()
        }
    }
}