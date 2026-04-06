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
    private lateinit var btnVincular: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        // Inicializar vistas
        txtRacha = root.findViewById(R.id.txtRachaHome)
        txtPrecision = root.findViewById(R.id.txtPrecisionHome)
        btnVincular = root.findViewById(R.id.btnVincularGuante)

        btnVincular.setOnClickListener {
            vincularDispositivo()
        }

        return root
    }

    // Usamos onResume para que se actualice CADA VEZ que el usuario entra a esta pestaña
    override fun onResume() {
        super.onResume()
        cargarEstadisticasReales()
    }

    private fun cargarEstadisticasReales() {
        // Leemos los datos guardados en el celular
        val rachaReal = LexiDataManager.obtenerRacha(requireContext())
        val precisionGlobal = LexiDataManager.obtenerPrecisionGlobal(requireContext())

        // Actualizamos la pantalla
        updateStats(rachaReal, precisionGlobal)
    }

    private fun vincularDispositivo() {
        val mainActivity = activity as? MainActivity
        if (mainActivity != null) {
            mainActivity.iniciarVinculacion()
            Toast.makeText(requireContext(), "Buscando LexiHand...", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateStats(racha: Int, precision: Int) {
        txtRacha.text = "$racha 🔥"
        txtPrecision.text = "$precision% 🎯"
    }
}