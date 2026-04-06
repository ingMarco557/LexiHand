package com.example.lexihand

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LexiDataManager {

    private const val PREFS_NAME = "LexiStatsPrefs"
    private const val KEY_STREAK = "current_streak"
    private const val KEY_LAST_DATE = "last_played_date"

    // 1. Lógica de Rachas (Anti-trampas)
    fun registrarJuegoCompletado(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastDateStr = prefs.getString(KEY_LAST_DATE, "")
        val currentStreak = prefs.getInt(KEY_STREAK, 0)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Date()
        val todayStr = sdf.format(today)

        if (lastDateStr == todayStr) {
            // Ya jugó hoy, mantenemos la racha igual
            return
        }

        if (lastDateStr.isNullOrEmpty()) {
            // Primera vez que juega
            prefs.edit().putInt(KEY_STREAK, 1).putString(KEY_LAST_DATE, todayStr).apply()
            return
        }

        try {
            val lastDate = sdf.parse(lastDateStr)
            val diffInMillis = today.time - lastDate!!.time
            val diffInDays = diffInMillis / (1000 * 60 * 60 * 24)

            if (diffInDays == 1L) {
                // Jugó ayer, aumenta la racha
                prefs.edit().putInt(KEY_STREAK, currentStreak + 1).putString(KEY_LAST_DATE, todayStr).apply()
            } else {
                // Trampa (fecha pasada) o perdió la racha (pasaron más de 1 día)
                prefs.edit().putInt(KEY_STREAK, 1).putString(KEY_LAST_DATE, todayStr).apply()
            }
        } catch (e: Exception) {
            prefs.edit().putInt(KEY_STREAK, 1).putString(KEY_LAST_DATE, todayStr).apply()
        }
    }

    fun obtenerRacha(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_STREAK, 0)
    }

    // 2. Lógica de Estadísticas por Letra
    fun registrarIntentoLetra(context: Context, letra: Char, fueCorrecto: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val charUpper = letra.uppercaseChar()

        val totalKey = "TOTAL_$charUpper"
        val correctKey = "CORRECT_$charUpper"

        val total = prefs.getInt(totalKey, 0) + 1
        val correct = prefs.getInt(correctKey, 0) + (if (fueCorrecto) 1 else 0)

        prefs.edit().putInt(totalKey, total).putInt(correctKey, correct).apply()
    }

    fun obtenerStatsLetra(context: Context, letra: Char): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val total = prefs.getInt("TOTAL_${letra.uppercaseChar()}", 0)
        val correct = prefs.getInt("CORRECT_${letra.uppercaseChar()}", 0)
        if (total == 0) return 0
        return ((correct.toFloat() / total.toFloat()) * 100).toInt()
    }

    fun obtenerPrecisionGlobal(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var totalAll = 0
        var correctAll = 0
        for (c in 'A'..'Z') {
            totalAll += prefs.getInt("TOTAL_$c", 0)
            correctAll += prefs.getInt("CORRECT_$c", 0)
        }
        if (totalAll == 0) return 0
        return ((correctAll.toFloat() / totalAll.toFloat()) * 100).toInt()
    }
}