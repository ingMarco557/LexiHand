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

        // 1. Enlazamos la vista con el código
        tvProgress = findViewById(R.id.tv_progress)
        tvTimer = findViewById(R.id.tv_timer)
        ivSignImage = findViewById(R.id.iv_sign_image)
        btnOption1 = findViewById(R.id.btn_option_1)
        btnOption2 = findViewById(R.id.btn_option_2)
        btnOption3 = findViewById(R.id.btn_option_3)
        btnOption4 = findViewById(R.id.btn_option_4)

        // 2. ¿Qué modo de juego eligió el usuario?
        val gameMode = intent.getStringExtra("GAME_MODE")

        // 3. Cargamos las preguntas según el modo
        questionsList = if (gameMode == "QUICK") {
            GameRepository.getQuickModeQuestions()
        } else {
            GameRepository.getFullModeQuestions()
        }

        // 4. Arrancamos la primera pregunta
        setQuestion()
    }

    private fun setQuestion() {
        // Obtenemos la pregunta actual
        val question = questionsList[currentPosition]

        // Actualizamos textos e imagen
        tvProgress.text = "${currentPosition + 1}/${questionsList.size}"
        ivSignImage.setImageResource(question.imageResourceId)

        // Mezclamos las opciones para que la correcta cambie de botón
        val options = mutableListOf(
            question.correctAnswer,
            question.choiceOne,
            question.choiceTwo,
            question.choiceThree
        )
        options.shuffle()

        // Asignamos las letras a los botones
        btnOption1.text = options[0]
        btnOption2.text = options[1]
        btnOption3.text = options[2]
        btnOption4.text = options[3]

        // Configuramos qué pasa al hacer clic
        btnOption1.setOnClickListener { checkAnswer(btnOption1.text.toString(), question.correctAnswer) }
        btnOption2.setOnClickListener { checkAnswer(btnOption2.text.toString(), question.correctAnswer) }
        btnOption3.setOnClickListener { checkAnswer(btnOption3.text.toString(), question.correctAnswer) }
        btnOption4.setOnClickListener { checkAnswer(btnOption4.text.toString(), question.correctAnswer) }

        // Iniciamos el cronómetro de 10 segundos
        startTimer()
    }

    private fun startTimer() {
        timer?.cancel() // Cancelamos si había uno anterior

        // 10000 milisegundos = 10 segundos. Se actualiza cada 1000 ms (1 segundo)
        timer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvTimer.text = "⏱️ ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                // Si el tiempo se acaba, es como si hubiera fallado
                tvTimer.text = "⏱️ 0s"
                showFeedbackDialog(false, "¡Se acabó el tiempo!")
            }
        }.start()
    }

    private fun checkAnswer(selectedAnswer: String, correctAnswer: String) {
        timer?.cancel() // Detenemos el reloj porque ya respondió

        if (selectedAnswer == correctAnswer) {
            correctAnswersCount++
            showFeedbackDialog(true, "¡Excelente, vamos por la siguiente!")
        } else {
            showFeedbackDialog(false, "Qué vergüenza, espero que en esta sí puedas. Era la letra: $correctAnswer")
        }
    }

    private fun showFeedbackDialog(isCorrect: Boolean, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(if (isCorrect) "¡Correcto! ✅" else "¡Incorrecto! ❌")
        builder.setMessage(message)
        builder.setCancelable(false) // Para que no lo cierre tocando afuera

        builder.setPositiveButton("Continuar") { dialog, _ ->
            dialog.dismiss()
            currentPosition++ // Pasamos a la siguiente pregunta

            if (currentPosition < questionsList.size) {
                setQuestion() // Cargamos la siguiente
            } else {
                // Se acabaron las preguntas, vamos a los resultados
                val intent = Intent(this, ResultActivity::class.java)
                intent.putExtra("TOTAL_QUESTIONS", questionsList.size)
                intent.putExtra("CORRECT_ANSWERS", correctAnswersCount)
                startActivity(intent)
                finish() // Cerramos el juego
            }
        }
        builder.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel() // Aseguramos que el reloj se detenga si sale de la app
    }
}