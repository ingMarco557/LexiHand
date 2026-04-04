package com.example.lexihand


data class WordQuestionData(
    val answerWord: String,
    val imageResIds: List<Int> // Una lista de imágenes que forman la palabra
)
object Game2Repository {

    // Todas las palabras disponibles (El modo completo usará todas)
    // Cambia esto en tu Game2Repository.kt para que sea aleatorio:
    fun getFullModeWords(): List<WordQuestionData> {
        val list = listOf(

            WordQuestionData("red", listOf(R.drawable.sena_r, R.drawable.sena_e, R.drawable.sena_d)),
            WordQuestionData("bit", listOf(R.drawable.sena_b, R.drawable.sena_i, R.drawable.sena_t)),

            WordQuestionData("mano", listOf(R.drawable.sena_m, R.drawable.sena_a, R.drawable.sena_n, R.drawable.sena_o)),
            WordQuestionData( answerWord= "amor", imageResIds = listOf(R.drawable.sena_a, R.drawable.sena_m, R.drawable.sena_o, R.drawable.sena_r)),
            WordQuestionData("mouse", listOf(R.drawable.sena_m, R.drawable.sena_o, R.drawable.sena_u, R.drawable.sena_s, R.drawable.sena_e)),
            WordQuestionData("fibra", listOf(R.drawable.sena_f, R.drawable.sena_i, R.drawable.sena_b, R.drawable.sena_r, R.drawable.sena_a)),
            WordQuestionData("vida", listOf(R.drawable.sena_v, R.drawable.sena_i, R.drawable.sena_d, R.drawable.sena_a)),
            WordQuestionData("lobo", listOf(R.drawable.sena_l, R.drawable.sena_o, R.drawable.sena_b, R.drawable.sena_o)),
            WordQuestionData("cable", listOf(R.drawable.sena_c, R.drawable.sena_a, R.drawable.sena_b, R.drawable.sena_l, R.drawable.sena_e)),
            WordQuestionData("clave", listOf(R.drawable.sena_c, R.drawable.sena_l, R.drawable.sena_a, R.drawable.sena_v, R.drawable.sena_e)),
            WordQuestionData("gato", listOf(R.drawable.sena_g, R.drawable.sena_a, R.drawable.sena_t, R.drawable.sena_o)),
            WordQuestionData("luna", listOf(R.drawable.sena_l, R.drawable.sena_u, R.drawable.sena_n, R.drawable.sena_a)),
            WordQuestionData("nube", listOf(R.drawable.sena_n, R.drawable.sena_u, R.drawable.sena_b, R.drawable.sena_e)),


            WordQuestionData("perro", listOf(R.drawable.sena_p, R.drawable.sena_e, R.drawable.sena_r, R.drawable.sena_r, R.drawable.sena_o)),
            WordQuestionData("fruta", listOf(R.drawable.sena_f, R.drawable.sena_r, R.drawable.sena_u, R.drawable.sena_t, R.drawable.sena_a)),
            WordQuestionData("fuego", listOf(R.drawable.sena_f, R.drawable.sena_u, R.drawable.sena_e, R.drawable.sena_g, R.drawable.sena_o)),
            WordQuestionData("verde", listOf(R.drawable.sena_v, R.drawable.sena_e, R.drawable.sena_r, R.drawable.sena_d, R.drawable.sena_e)),
            WordQuestionData("codigo", listOf(R.drawable.sena_c, R.drawable.sena_o, R.drawable.sena_d, R.drawable.sena_i, R.drawable.sena_g, R.drawable.sena_o)),
            WordQuestionData("puerto", listOf(R.drawable.sena_p, R.drawable.sena_u, R.drawable.sena_e, R.drawable.sena_r, R.drawable.sena_t, R.drawable.sena_o)),
            WordQuestionData("server", listOf(R.drawable.sena_s, R.drawable.sena_e, R.drawable.sena_r, R.drawable.sena_v, R.drawable.sena_e, R.drawable.sena_r)),
            WordQuestionData("binario", listOf(R.drawable.sena_b, R.drawable.sena_i, R.drawable.sena_n, R.drawable.sena_a, R.drawable.sena_r, R.drawable.sena_i, R.drawable.sena_o)),
            WordQuestionData("libros", listOf(R.drawable.sena_l, R.drawable.sena_i, R.drawable.sena_b, R.drawable.sena_r, R.drawable.sena_o, R.drawable.sena_s))

        )

        return list.shuffled() // 🌟 ¡Esto hace la magia de revolverlas todas!
    }




    // Modo rápido (Agarra solo unas cuantas al azar, por ahora puse 2 como ejemplo)
    fun getQuickModeWords(): List<WordQuestionData> {
        val allWords = getFullModeWords().shuffled()
        // Si tienes más de 10, cambias el 2 por un 10
        return allWords.take(Math.min(2, allWords.size))
    }
}