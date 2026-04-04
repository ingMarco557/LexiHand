
package com.example.lexihand
    // Esta clase guarda los datos de una sola pregunta
    data class QuestionData(
        val imageResourceId: Int,      // Aquí guardamos el ID de la imagen de la seña (ej: R.drawable.sena_a)
        val correctAnswer: String,     // La letra correcta
        val choiceOne: String,         // Una opción incorrecta
        val choiceTwo: String,         // Otra opción incorrecta
        val choiceThree: String        // Una última opción incorrecta
    )


