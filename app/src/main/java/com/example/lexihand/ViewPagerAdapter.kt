package com.example.lexihand

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Adaptador que maneja los 4 fragmentos principales
 * Cada fragmento se carga cuando se necesita
 */
class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    // Lista de fragmentos (en el mismo orden del menú inferior)
    private val fragments = listOf(
        HomeFragment(),           // Índice 0: Inicio
        TraductorFragment(),      // Índice 1: Traductor
        JuegosFragment(),         // Índice 2: Juegos
        MenuFragment()          // Índice 3: Perfil
    )

    /**
     * Retorna el número total de fragmentos
     */
    override fun getItemCount(): Int = fragments.size

    /**
     * Crea el fragmento en la posición indicada
     */
    override fun createFragment(position: Int): Fragment = fragments[position]
}