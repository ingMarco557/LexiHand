package com.example.lexihand

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class HomeFragment : Fragment() {

    private lateinit var txtRacha: TextView
    private lateinit var txtPrecision: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflamos el layout que contendrá el diseño de la racha
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        // Inicializamos las vistas del Dashboard local
        txtRacha = root.findViewById(R.id.txtRachaHome)
        txtPrecision = root.findViewById(R.id.txtPrecisionHome)

        // Por ahora hardcodeamos los datos, luego vendrán de StatisticsService
        updateStats(15, 92)

        return root
    }

    /**
     * Actualiza los indicadores visuales de progreso del usuario
     */
    fun updateStats(racha: Int, precision: Int) {
        txtRacha.text = "$racha 🔥"
        txtPrecision.text = "$precision% 🎯"
    }
}