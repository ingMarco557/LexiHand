package com.example.lexihand

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.card.MaterialCardView

class MenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 1. Inflamos la vista raíz
        val rootView = inflater.inflate(R.layout.fragment_menu, container, false)

        // --- CONFIGURACIÓN DE NAVEGACIÓN ---

        // 10. PERFIL (¡ACTIVADO!)
        rootView.findViewById<MaterialCardView>(R.id.cardProfile)?.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }

        // 9. DIAGNÓSTICO
        rootView.findViewById<MaterialCardView>(R.id.cardDiagnostics)?.setOnClickListener {
            val intent = Intent(requireContext(), DiagnosticsActivity::class.java)
            startActivity(intent)
        }

        // 11. ESTADÍSTICAS
        rootView.findViewById<MaterialCardView>(R.id.cardStats)?.setOnClickListener {
            val intent = Intent(requireContext(), StatisticsActivity::class.java)
            startActivity(intent)
        }

        // 12. CONFIGURACIÓN
        rootView.findViewById<MaterialCardView>(R.id.cardSettings)?.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }

        // 19. ENTRENAMIENTO ML (Lo dejamos listo para cuando crees la Activity)
        rootView.findViewById<MaterialCardView>(R.id.cardTraining)?.setOnClickListener {
            // Cuando crees ModelTrainingActivity, descomenta estas líneas:
            // val intent = Intent(requireContext(), ModelTrainingActivity::class.java)
            // startActivity(intent)
        }

        rootView.findViewById<MaterialCardView>(R.id.cardTraining)?.setOnClickListener {
            val intent = Intent(requireContext(), DataCollectionActivity::class.java)
            startActivity(intent)
        }


        return rootView
    }
}