package com.example.lexihand

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    // Variables para las vistas principales
    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Configurar edge-to-edge (barras de estado transparentes)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // PASO 1: Inicializar vistas del layout
        viewPager = findViewById(R.id.viewPager)
        bottomNav = findViewById(R.id.bottomNav)

        // PASO 2: Asignar adaptador al ViewPager
        viewPager.adapter = ViewPagerAdapter(this)

        // PASO 3: Permitir deslizamiento suave
        viewPager.isUserInputEnabled = true  // Cambiar a false si quieres SOLO botones

        // PASO 4: Sincronizar ViewPager con BottomNav
        setupViewPagerListener()
        setupBottomNavListener()
    }

    /**
     * SINCRONIZACIÓN 1: Cuando el usuario DESLIZA (swipe)
     * El BottomNav se actualiza automáticamente
     */
    private fun setupViewPagerListener() {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            /**
             * Se llama cuando se selecciona una nueva página
             * @param position: Índice de la página (0, 1, 2, 3)
             */
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                // Actualizar el ítem seleccionado del BottomNav
                // Sin esta línea, el usuario desliza pero el botón no se marca
                when (position) {
                    0 -> bottomNav.selectedItemId = R.id.nav_home      // Swipe → Home
                    1 -> bottomNav.selectedItemId = R.id.nav_traductor // Swipe → Traductor
                    2 -> bottomNav.selectedItemId = R.id.nav_juegos    // Swipe → Juegos
                    3 -> bottomNav.selectedItemId = R.id.nav_perfil    // Swipe → Perfil
                }
            }

            /**
             * Opcional: Se llama mientras se desliza
             * Útil para animaciones suaves
             */
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                // Aquí puedes agregar lógica para animaciones
            }
        })
    }

    /**
     * SINCRONIZACIÓN 2: Cuando el usuario TOCA un botón del BottomNav
     * El ViewPager salta a ese fragmento
     */
    private fun setupBottomNavListener() {
        bottomNav.setOnItemSelectedListener { item ->
            // Cambiar la página según el ítem clickeado
            when (item.itemId) {
                R.id.nav_home -> viewPager.currentItem = 0       // Botón → Home
                R.id.nav_traductor -> viewPager.currentItem = 1  // Botón → Traductor
                R.id.nav_juegos -> viewPager.currentItem = 2     // Botón → Juegos
                R.id.nav_perfil -> viewPager.currentItem = 3     // Botón → Perfil
                else -> return@setOnItemSelectedListener false
            }
            // Retornar true indica que se procesó la selección
            true
        }
    }
}