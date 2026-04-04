package com.example.lexihand

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val tvScoreText: TextView = findViewById(R.id.tv_score_text)
        val tvPercentage: TextView = findViewById(R.id.tv_percentage)
        val btnFinish: Button = findViewById(R.id.btn_finish)

        // Recuperamos los datos que nos mandó el juego
        val totalQuestions = intent.getIntExtra("TOTAL_QUESTIONS", 0)
        val correctAnswers = intent.getIntExtra("CORRECT_ANSWERS", 0)

        // Calculamos el porcentaje
        val percentage = (correctAnswers.toDouble() / totalQuestions.toDouble()) * 100

        tvScoreText.text = "Acertaste $correctAnswers de $totalQuestions"
        tvPercentage.text = "${percentage.toInt()}%"

        // Botón para salir al menú principal (MainActivity)
        btnFinish.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            // Esto borra el historial para que no pueda volver atrás con el botón del celular
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}