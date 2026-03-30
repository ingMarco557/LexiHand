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
        // INFLAMOS la vista primero
        val rootView = inflater.inflate(R.layout.fragment_menu, container, false)

        // --- CONFIGURACIÓN DE CLICS (Solo se activan cuando el usuario toca el botón) ---

        // 9. Diagnóstico (Corregido: Ahora solo abre al hacer CLICK)
        rootView.findViewById<MaterialCardView>(R.id.cardDiagnostics)?.setOnClickListener {
            val intent = Intent(requireContext(), DiagnosticsActivity::class.java)
            startActivity(intent)
        }

        // 11. Estadísticas
        rootView.findViewById<MaterialCardView>(R.id.cardStats)?.setOnClickListener {
            val intent = Intent(requireContext(), StatisticsActivity::class.java)
            startActivity(intent)
        }

        // 12. Configuración
        rootView.findViewById<MaterialCardView>(R.id.cardSettings)?.setOnClickListener {
            val intent = Intent(requireContext(), SettingsActivity::class.java)
            startActivity(intent)
        }

        // 10. Perfil (Mantenemos comentado hasta que crees la Activity)
        rootView.findViewById<MaterialCardView>(R.id.cardProfile)?.setOnClickListener {
            // val intent = Intent(requireContext(), ProfileActivity::class.java)
            // startActivity(intent)
        }

        // 19. Entrenamiento ML
        rootView.findViewById<MaterialCardView>(R.id.cardTraining)?.setOnClickListener {
            // val intent = Intent(requireContext(), ModelTrainingActivity::class.java)
            // startActivity(intent)
        }

        return rootView
    }
}