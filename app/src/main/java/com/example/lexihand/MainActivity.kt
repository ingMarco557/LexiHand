package com.example.lexihand

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewPager = findViewById(R.id.viewPager)
        bottomNav = findViewById(R.id.bottomNav)
        viewPager.adapter = ViewPagerAdapter(this)
        viewPager.offscreenPageLimit = 3

        guanteManager = GuanteManager(this, this)

        setupViewPagerListener()
        setupBottomNavListener()

        // Verificación inicial de Bluetooth
        verificarPermisosSolo()
    }

    fun iniciarVinculacion() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = bluetoothManager.adapter

        if (adapter == null) {
            Toast.makeText(this, "El dispositivo no soporta Bluetooth", Toast.LENGTH_LONG).show()
            return
        }

        if (!adapter.isEnabled) {
            Toast.makeText(this, "Por favor, enciende el Bluetooth", Toast.LENGTH_LONG).show()
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            //startActivity(enableBtIntent)
            return
        }

        findViewById<TextView>(R.id.txtBLEStatus)?.text = "Buscando guante..."
        guanteManager.connect()
    }

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

    private fun verificarPermisosSolo() {
        val permisos = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permisos.add(Manifest.permission.BLUETOOTH_SCAN)
            permisos.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permisos.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val faltanPermisos = permisos.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (faltanPermisos.isNotEmpty()) {
            requestPermissionLauncher.launch(faltanPermisos)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (!results.all { it.value }) {
            Toast.makeText(this, "Se requieren permisos para el guante", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupViewPagerListener() {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
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