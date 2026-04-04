package com.example.lexihand

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Game2ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game2_result)

        val tvScoreText: TextView = findViewById(R.id.tv_game2_score_text)
        val tvPercentage: TextView = findViewById(R.id.tv_game2_percentage)
        val btnFinish: Button = findViewById(R.id.btn_game2_finish)

        val totalQuestions = intent.getIntExtra("TOTAL_QUESTIONS", 0)
        val correctAnswers = intent.getIntExtra("CORRECT_ANSWERS", 0)

        val percentage = if (totalQuestions > 0) {
            (correctAnswers.toDouble() / totalQuestions.toDouble()) * 100
        } else 0.0

        tvScoreText.text = "Descifraste $correctAnswers de $totalQuestions"
        tvPercentage.text = "${percentage.toInt()}%"

        btnFinish.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}