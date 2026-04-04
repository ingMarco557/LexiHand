package com.example.lexihand

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupToolbar()
        loadUserProfile()

        // Botón Re-vincular
        findViewById<Button>(R.id.btnRevinculate).setOnClickListener {
            reVinculateDevice()
        }

        // Botón Cerrar Sesión
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            cerrarSesion()
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarProfile)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            // Buscamos el documento en la colección "usuarios" que creamos en el Login
            db.collection("usuarios").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Extraemos los datos exactos del mapa que guardamos
                        val nombre = document.getString("nombre") ?: "Usuario"
                        val nivel = document.getString("nivel") ?: "Principiante"
                        val edad = document.getString("edad") ?: "--"
                        val mano = document.getString("mano") ?: "--"

                        // Pintamos en la UI
                        findViewById<TextView>(R.id.txtUserName).text = nombre
                        findViewById<TextView>(R.id.txtUserLevel).text = "Nivel: $nivel"
                        findViewById<TextView>(R.id.txtUserAge).text = "Edad: $edad años"
                        findViewById<TextView>(R.id.txtUserHand).text = "Mano: $mano"
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun cerrarSesion() {
        // 1. Cerrar sesión en Firebase
        auth.signOut()

        // 2. Limpiar preferencias si usas AuthManager manual
        val authManager = AuthManager(this)
        authManager.cerrarSesionLocal()

        // 3. Regresar al Login y borrar el historial de pantallas
        val intent = Intent(this, login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun reVinculateDevice() {
        // Aquí puedes redirigir a una pantalla de escaneo o llamar al MainActivity
        Toast.makeText(this, "Buscando guante LexiHand...", Toast.LENGTH_SHORT).show()
    }
}