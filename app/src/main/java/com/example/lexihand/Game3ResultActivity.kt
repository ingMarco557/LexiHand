package com.example.lexihand

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class Game3ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game3_result)

        val total = intent.getIntExtra("TOTAL_QUESTIONS", 0)
        val correct = intent.getIntExtra("CORRECT_ANSWERS", 0)
        val percentage = if (total > 0) (correct.toDouble() / total) * 100 else 0.0

        findViewById<TextView>(R.id.tv_g3_score).text = "Replicaste $correct de $total"
        findViewById<TextView>(R.id.tv_g3_percentage).text = "${percentage.toInt()}%"

        findViewById<Button>(R.id.btn_g3_finish).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}