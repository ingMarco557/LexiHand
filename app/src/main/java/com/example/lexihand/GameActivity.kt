package com.example.lexihand

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {

    private lateinit var tvProgress: TextView
    private lateinit var tvTimer: TextView
    private lateinit var ivSignImage: ImageView
    private lateinit var btnOption1: Button
    private lateinit var btnOption2: Button
    private lateinit var btnOption3: Button
    private lateinit var btnOption4: Button

    private var questionsList: List<QuestionData> = listOf()
    private var currentPosition: Int = 0
    private var correctAnswersCount: Int = 0
    private var timer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        tvProgress = findViewById(R.id.tv_progress)
        tvTimer = findViewById(R.id.tv_timer)
        ivSignImage = findViewById(R.id.iv_sign_image)
        btnOption1 = findViewById(R.id.btn_option_1)
        btnOption2 = findViewById(R.id.btn_option_2)
        btnOption3 = findViewById(R.id.btn_option_3)
        btnOption4 = findViewById(R.id.btn_option_4)

        val gameMode = intent.getStringExtra("GAME_MODE")

        questionsList = if (gameMode == "QUICK") {
            GameRepository.getQuickModeQuestions()
        } else {
            GameRepository.getFullModeQuestions()
        }

        setQuestion()
    }

    private fun setQuestion() {
        val question = questionsList[currentPosition]

        tvProgress.text = "${currentPosition + 1}/${questionsList.size}"
        ivSignImage.setImageResource(question.imageResourceId)

        val options = mutableListOf(
            question.correctAnswer,
            question.choiceOne,
            question.choiceTwo,
            question.choiceThree
        )
        options.shuffle()

        btnOption1.text = options[0]
        btnOption2.text = options[1]
        btnOption3.text = options[2]
        btnOption4.text = options[3]

        btnOption1.setOnClickListener { checkAnswer(btnOption1.text.toString(), question.correctAnswer) }
        btnOption2.setOnClickListener { checkAnswer(btnOption2.text.toString(), question.correctAnswer) }
        btnOption3.setOnClickListener { checkAnswer(btnOption3.text.toString(), question.correctAnswer) }
        btnOption4.setOnClickListener { checkAnswer(btnOption4.text.toString(), question.correctAnswer) }

        startTimer()
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvTimer.text = "⏱️ ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                tvTimer.text = "⏱️ 0s"

                // Si el tiempo se acaba, el usuario falla en la letra actual
                val targetChar = questionsList[currentPosition].correctAnswer.first()
                LexiDataManager.registrarIntentoLetra(this@GameActivity, targetChar, false)

                showFeedbackDialog(false, "¡Se acabó el tiempo!")
            }
        }.start()
    }

    private fun checkAnswer(selectedAnswer: String, correctAnswer: String) {
        timer?.cancel()

        val targetChar = correctAnswer.first() // De String "A" pasa a Char 'A'

        if (selectedAnswer == correctAnswer) {
            correctAnswersCount++
            // ✅ Guardamos acierto en la precisión de la letra
            LexiDataManager.registrarIntentoLetra(this, targetChar, true)

            showFeedbackDialog(true, "¡Excelente, vamos por la siguiente!")
        } else {
            // ❌ Guardamos fallo en la precisión de la letra
            LexiDataManager.registrarIntentoLetra(this, targetChar, false)

            showFeedbackDialog(false, "Qué vergüenza, espero que en esta sí puedas. Era la letra: $correctAnswer")
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

            if (currentPosition < questionsList.size) {
                setQuestion()
            } else {
                // 🌟 AQUÍ REGISTRAMOS LA RACHA AL COMPLETAR EL JUEGO 🌟
                LexiDataManager.registrarJuegoCompletado(this)

                val intent = Intent(this, ResultActivity::class.java)
                intent.putExtra("TOTAL_QUESTIONS", questionsList.size)
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