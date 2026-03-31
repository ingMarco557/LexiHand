package com.example.lexihand

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class HomeFragment : Fragment() {

    private lateinit var txtRacha: TextView
    private lateinit var txtPrecision: TextView
    private lateinit var btnVincular: Button // Nuevo: Referencia al botón

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        // 1. Inicializar vistas de estadísticas
        txtRacha = root.findViewById(R.id.txtRachaHome)
        txtPrecision = root.findViewById(R.id.txtPrecisionHome)

        // 2. Inicializar Botón de Vinculación
        // Asegúrate de que en tu fragment_home.xml el botón tenga este ID
        btnVincular = root.findViewById(R.id.btnVincularGuante)

        btnVincular.setOnClickListener {
            vincularDispositivo()
        }

        // Datos iniciales
        updateStats(15, 92)

        return root
    }

    /**
     * Llama al método del MainActivity para iniciar el escaneo de Bluetooth
     */
    private fun vincularDispositivo() {
        val mainActivity = activity as? MainActivity
        if (mainActivity != null) {
            // Ejecutamos la función que creamos en el MainActivity
            mainActivity.iniciarVinculacion()

            // Opcional: Feedback inmediato en el fragmento
            Toast.makeText(requireContext(), "Buscando LexiHand...", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Actualiza los indicadores visuales de progreso del usuario
     */
    fun updateStats(racha: Int, precision: Int) {
        txtRacha.text = "$racha 🔥"
        txtPrecision.text = "$precision% 🎯"
    }
}