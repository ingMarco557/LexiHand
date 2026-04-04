package com.example.lexihand

data class Game3Question(
    val letterTarget: String, // La letra que el modelo TFLite debe detectar
    val imageResId: Int       // La imagen que se le muestra al usuario
)

object Game3Repository {
    fun getFullModeQuestions(): List<Game3Question> {
        val list = listOf(
            Game3Question("A", R.drawable.sena_a), // Cambia sena_a por tus imágenes reales
            Game3Question("B", R.drawable.sena_b),
            Game3Question("C", R.drawable.sena_c),
            Game3Question("D", R.drawable.sena_d) // Ejemplo
            // Agrega todas las que soporte tu modelo aquí
        )
        return list.shuffled()
    }

    fun getQuickModeQuestions(): List<Game3Question> {
        val all = getFullModeQuestions()
        return all.take(Math.min(10, all.size))
    }
}