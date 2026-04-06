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
            Game3Question("D", R.drawable.sena_d),
            Game3Question("E", R.drawable.sena_e),
            Game3Question("F", R.drawable.sena_f),
            Game3Question("G", R.drawable.sena_g),
            Game3Question("H", R.drawable.sena_h),
            Game3Question("I", R.drawable.sena_i),
            Game3Question("J", R.drawable.sena_j),
            Game3Question("K", R.drawable.sena_k),
            Game3Question("L", R.drawable.sena_l),
            Game3Question("M", R.drawable.sena_m),
            Game3Question("N", R.drawable.sena_n),
            Game3Question("Ñ", R.drawable.sena_n2),
            Game3Question("O", R.drawable.sena_o),
            Game3Question("P", R.drawable.sena_p),
            Game3Question("Q", R.drawable.sena_q),
            Game3Question("R", R.drawable.sena_r),
            Game3Question("S", R.drawable.sena_s),
            Game3Question("T", R.drawable.sena_t),
            Game3Question("U", R.drawable.sena_u),
            Game3Question("V", R.drawable.sena_v),
            Game3Question("W", R.drawable.sena_w),
            Game3Question("X", R.drawable.sena_x),
            Game3Question("Y", R.drawable.sena_y) // Ejemplo
            // Agrega todas las que soporte tu modelo aquí
        )
        return list.shuffled()
    }

    fun getQuickModeQuestions(): List<Game3Question> {
        val all = getFullModeQuestions()
        return all.take(Math.min(10, all.size))
    }
}