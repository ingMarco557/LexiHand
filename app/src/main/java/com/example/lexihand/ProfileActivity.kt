package com.example.lexihand

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Referencias de UI
    private lateinit var txtUserName: TextView
    private lateinit var txtUserLevel: TextView
    private lateinit var txtUserAge: TextView
    private lateinit var txtUserHand: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inicializar vistas
        txtUserName = findViewById(R.id.txtUserName)
        txtUserLevel = findViewById(R.id.txtUserLevel)
        txtUserAge = findViewById(R.id.txtUserAge)
        txtUserHand = findViewById(R.id.txtUserHand)

        // setupToolbar()

        // 1. Cargar datos desde la Caché local (Instantáneo)
        cargarDatosDesdeCache()

        // 2. Cargar datos desde Firebase (Actualización en segundo plano)
        loadUserProfileFromFirebase()

        findViewById<Button>(R.id.btnRevinculate).setOnClickListener {
            reVinculateDevice()
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            cerrarSesion()
        }
    }

   /* private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarProfile)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }*/

    private fun cargarDatosDesdeCache() {
        val prefs = getSharedPreferences("UserCache", Context.MODE_PRIVATE)

        // Leemos los datos. Si no existen, ponemos valores por defecto
        txtUserName.text = prefs.getString("nombre", "Usuario")
        txtUserLevel.text = "Nivel: ${prefs.getString("nivel", "--")}"
        txtUserAge.text = "Edad: ${prefs.getString("edad", "--")} años"
        txtUserHand.text = "Mano: ${prefs.getString("mano", "--")}"
    }

    private fun guardarDatosEnCache(nombre: String, nivel: String, edad: String, mano: String) {
        val prefs = getSharedPreferences("UserCache", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("nombre", nombre)
        editor.putString("nivel", nivel)
        editor.putString("edad", edad)
        editor.putString("mano", mano)
        editor.apply() // Guarda en segundo plano
    }

    private fun loadUserProfileFromFirebase() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val nombre = document.getString("nombre") ?: "Usuario"
                    val nivel = document.getString("nivel") ?: "Principiante"
                    val edad = document.getString("edad") ?: "--"
                    val mano = document.getString("mano") ?: "--"

                    // Actualizar UI
                    txtUserName.text = nombre
                    txtUserLevel.text = "Nivel: $nivel"
                    txtUserAge.text = "Edad: $edad años"
                    txtUserHand.text = "Mano: $mano"

                    // Guardar en Caché para la próxima vez
                    guardarDatosEnCache(nombre, nivel, edad, mano)
                }
            }
            .addOnFailureListener {
                // Si falla (por falta de internet), el usuario ya está viendo los datos de la caché
                Toast.makeText(this, "Mostrando datos locales (Sin conexión)", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cerrarSesion() {
        // 1. Limpiar Caché Local
        val prefs = getSharedPreferences("UserCache", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        // 2. Cerrar sesión en Firebase
        auth.signOut()

        // 3. Limpiar AuthManager
        val authManager = AuthManager(this)
        authManager.cerrarSesionLocal()

        val intent = Intent(this, login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun reVinculateDevice() {
        Toast.makeText(this, "Buscando guante LexiHand...", Toast.LENGTH_SHORT).show()
    }
}