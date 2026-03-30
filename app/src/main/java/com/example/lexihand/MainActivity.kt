package com.example.lexihand

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Configurar edge-to-edge
        val mainView = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Inicializar vistas
        viewPager = findViewById(R.id.viewPager)
        bottomNav = findViewById(R.id.bottomNav)

        // 2. Asignar el Adaptador (Clase definida abajo)
        viewPager.adapter = ViewPagerAdapter(this)

        // 3. Configurar Sincronización
        setupViewPagerListener()
        setupBottomNavListener()
    }

    private fun setupViewPagerListener() {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Sincroniza el ítem del BottomNav al deslizar
                bottomNav.menu.getItem(position).isChecked = true
            }
        })
    }

    private fun setupBottomNavListener() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> viewPager.currentItem = 0
                R.id.juegosFragment -> viewPager.currentItem = 2
                R.id.traductorFragment -> viewPager.currentItem = 1
                R.id.perfilFragment -> viewPager.currentItem = 3
                else -> return@setOnItemSelectedListener false
            }
            true
        }
    }

    /**
     * ADAPTADOR NECESARIO: Esta clase gestiona qué fragmento se muestra en cada posición.
     */
    private inner class ViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {

        // Número total de pestañas (Home, Juegos, Chat/Traductor, Menú)
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> HomeFragment()
                1 -> TraductorFragment()   // Módulo de gamificación
                2 -> JuegosFragment() // Tu chat bidireccional (Voz/Guante)
                3 -> MenuFragment()       // Ajustes y perfil
                else -> HomeFragment()
            }
        }
    }
}