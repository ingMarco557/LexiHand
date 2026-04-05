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

    // Usamos "UserCache" para que coincida con lo que lee ProfileActivity
    private val sharedPrefs = context.getSharedPreferences("UserCache", Context.MODE_PRIVATE)

    fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    /**
     * REGISTRO: Crea el usuario en Auth y luego usa su UID para crear el perfil en Firestore
     */
    fun registerUserCloud(email: String, pass: String, datos: Map<String, Any>, onResult: (Boolean, String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                // AQUÍ OBTENEMOS EL UID AUTOMÁTICAMENTE
                val uid = authResult.user?.uid ?: ""

                val perfilCompleto = datos.toMutableMap()
                if (!perfilCompleto.containsKey("nivel")) {
                    perfilCompleto["nivel"] = "Principiante"
                }

                // Guardamos en Firestore usando el UID como nombre del documento
                db.collection("usuarios").document(uid)
                    .set(perfilCompleto)
                    .addOnSuccessListener {
                        // Guardamos en caché local para que el Perfil lo lea al instante
                        guardarSesionLocal(
                            email,
                            pass,
                            perfilCompleto["nombre"].toString(),
                            perfilCompleto["nivel"].toString(),
                            perfilCompleto["edad"].toString(),
                            perfilCompleto["mano"].toString()
                        )
                        onResult(true, "Cuenta creada con éxito")
                    }
                    .addOnFailureListener {
                        // Si falla Firestore, al menos tenemos los datos locales
                        guardarSesionLocal(email, pass, datos["nombre"].toString(), "Principiante", datos["edad"].toString(), datos["mano"].toString())
                        onResult(true, "Registrado (Datos guardados localmente)")
                    }
            }
            .addOnFailureListener { e ->
                val errorMsg = when (e) {
                    is FirebaseAuthUserCollisionException -> "Este correo ya está registrado."
                    is FirebaseAuthWeakPasswordException -> "La contraseña es muy corta (mínimo 6)."
                    is FirebaseAuthInvalidCredentialsException -> "El formato del correo es inválido."
                    else -> "Error: ${e.localizedMessage}"
                }
                onResult(false, errorMsg)
            }
    }

    /**
     * LOGIN: Entra y descarga los datos del UID correspondiente
     */
    fun loginUser(email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        if (isOnline()) {
            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid ?: ""

                    // Buscamos el documento que se llama igual que el UID
                    db.collection("usuarios").document(uid).get()
                        .addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                guardarSesionLocal(
                                    email,
                                    pass,
                                    doc.getString("nombre") ?: "Usuario",
                                    doc.getString("nivel") ?: "Principiante",
                                    doc.getString("edad") ?: "--",
                                    doc.getString("mano") ?: "--"
                                )
                                onResult(true, "¡Bienvenido de nuevo!")
                            } else {
                                onResult(true, "Sesión iniciada (Perfil no encontrado)")
                            }
                        }
                        .addOnFailureListener {
                            onResult(true, "Sesión iniciada (Modo offline)")
                        }
                }
                .addOnFailureListener { e ->
                    when (e) {
                        is FirebaseAuthInvalidUserException -> onResult(false, "El usuario no existe.")
                        is FirebaseAuthInvalidCredentialsException -> onResult(false, "Contraseña incorrecta.")
                        else -> validarLoginLocal(email, pass, onResult)
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
            onResult(false, "No hay datos locales. Conéctate a internet una vez.")
        } else if (email == savedUser && pass == savedPass) {
            onResult(true, "Acceso local concedido")
        } else {
            onResult(false, "Credenciales incorrectas offline.")
        }
    }

    private fun guardarSesionLocal(email: String, pass: String, nombre: String, nivel: String, edad: String, mano: String) {
        val editor = sharedPrefs.edit()
        editor.putString("email_local", email)
        editor.putString("pass_local", pass)
        editor.putString("nombre", nombre)
        editor.putString("nivel", nivel)
        editor.putString("edad", edad)
        editor.putString("mano", mano)
        editor.putBoolean("esta_logueado", true)
        editor.apply()
    }

    fun verificarSesionActiva(): Boolean {
        return sharedPrefs.getBoolean("esta_logueado", false)
    }

    fun cerrarSesionLocal() {
        auth.signOut()
        sharedPrefs.edit().clear().apply()
    }
}