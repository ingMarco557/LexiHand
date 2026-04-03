package com.example.lexihand

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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

    // --- NUEVO: Antena para escuchar a la IA y pasarlo al Fragmento ---
    private val iaReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "GUANTE_AI_DATA") {
                val letra = intent.getStringExtra("letra") ?: "--"

                // Buscamos el fragmento del Traductor (ViewPager2 usa f0, f1, f2...)
                val fragment = supportFragmentManager.findFragmentByTag("f1")
                if (fragment is TraductorFragment) {
                    fragment.onGestoDetectado(letra)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewPager = findViewById(R.id.viewPager)
        bottomNav = findViewById(R.id.bottomNav)
        viewPager.adapter = ViewPagerAdapter(this)

        inicializarEstadoUI()

        guanteManager = GuanteManager(this, this)

        setupViewPagerListener()
        setupBottomNavListener()

        // Solicitar todos los permisos necesarios
        verificarPermisosSolo()
    }

    // Registramos la antena cuando la app está en pantalla
    override fun onResume() {
        super.onResume()
        val filter = IntentFilter("GUANTE_AI_DATA")
        ContextCompat.registerReceiver(this, iaReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    // Apagamos la antena si la app se minimiza para ahorrar batería
    override fun onPause() {
        super.onPause()
        try {
            unregisterReceiver(iaReceiver)
        } catch (e: Exception) {
            // Ignorar si no estaba registrado
        }
    }

    private fun inicializarEstadoUI() {
        onStatusChanged(false, 0)
    }

    fun iniciarVinculacion() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter

        if (adapter == null || !adapter.isEnabled) {
            Toast.makeText(this, "Enciende el Bluetooth para conectar el guante", Toast.LENGTH_LONG).show()
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBtIntent)
            return
        }

        runOnUiThread {
            val txtStatus = findViewById<TextView>(R.id.txtBLEStatus)
            txtStatus?.text = "Buscando guante..."
            txtStatus?.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        }

        guanteManager.connect()
    }

    private fun verificarPermisosSolo() {
        val permisos = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permisos.add(Manifest.permission.BLUETOOTH_SCAN)
            permisos.add(Manifest.permission.BLUETOOTH_CONNECT)
            permisos.add(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            permisos.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val faltanPermisos = permisos.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (faltanPermisos.isNotEmpty()) {
            requestPermissionLauncher.launch(faltanPermisos.toTypedArray())
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (!results.all { it.value }) {
            Toast.makeText(this, "Acepta los permisos de Ubicación y Dispositivos Cercanos", Toast.LENGTH_LONG).show()
        }
    }

    // Único método requerido por nuestra nueva versión de GuanteListener
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
                statusDot?.setBackgroundResource(R.drawable.shape_circle_red)
                txtStatus?.text = "Guante no conectado"
                txtStatus?.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
                txtBattery?.text = "🔋 --%"
            }
        }
    }

    // --- NUEVO: Función para abrir el panel de Diagnóstico Mecatrónico ---
    // Llama a esta función desde el botón que tú quieras (ej. en tu MenuFragment)
    fun abrirPanelDiagnostico() {
        val intent = Intent(this, DiagnosticsActivity::class.java)
        startActivity(intent)
    }

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