
package com.example.lexihand

object GameRepository {

    // 🌟 ESTA ES TU BASE DE DATOS DE PREGUNTAS 🌟
    // Puedes agregar tantas preguntas como quieras siguiendo este formato.
    private val allQuestionsList: List<QuestionData> = listOf(
        QuestionData(
            R.drawable.sena_a, // Asegúrate de que tienes una imagen en drawable con este nombre
            "A",               // La correcta
            "D", "G", "J"      // Las incorrectas
        ),
        QuestionData(
            R.drawable.sena_b, // Asegúrate de que tienes una imagen en drawable con este nombre
            "B",
            "M", "R", "X"
        ),
        QuestionData(
            R.drawable.sena_c, // Asegúrate de que tienes una imagen en drawable con este nombre
            "C",
            "E", "I", "O"
        ),
        QuestionData(
            R.drawable.sena_d, // Asegúrate de que tienes una imagen en drawable con este nombre
            "D",
            "S", "A", "K"
        ),
        QuestionData(
            R.drawable.sena_e, // Asegúrate de que tienes una imagen en drawable con este nombre
            "E",
            "R", "K", "Q"
        ),
        QuestionData(
            R.drawable.sena_f, // Asegúrate de que tienes una imagen en drawable con este nombre
            "F",
            "K", "H", "C"
        ),
        QuestionData(
            R.drawable.sena_g, // Asegúrate de que tienes una imagen en drawable con este nombre
            "G",
            "C", "I", "P"
        ),
        QuestionData(
            R.drawable.sena_h, // Asegúrate de que tienes una imagen en drawable con este nombre
            "H",
            "E", "Y", "I"
        ),
        QuestionData(
            R.drawable.sena_i, // Asegúrate de que tienes una imagen en drawable con este nombre
            "I",
            "X", "V", "L"
        ),
        QuestionData(
            R.drawable.sena_j, // Asegúrate de que tienes una imagen en drawable con este nombre
            "J",
            "W", "K", "E"
        ),
        QuestionData(
            R.drawable.sena_k, // Asegúrate de que tienes una imagen en drawable con este nombre
            "K",
            "E", "M", "Q"
        ),
        QuestionData(
            R.drawable.sena_l, // Asegúrate de que tienes una imagen en drawable con este nombre
            "L",
            "A", "O", "B"
        ),
        QuestionData(
            R.drawable.sena_m, // Asegúrate de que tienes una imagen en drawable con este nombre
            "M",
            "Q", "S", "F"
        ),
        QuestionData(
            R.drawable.sena_n, // Asegúrate de que tienes una imagen en drawable con este nombre
            "N",
            "W", "H", "T"
        ),
        QuestionData(
            R.drawable.sena_n2, // Asegúrate de que tienes una imagen en drawable con este nombre
            "N2",
            "P", "U", "M"
        ),
        QuestionData(
            R.drawable.sena_o, // Asegúrate de que tienes una imagen en drawable con este nombre
            "O",
            "F", "K", "L"
        ),
        QuestionData(
            R.drawable.sena_p, // Asegúrate de que tienes una imagen en drawable con este nombre
            "P",
            "X", "C", "V"
        ),
        QuestionData(
            R.drawable.sena_q, // Asegúrate de que tienes una imagen en drawable con este nombre
            "Q",
            "P", "O", "I"
        ),
        QuestionData(
            R.drawable.sena_r, // Asegúrate de que tienes una imagen en drawable con este nombre
            "R",
            "U", "Y", "T"
        ),
        QuestionData(
            R.drawable.sena_s, // Asegúrate de que tienes una imagen en drawable con este nombre
            "S",
            "K", "L", "P"
        ),
        QuestionData(
            R.drawable.sena_t, // Asegúrate de que tienes una imagen en drawable con este nombre
            "T",
            "G", "H", "J"
        ),
        QuestionData(
            R.drawable.sena_u, // Asegúrate de que tienes una imagen en drawable con este nombre
            "U",
            "S", "D", "F"
        ),
        QuestionData(
            R.drawable.sena_v, // Asegúrate de que tienes una imagen en drawable con este nombre
            "V",
            "B", "M", "Z"
        ),
        QuestionData(
            R.drawable.sena_w, // Asegúrate de que tienes una imagen en drawable con este nombre
            "W",
            "V", "B", "N"
        ),
        QuestionData(
            R.drawable.sena_x, // Asegúrate de que tienes una imagen en drawable con este nombre
            "X",
            "D", "O", "M"
        ),
        QuestionData(
            R.drawable.sena_y, // Asegúrate de que tienes una imagen en drawable con este nombre
            "Y",
            "A", "L", "E"
        )
        // Agrega más aquí...
    )

    // Esta función regresa las preguntas para el Modo Completo (todas)
    fun getFullModeQuestions(): List<QuestionData> {
        return allQuestionsList.shuffled() // .shuffled() las mezcla de forma aleatoria
    }

    // Esta función regresa las preguntas para el Modo Rápido (solo 10 aleatorias)
    fun getQuickModeQuestions(): List<QuestionData> {
        return allQuestionsList.shuffled().take(10) // .shuffled() las mezcla, .take(10) toma solo las primeras 10
    }
}
