package com.example.lexihand

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class Game2Activity : AppCompatActivity() {

    private lateinit var tvProgress: TextView
    private lateinit var tvTimer: TextView
    private lateinit var layoutWordImages: LinearLayout
    private lateinit var etUserAnswer: EditText
    private lateinit var btnSubmitAnswer: Button

    private var wordsList: List<WordQuestionData> = listOf()
    private var currentPosition: Int = 0
    private var correctAnswersCount: Int = 0
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game2)

        tvProgress = findViewById(R.id.tv_game2_progress)
        tvTimer = findViewById(R.id.tv_game2_timer)
        layoutWordImages = findViewById(R.id.layout_word_images)
        etUserAnswer = findViewById(R.id.et_user_answer)
        btnSubmitAnswer = findViewById(R.id.btn_submit_answer)

        val gameMode = intent.getStringExtra("GAME_MODE")

        wordsList = if (gameMode == "QUICK") {
            Game2Repository.getQuickModeWords()
        } else {
            Game2Repository.getFullModeWords()
        }

        setQuestion()

        btnSubmitAnswer.setOnClickListener {
            val answer = etUserAnswer.text.toString().trim()
            if (answer.isNotEmpty()) {
                checkAnswer(answer, wordsList[currentPosition].answerWord)
            }
        }
    }

    private fun setQuestion() {
        val question = wordsList[currentPosition]

        // 1. Textos
        tvProgress.text = "${currentPosition + 1}/${wordsList.size}"
        etUserAnswer.text.clear() // Limpiamos la caja de texto

        // 2. Limpiar imágenes anteriores y cargar las nuevas
        // --- CÓDIGO A REEMPLAZAR DENTRO DE setQuestion() ---

        // 2. Limpiar imágenes anteriores y cargar las nuevas
        layoutWordImages.removeAllViews()
        for (imageRes in question.imageResIds) {
            val imageView = ImageView(this)

            // 🌟 REPARACIÓN 🌟
            // Definimos un tamaño fijo y más pequeño para cada imagen (ej. 80dp x 80dp)
            // para que quepan más en la pantalla.
            val sizeInDp = 80
            val sizeInPx = (sizeInDp * resources.displayMetrics.density).toInt()

            val params = LinearLayout.LayoutParams(sizeInPx, sizeInPx)
            params.setMargins(8, 0, 8, 0) // Márgenes entre letras (izquierda, arriba, derecha, abajo)
            imageView.layoutParams = params

            imageView.setImageResource(imageRes)
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER

            layoutWordImages.addView(imageView)
        }

        startTimer()
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvTimer.text = "Escríbelo en ⏱️ ${millisUntilFinished / 1000}"
            }

            override fun onFinish() {
                tvTimer.text = "⏱️ 0"
                checkAnswer("", wordsList[currentPosition].answerWord) // Se acabó el tiempo
            }
        }.start()
    }

    private fun checkAnswer(userAnswer: String, correctAnswer: String) {
        timer?.cancel()

        // Convertimos ambas a minúsculas para que no importe si escribe "MANO" o "mano"
        if (userAnswer.lowercase() == correctAnswer.lowercase()) {
            correctAnswersCount++
            showFeedbackDialog(true, "¡Excelente, pensé que no lo lograrías! Vamos por la siguiente.")
        } else {
            showFeedbackDialog(false, "Qué decepción, esperaba más de ti.\n\nEra la palabra: ${correctAnswer.uppercase()}")
        }
    }

    private fun showFeedbackDialog(isCorrect: Boolean, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(if (isCorrect) "¡Correcto! ✅" else "¡Incorrecto! ❌")
        builder.setMessage(message)
        builder.setCancelable(false)

        builder.setPositiveButton("Continuar") { dialog, _ ->
            dialog.dismiss()
            currentPosition++

            if (currentPosition < wordsList.size) {
                setQuestion()
            } else {
                val intent = Intent(this, Game2ResultActivity::class.java)
                intent.putExtra("TOTAL_QUESTIONS", wordsList.size)
                intent.putExtra("CORRECT_ANSWERS", correctAnswersCount)
                startActivity(intent)
                finish()
            }
        }
        builder.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}