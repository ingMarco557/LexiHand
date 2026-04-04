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

        // 2. VERIFICACIÓN DE SESIÓN AUTOMÁTICA (MODO OFFLINE/ONLINE)
        // Si ya existe una sesión guardada en el teléfono, saltamos directo al Main
        if (authManager.verificarSesionActiva()) {
            irAlMenuPrincipal()
            return // Detiene la ejecución para no cargar el layout de login innecesariamente
        }

        // 3. Si no hay sesión, cargamos la interfaz
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
                // MODO: INICIAR SESIÓN
                tvTitle.text = "Inicio de sesión"
                layoutRegistroExtra.visibility = View.GONE
                btnLogin.text = "Iniciar sesión"
                tvRegister.text = "¿No tienes cuenta? Regístrate"
            } else {
                // MODO: REGISTRO
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

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Completa usuario y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isLoginMode) {
                // --- PROCESO DE INICIO DE SESIÓN (Híbrido Online/Offline) ---
                authManager.loginUser(user, pass) { exito, mensaje ->
                    if (exito) {
                        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
                        irAlMenuPrincipal()
                    } else {
                        // Aquí el mensaje dirá si la cuenta no existe localmente
                        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                    }
                }

            } else {
                // --- PROCESO DE REGISTRO (Firebase + Local) ---
                val edad = etEdad.text.toString().trim()
                if (edad.isEmpty()) {
                    Toast.makeText(this, "La edad es obligatoria para calibrar el guante", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Obtener selecciones de los RadioButtons
                val seleccionadoManoId = rgMano.checkedRadioButtonId
                val seleccionadoNivelId = rgNivel.checkedRadioButtonId

                val mano = findViewById<RadioButton>(seleccionadoManoId).text.toString()
                val nivel = findViewById<RadioButton>(seleccionadoNivelId).text.toString()

                // Preparar datos para Firestore
                val datosUsuario = hashMapOf(
                    "nombre" to user.split("@")[0], // Nombre base
                    "edad" to edad,
                    "mano" to mano,
                    "nivel" to nivel,
                    "rol" to if (user.endsWith("@lexihand.dev")) "admin" else "user"
                )

                // Llamada al registro en la nube con persistencia local
                authManager.registerUserCloud(user, pass, datosUsuario) { exito, mensaje ->
                    if (exito) {
                        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                        irAlMenuPrincipal()
                    } else {
                        Toast.makeText(this, "Error: $mensaje", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun irAlMenuPrincipal() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Elimina esta actividad de la pila para que no regresen con el botón "Atrás"
    }
}