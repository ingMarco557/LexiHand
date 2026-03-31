package com.example.lexihand

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), GuanteManager.GuanteListener {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var guanteManager: GuanteManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 1. Configurar Edge-to-Edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 2. Inicializar Vistas
        viewPager = findViewById(R.id.viewPager)
        bottomNav = findViewById(R.id.bottomNav)
        viewPager.adapter = ViewPagerAdapter(this)

        // 3. ESTADO INICIAL: Forzamos a que aparezca desconectado antes de cualquier cosa
        inicializarEstadoUI()

        // 4. Inicializar Motor del Guante e IA
        guanteManager = GuanteManager(this, this)

        // 5. Configurar Navegación
        setupViewPagerListener()
        setupBottomNavListener()

        // 6. Verificar Permisos y Conectar
        verificarPermisosYConectar()
    }

    private fun inicializarEstadoUI() {
        // Ejecutamos la lógica de desconexión manualmente para pintar la UI de rojo al inicio
        onStatusChanged(false, 0)
    }

    // --- LÓGICA DE PERMISOS ---
    private fun verificarPermisosYConectar() {
        val permisos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val faltanPermisos = permisos.any {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (faltanPermisos) {
            requestPermissionLauncher.launch(permisos)
        } else {
            guanteManager.connect()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.all { it.value }) {
            guanteManager.connect()
        } else {
            onStatusChanged(false, 0)
            Toast.makeText(this, "Se requieren permisos para el guante", Toast.LENGTH_SHORT).show()
        }
    }

    // --- IMPLEMENTACIÓN DE GUANTE LISTENER ---
    // Este método es el que asegura que si el guante se apaga, la UI cambie a rojo
    override fun onStatusChanged(conectado: Boolean, bateria: Int) {
        runOnUiThread {
            val statusDot = findViewById<View>(R.id.statusDot)
            val txtStatus = findViewById<TextView>(R.id.txtBLEStatus)
            val txtBattery = findViewById<TextView>(R.id.txtBattery)

            if (conectado) {
                statusDot?.setBackgroundResource(R.drawable.shape_circle_green)
                txtStatus?.text = "🔗 Guante Conectado"
                txtStatus?.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
                txtBattery?.text = "🔋 $bateria%"
            } else {
                // AQUÍ SE APLICA TU REQUERIMIENTO: "Guante no conectado"
                statusDot?.setBackgroundResource(R.drawable.shape_circle_red)
                txtStatus?.text = "Guante no conectado"
                txtStatus?.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
                txtBattery?.text = "🔋 --%"
            }
        }
    }

    override fun onLetraDetectada(letra: String) {
        runOnUiThread {
            val fragment = supportFragmentManager.findFragmentByTag("f1")
            if (fragment is TraductorFragment) {
                fragment.onGestoDetectado(letra)
            }
        }
    }

    override fun onRawDataReceived(data: String) {
        // Opcional: Logcat para debug
    }

    // --- CONFIGURACIÓN DE UI ---
    private fun setupViewPagerListener() {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                bottomNav.menu.getItem(position).isChecked = true
            }
        })
    }

    private fun setupBottomNavListener() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> viewPager.currentItem = 0
                R.id.traductorFragment -> viewPager.currentItem = 1
                R.id.juegosFragment -> viewPager.currentItem = 2
                R.id.perfilFragment -> viewPager.currentItem = 3
                else -> return@setOnItemSelectedListener false
            }
            true
        }
    }

    private inner class ViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 4
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> HomeFragment()
                1 -> TraductorFragment()
                2 -> JuegosFragment()
                3 -> MenuFragment()
                else -> HomeFragment()
            }
        }
    }
}