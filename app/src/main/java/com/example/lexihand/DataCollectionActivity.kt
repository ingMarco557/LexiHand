package com.example.lexihand

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.*

class DataCollectionActivity : AppCompatActivity() {

    // --- VARIABLES DE DATOS ---
    private var isRecording = false
    private var samplesCollected = 0
    private val maxSamples = 100
    private val muestrasLista = mutableListOf<String>()
    private val userId = "usuario_prueba_1"

    // --- FIREBASE ---
    private val database = FirebaseDatabase.getInstance("https://lexihand-bfe1a-default-rtdb.firebaseio.com/").reference

    // --- UI ---
    private lateinit var spinnerGestures: Spinner
    private lateinit var layoutCustomGesture: TextInputLayout
    private lateinit var etCustomGesture: EditText
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var txtSamples: TextView
    private lateinit var btnStartStop: Button
    private lateinit var txtRealTimeData: TextView
    private lateinit var txtGestosNube: TextView
    private lateinit var btnDownloadModel: Button
    private lateinit var btnClear: Button

    // --- RECEIVER (Escucha al Service del Bluetooth) ---
    private val dataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "GUANTE_RAW_DATA") {
                val rawData = intent.getStringExtra("data") ?: ""

                // Mostrar en pantalla (S1:0.5, S2:0.1...)
                txtRealTimeData.text = rawData.replace(",", "\n")

                if (isRecording && samplesCollected < maxSamples) {
                    // Limpiar el texto para dejar solo los números (0.5, 0.1...)
                    val soloNumeros = rawData.replace(Regex("S\\d:"), "")
                    muestrasLista.add(soloNumeros)

                    samplesCollected++
                    actualizarUI()

                    if (samplesCollected >= maxSamples) {
                        stopRecording()
                        Toast.makeText(this@DataCollectionActivity, "¡Muestras capturadas!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_collection)

        vincularVistas()
        configurarSpinner()
        escucharNube()

        // BOTÓN: GRABAR / DETENER
        btnStartStop.setOnClickListener {
            if (!isRecording) startRecording() else stopRecording()
        }

        // BOTÓN: SUBIR A FIREBASE
        findViewById<Button>(R.id.btnExport).setOnClickListener {
            subirAFirebase()
        }

        // BOTÓN: LIMPIAR MANUALMENTE
        btnClear.setOnClickListener {
            limpiarMemoriaLocal()
            Toast.makeText(this, "Caché de muestras borrada", Toast.LENGTH_SHORT).show()
        }

        // BOTÓN: DESCARGAR (Simulacro)
        btnDownloadModel.setOnClickListener {
            Toast.makeText(this, "El modelo se genera en tu PC (trainer.py)", Toast.LENGTH_LONG).show()
        }
    }

    private fun vincularVistas() {
        spinnerGestures = findViewById(R.id.spinnerGestures)
        layoutCustomGesture = findViewById(R.id.layoutCustomGesture)
        etCustomGesture = findViewById(R.id.etCustomGesture)
        progressBar = findViewById(R.id.progressSamples)
        txtSamples = findViewById(R.id.txtSampleCount)
        btnStartStop = findViewById(R.id.btnStartStop)
        txtRealTimeData = findViewById(R.id.txtRealTimeData)
        txtGestosNube = findViewById(R.id.txtGestosNube)
        btnDownloadModel = findViewById(R.id.btnDownloadModel)
        btnClear = findViewById(R.id.btnClear)
    }

    private fun startRecording() {
        limpiarMemoriaLocal() // Empezamos desde cero
        isRecording = true
        btnStartStop.text = "Detener"
        btnStartStop.backgroundTintList = getColorStateList(android.R.color.black)
    }

    private fun stopRecording() {
        isRecording = false
        btnStartStop.text = "Grabar"
        btnStartStop.backgroundTintList = getColorStateList(android.R.color.holo_green_dark)
    }

    private fun limpiarMemoriaLocal() {
        samplesCollected = 0
        muestrasLista.clear()
        actualizarUI()
    }

    private fun actualizarUI() {
        progressBar.progress = samplesCollected
        txtSamples.text = "Muestras: $samplesCollected / $maxSamples"
    }

    private fun subirAFirebase() {
        if (muestrasLista.size < maxSamples) {
            Toast.makeText(this, "Captura al menos $maxSamples muestras primero", Toast.LENGTH_SHORT).show()
            return
        }

        val nombreGesto = obtenerNombreGesto()
        val ref = database.child("Proyectos").child(userId).child("DatosRecoleccion").child(nombreGesto)

        Toast.makeText(this, "Subiendo $nombreGesto...", Toast.LENGTH_SHORT).show()

        ref.setValue(muestrasLista).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Notificar al script de Python
                database.child("Proyectos").child(userId).child("EstadoEntrenamiento").setValue("SOLICITADO")

                Toast.makeText(this, "¡Subida exitosa! Memoria local limpia.", Toast.LENGTH_SHORT).show()

                // AUTO-LIMPIEZA: Una vez subido, borramos para el siguiente gesto
                limpiarMemoriaLocal()
            } else {
                Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun obtenerNombreGesto(): String {
        val seleccionado = spinnerGestures.selectedItem.toString()
        return if (seleccionado == "Personalizar...") {
            etCustomGesture.text.toString().trim().ifEmpty { "GestoX" }
        } else {
            seleccionado
        }
    }

    private fun escucharNube() {
        // 1. Escuchar los gestos que ya existen
        database.child("Proyectos").child(userId).child("DatosRecoleccion")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombres = snapshot.children.mapNotNull { it.key }
                    txtGestosNube.text = if (nombres.isEmpty()) "Sin datos" else "En la nube: " + nombres.joinToString(" • ")
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        // 2. Escuchar el estado de la IA (Para saber si ya terminó Python)
        database.child("Proyectos").child(userId).child("EstadoEntrenamiento")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val estado = snapshot.getValue(String::class.java)
                    if (estado == "SOLICITADO") {
                        btnDownloadModel.text = "IA Entrenando... Espere"
                        btnDownloadModel.isEnabled = false
                    } else if (estado == "LISTO") {
                        btnDownloadModel.text = "¡Modelo Listo! Descargar"
                        btnDownloadModel.isEnabled = true
                        btnDownloadModel.backgroundTintList = getColorStateList(android.R.color.holo_orange_light)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // --- CICLO DE VIDA (Bluetooth) ---
    override fun onResume() {
        super.onResume()
        ContextCompat.registerReceiver(this, dataReceiver, IntentFilter("GUANTE_RAW_DATA"), ContextCompat.RECEIVER_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(dataReceiver)
    }

    private fun configurarSpinner() {
        val opciones = arrayOf("Reposo", "A", "B", "Hola", "Gracias", "Personalizar...")
        spinnerGestures.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opciones)
        spinnerGestures.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                layoutCustomGesture.visibility = if (opciones[pos] == "Personalizar...") View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }
}