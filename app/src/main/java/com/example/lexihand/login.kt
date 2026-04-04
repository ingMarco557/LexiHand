package com.example.lexihand

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class login : AppCompatActivity() {

    private lateinit var authManager: AuthManager
    private var isLoginMode = true // true = Inicio de sesión, false = Registro

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inicializar el Manager
        authManager = AuthManager(this)

        // 2. VERIFICACIÓN DE SESIÓN AUTOMÁTICA
        // Si el usuario ya está logueado, saltamos el login.
        if (authManager.verificarSesionActiva()) {
            irAlMenuPrincipal()
            return
        }

        // 3. Cargar la interfaz
        setContentView(R.layout.activity_login)

        // Referencias a los elementos visuales
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val etUser = findViewById<EditText>(R.id.etUser)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val layoutRegistroExtra = findViewById<LinearLayout>(R.id.layoutRegistroExtra)
        val etEdad = findViewById<EditText>(R.id.etEdad)
        val rgMano = findViewById<RadioGroup>(R.id.rgMano)
        val rgNivel = findViewById<RadioGroup>(R.id.rgNivel)
        val btnLogin = findViewById<AppCompatButton>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        // Lógica para cambiar entre Iniciar Sesión y Registrarse
        tvRegister.setOnClickListener {
            isLoginMode = !isLoginMode

            if (isLoginMode) {
                tvTitle.text = "Inicio de sesión"
                layoutRegistroExtra.visibility = View.GONE
                btnLogin.text = "Iniciar sesión"
                tvRegister.text = "¿No tienes cuenta? Regístrate"
            } else {
                tvTitle.text = "Crear Cuenta"
                layoutRegistroExtra.visibility = View.VISIBLE
                btnLogin.text = "Registrarse"
                tvRegister.text = "¿Ya tienes cuenta? Inicia sesión"
            }
        }

        // Lógica del Botón Principal
        btnLogin.setOnClickListener {
            val user = etUser.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            // Validación básica de campos comunes
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Por favor, completa usuario y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isLoginMode) {
                // --- PROCESO DE INICIO DE SESIÓN ---
                authManager.loginUser(user, pass) { exito, mensaje ->
                    if (exito) {
                        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                        irAlMenuPrincipal()
                    } else {
                        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                    }
                }

            } else {
                // --- PROCESO DE REGISTRO ---
                val edad = etEdad.text.toString().trim()

                // Validación de campos extra de registro
                if (edad.isEmpty()) {
                    Toast.makeText(this, "La edad es obligatoria", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Validación de RadioButtons (Evita que la app truene si no hay nada marcado)
                val seleccionadoManoId = rgMano.checkedRadioButtonId
                val seleccionadoNivelId = rgNivel.checkedRadioButtonId

                if (seleccionadoManoId == -1 || seleccionadoNivelId == -1) {
                    Toast.makeText(this, "Selecciona mano y nivel de experiencia", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val mano = findViewById<RadioButton>(seleccionadoManoId).text.toString()
                val nivel = findViewById<RadioButton>(seleccionadoNivelId).text.toString()

                // Preparar datos para Firestore
                val datosUsuario = hashMapOf(
                    "nombre" to user.split("@")[0],
                    "edad" to edad,
                    "mano" to mano,
                    "nivel" to nivel,
                    "rol" to if (user.endsWith("@lexihand.dev")) "admin" else "user"
                )

                // Llamada al registro en la nube
                authManager.registerUserCloud(user, pass, datosUsuario) { exito, mensaje ->
                    if (exito) {
                        Toast.makeText(this, "¡Bienvenido a LexiHand!", Toast.LENGTH_SHORT).show()
                        irAlMenuPrincipal()
                    } else {
                        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun irAlMenuPrincipal() {
        val intent = Intent(this, MainActivity::class.java)
        // Bandera de seguridad: limpia el historial de actividades para que no se pueda volver al login con "atrás"
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}