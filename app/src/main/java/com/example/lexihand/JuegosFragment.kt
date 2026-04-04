package com.example.lexihand

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment

class JuegosFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflamos el diseño de tu fragment_juegos.xml
        return inflater.inflate(R.layout.fragment_juegos, container, false)
    }

    // 🌟 AQUÍ OCURRE LA MAGIA: onViewCreated se ejecuta justo después de que la pantalla se dibuja
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- CONTROLES JUEGO 1: Adivinar la Seña ---
        val btnGameOneHeader: Button = view.findViewById(R.id.btn_game_one_header)
        val layoutSubmenuGameOne: LinearLayout = view.findViewById(R.id.layout_submenu_game_one)
        val btnGameOneQuick: Button = view.findViewById(R.id.btn_game_one_quick)
        val btnGameOneFull: Button = view.findViewById(R.id.btn_game_one_full)

        btnGameOneHeader.setOnClickListener {
            layoutSubmenuGameOne.visibility = if (layoutSubmenuGameOne.visibility == View.GONE) View.VISIBLE else View.GONE
        }
        btnGameOneQuick.setOnClickListener { startGame1("QUICK") }
        btnGameOneFull.setOnClickListener { startGame1("FULL") }


        // --- CONTROLES JUEGO 2: Descifrar la Palabra ---
        // (Asegúrate de que los IDs coincidan con tu XML)
        val btnGameTwoHeader: Button = view.findViewById(R.id.btn_game_two_header)
        val layoutSubmenuGameTwo: LinearLayout = view.findViewById(R.id.layout_submenu_game_two)
        val btnGameTwoQuick: Button = view.findViewById(R.id.btn_game_two_quick)
        val btnGameTwoFull: Button = view.findViewById(R.id.btn_game_two_full)

        btnGameTwoHeader.setOnClickListener {
            layoutSubmenuGameTwo.visibility = if (layoutSubmenuGameTwo.visibility == View.GONE) View.VISIBLE else View.GONE
        }
        btnGameTwoQuick.setOnClickListener { startGame2("QUICK") }
        btnGameTwoFull.setOnClickListener { startGame2("FULL") }


        // --- CONTROLES JUEGO 3: Réplica la Imagen ---
        val btnGameThreeHeader: Button = view.findViewById(R.id.btn_game_three_header)
        val layoutSubmenuGameThree: LinearLayout = view.findViewById(R.id.layout_submenu_game_three)
        val btnGameThreeQuick: Button = view.findViewById(R.id.btn_game_three_quick)
        val btnGameThreeFull: Button = view.findViewById(R.id.btn_game_three_full)

        btnGameThreeHeader.setOnClickListener {
            layoutSubmenuGameThree.visibility = if (layoutSubmenuGameThree.visibility == View.GONE) View.VISIBLE else View.GONE
        }
        btnGameThreeQuick.setOnClickListener { startGame3("QUICK") }
        btnGameThreeFull.setOnClickListener { startGame3("FULL") }
    }

    // --- FUNCIONES PARA ABRIR LOS JUEGOS ---
    // Usamos requireContext() en lugar de 'this' porque estamos en un Fragmento
    private fun startGame1(mode: String) {
        val intent = Intent(requireContext(), GamePrestartActivity::class.java)
        intent.putExtra("GAME_MODE", mode)
        startActivity(intent)
    }

    private fun startGame2(mode: String) {
        val intent = Intent(requireContext(), Game2PrestartActivity::class.java)
        intent.putExtra("GAME_MODE", mode)
        startActivity(intent)
    }

    private fun startGame3(mode: String) {
        val intent = Intent(requireContext(), Game3PrestartActivity::class.java)
        intent.putExtra("GAME_MODE", mode)
        startActivity(intent)
    }
}