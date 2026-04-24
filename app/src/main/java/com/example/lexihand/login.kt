package com.example.lexihand

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputLayout
import android.graphics.Color
import androidx.core.view.WindowCompat

class login : AppCompatActivity() {

    private lateinit var authManager: AuthManager
    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authManager = AuthManager(this)

        if (authManager.verificarSesionActiva()) {
            irAlMenuPrincipal()
            return
        }


        WindowCompat.setDecorFitsSystemWindows(window, false)
        // 2. Pinta la barra de estado de transparente
        window.statusBarColor = Color.TRANSPARENT

        setContentView(R.layout.activity_login)

        // Referencias de Contenedores (Para mostrar errores)
        val tilEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)
        val tilNombre = findViewById<TextInputLayout>(R.id.tilNombre)
        val tilEdad = findViewById<TextInputLayout>(R.id.tilEdad)

        // Referencias de Campos
        val etUser = findViewById<EditText>(R.id.etUser)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etEdad = findViewById<EditText>(R.id.etEdad)

        val layoutRegistroExtra = findViewById<LinearLayout>(R.id.layoutRegistroExtra)
        val rgMano = findViewById<RadioGroup>(R.id.rgMano)
        val cbTerms = findViewById<CheckBox>(R.id.cbTerms)
        val btnLogin = findViewById<AppCompatButton>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)

        tvRegister.setOnClickListener {
            isLoginMode = !isLoginMode
            limpiarErrores(tilEmail, tilPassword, tilNombre, tilEdad)

            if (isLoginMode) {
                tvTitle.text = "Bienvenido"
                layoutRegistroExtra.visibility = View.GONE
                btnLogin.text = "Iniciar sesión"
                tvRegister.text = "¿No tienes cuenta? Regístrate"
            } else {
                tvTitle.text = "Crea tu cuenta"
                layoutRegistroExtra.visibility = View.VISIBLE
                btnLogin.text = "Registrarse"
                tvRegister.text = "¿Ya tienes cuenta? Inicia sesión"
            }
        }

        btnLogin.setOnClickListener {
            val email = etUser.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            limpiarErrores(tilEmail, tilPassword, tilNombre, tilEdad)

            // --- VALIDACIONES COMUNES ---
            if (!validarEmail(email, tilEmail)) return@setOnClickListener
            if (!validarPassword(pass, tilPassword)) return@setOnClickListener

            if (isLoginMode) {
                authManager.loginUser(email, pass) { exito, mensaje ->
                    if (exito) irAlMenuPrincipal()
                    else Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                }
            } else {
                // --- VALIDACIONES DE REGISTRO ---
                val nombre = etNombre.text.toString().trim()
                val edad = etEdad.text.toString().trim()

                if (nombre.isEmpty()) {
                    tilNombre.error = "El nombre es obligatorio"
                    return@setOnClickListener
                }
                if (edad.isEmpty()) {
                    tilEdad.error = "La edad es obligatoria"
                    return@setOnClickListener
                }
                if (!cbTerms.isChecked) {
                    Toast.makeText(this, "Debes aceptar los términos legales", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val mano = if (findViewById<RadioButton>(rgMano.checkedRadioButtonId).text == "Derecha") "Derecha" else "Izquierda"

                val datosUsuario = hashMapOf(
                    "nombre" to nombre,
                    "email" to email,
                    "edad" to edad,
                    "mano" to mano,
                    "aceptacion_legal" to true
                )

                authManager.registerUserCloud(email, pass, datosUsuario) { exito, mensaje ->
                    if (exito) irAlMenuPrincipal()
                    else Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
                }
            }
        }



    }

    // FUNCIONES DE VALIDACIÓN
    private fun validarEmail(email: String, container: TextInputLayout): Boolean {
        return when {
            email.isEmpty() -> {
                container.error = "Escribe tu correo"
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                container.error = "Formato de correo inválido"
                false
            }
            else -> true
        }
    }

    private fun validarPassword(pass: String, container: TextInputLayout): Boolean {
        return when {
            pass.isEmpty() -> {
                container.error = "Escribe tu contraseña"
                false
            }
            pass.length < 6 -> {
                container.error = "Mínimo 6 caracteres"
                false
            }
            else -> true
        }
    }

    private fun limpiarErrores(vararg containers: TextInputLayout) {
        for (c in containers) c.error = null
    }

    private fun irAlMenuPrincipal() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}