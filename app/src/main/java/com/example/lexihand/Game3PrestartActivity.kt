package com.example.lexihand

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Game3PrestartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game3_prestart)

        val gameMode = intent.getStringExtra("GAME_MODE") ?: "QUICK"
        findViewById<Button>(R.id.btn_start_game3).setOnClickListener {
            val intent = Intent(this, Game3Activity::class.java)
            intent.putExtra("GAME_MODE", gameMode)
            startActivity(intent)
            finish()
        }
    }
}