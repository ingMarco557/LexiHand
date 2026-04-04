package com.example.lexihand

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Game2PrestartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game2_prestart)

        val btnStart: Button = findViewById(R.id.btn_start_game2)
        val gameMode = intent.getStringExtra("GAME_MODE") ?: "QUICK"

        btnStart.setOnClickListener {
            val intent = Intent(this, Game2Activity::class.java)
            intent.putExtra("GAME_MODE", gameMode)
            startActivity(intent)
            finish()
        }
    }
}