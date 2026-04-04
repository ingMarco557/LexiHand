package com.example.lexihand

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore

class AuthManager(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val sharedPrefs = context.getSharedPreferences("LexiHandLocalDB", Context.MODE_PRIVATE)

    fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    /**
     * REGISTRO CON VALIDACIONES ESPECÍFICAS
     */
    fun registerUserCloud(email: String, pass: String, datos: Map<String, Any>, onResult: (Boolean, String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""

                db.collection("users").document(uid)
                    .set(datos)
                    .addOnSuccessListener {
                        guardarSesionLocal(email, pass, datos["nombre"].toString(), datos["rol"].toString())
                        onResult(true, "Cuenta creada y sincronizada")
                    }
                    .addOnFailureListener {
                        guardarSesionLocal(email, pass, datos["nombre"].toString(), datos["rol"].toString())
                        marcarSincronizacionPendiente(true)
                        onResult(true, "Registrado localmente (Error de red al subir perfil)")
                    }
            }
            .addOnFailureListener { e ->
                // --- VALIDACIÓN DE ERRORES DE REGISTRO ---
                val errorMsg = when (e) {
                    is FirebaseAuthUserCollisionException -> "Este correo ya está registrado. Intenta iniciar sesión."
                    is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil. Usa al menos 6 caracteres."
                    is FirebaseAuthInvalidCredentialsException -> "El formato del correo no es válido."
                    else -> "Error en el registro: ${e.localizedMessage}"
                }
                onResult(false, errorMsg)
            }
    }

    /**
     * LOGIN CON VALIDACIONES ESPECÍFICAS
     */
    fun loginUser(email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        if (isOnline()) {
            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                    val rol = if (email.endsWith("@lexihand.dev")) "admin" else "user"
                    guardarSesionLocal(email, pass, email.split("@")[0], rol)
                    onResult(true, "Sesión iniciada (En línea)")
                }
                .addOnFailureListener { e ->
                    // --- VALIDACIÓN DE ERRORES DE LOGIN ONLINE ---
                    when (e) {
                        is FirebaseAuthInvalidUserException -> {
                            onResult(false, "No existe ninguna cuenta con este correo.")
                        }
                        is FirebaseAuthInvalidCredentialsException -> {
                            onResult(false, "Contraseña incorrecta.")
                        }
                        else -> {
                            // Si falla por otra cosa (como red), intentamos local
                            validarLoginLocal(email, pass, onResult)
                        }
                    }
                }
        } else {
            validarLoginLocal(email, pass, onResult)
        }
    }

    private fun validarLoginLocal(email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        val savedUser = sharedPrefs.getString("email_local", null)
        val savedPass = sharedPrefs.getString("pass_local", null)

        if (savedUser == null) {
            onResult(false, "No hay datos locales. Conéctate a internet para el primer inicio.")
        } else if (email == savedUser && pass == savedPass) {
            onResult(true, "Acceso concedido (Modo Offline)")
        } else {
            onResult(false, "Credenciales incorrectas (Modo Offline).")
        }
    }

    private fun guardarSesionLocal(email: String, pass: String, nombre: String, rol: String) {
        val editor = sharedPrefs.edit()
        editor.putString("email_local", email)
        editor.putString("pass_local", pass)
        editor.putString("nombre_local", nombre)
        editor.putString("rol_local", rol)
        editor.putBoolean("esta_logueado", true)
        editor.apply()
    }

    fun verificarSesionActiva(): Boolean {
        return sharedPrefs.getBoolean("esta_logueado", false)
    }

    private fun marcarSincronizacionPendiente(pendiente: Boolean) {
        sharedPrefs.edit().putBoolean("SYNC_PENDING", pendiente).apply()
    }

    fun cerrarSesion() {
        auth.signOut()
        sharedPrefs.edit().clear().apply()
    }
}