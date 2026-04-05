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
import com.google.firebase.database.FirebaseDatabase

class DataCollectionActivity : AppCompatActivity() {

    // --- VARIABLES DE ESTADO Y DATOS ---
    private var isRecording = false
    private var samplesCollected = 0
    private val maxSamples = 100
    private val muestrasLista = mutableListOf<String>()

    // --- FIREBASE ---
    // Usamos la URL exacta de tu imagen para evitar errores de conexión
    private val database = FirebaseDatabase.getInstance("https://lexihand-bfe1a-default-rtdb.firebaseio.com/").reference

    // --- UI COMPONENTS ---
    private lateinit var spinnerGestures: Spinner
    private lateinit var layoutCustomGesture: TextInputLayout
    private lateinit var etCustomGesture: EditText
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var txtSamples: TextView
    private lateinit var btnStartStop: Button
    private lateinit var txtRealTimeData: TextView

    // --- RECEIVER DEL BLUETOOTH ---
    private val dataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "GUANTE_RAW_DATA") {
                // Recibimos el formato "S1:0.54,S2:0.12,S3:0.88,S4:0.11,S5:0.92"
                val rawData = intent.getStringExtra("data") ?: ""

                // 1. Mostrar en la pantalla en tiempo real
                txtRealTimeData.text = rawData.replace(",", "\n")

                // 2. Si estamos grabando, guardar en la lista
                if (isRecording && samplesCollected < maxSamples) {
                    // Limpiamos "S1:", "S2:", etc. para dejar solo "0.54,0.12..."
                    val soloNumeros = rawData.replace(Regex("S\\d:"), "")
                    muestrasLista.add(soloNumeros)

                    samplesCollected++
                    progressBar.progress = samplesCollected
                    txtSamples.text = "Muestras: $samplesCollected / $maxSamples"

                    // Si llegamos a 100, nos detenemos automáticamente
                    if (samplesCollected >= maxSamples) {
                        stopRecording()
                        Toast.makeText(this@DataCollectionActivity, "¡Recolección completada!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_collection)

        // Vincular Vistas
        spinnerGestures = findViewById(R.id.spinnerGestures)
        layoutCustomGesture = findViewById(R.id.layoutCustomGesture)
        etCustomGesture = findViewById(R.id.etCustomGesture)
        progressBar = findViewById(R.id.progressSamples)
        txtSamples = findViewById(R.id.txtSampleCount)
        btnStartStop = findViewById(R.id.btnStartStop)
        txtRealTimeData = findViewById(R.id.txtRealTimeData) // Nueva vista vinculada

        setupToolbar()
        setupGestureSelector()

        // LISTENERS DE BOTONES
        btnStartStop.setOnClickListener {
            if (!isRecording) startRecording() else stopRecording()
        }

        findViewById<Button>(R.id.btnExport).setOnClickListener {
            exportData()
        }

        findViewById<Button>(R.id.btnValidate).setOnClickListener {
            validateData()
        }

        findViewById<Button>(R.id.btnClear).setOnClickListener {
            clearData()
        }
    }

    override fun onResume() {
        super.onResume()
        // Empezamos a escuchar al guante
        val filter = IntentFilter("GUANTE_RAW_DATA")
        ContextCompat.registerReceiver(this, dataReceiver, filter, ContextCompat.RECEIVER_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        // Dejamos de escuchar si salimos de la pantalla para ahorrar batería
        unregisterReceiver(dataReceiver)
    }

    private fun setupToolbar() {
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarCollection).setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupGestureSelector() {
        val gestures = arrayOf("A", "B", "Hola", "Adiós", "Gracias", "Personalizar...")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, gestures)
        spinnerGestures.adapter = adapter

        spinnerGestures.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if (gestures[position] == "Personalizar...") {
                    layoutCustomGesture.visibility = View.VISIBLE
                    etCustomGesture.requestFocus()
                } else {
                    layoutCustomGesture.visibility = View.GONE
                    etCustomGesture.text?.clear()
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun startRecording() {
        isRecording = true
        btnStartStop.text = "Detener"
        btnStartStop.backgroundTintList = getColorStateList(android.R.color.black)
        Toast.makeText(this, "Capturando datos de: ${getTargetName()}", Toast.LENGTH_SHORT).show()
    }

    private fun stopRecording() {
        isRecording = false
        btnStartStop.text = "Comenzar"
        btnStartStop.backgroundTintList = getColorStateList(android.R.color.holo_green_dark)
    }

    private fun getTargetName(): String {
        return if (spinnerGestures.selectedItem == "Personalizar...") {
            etCustomGesture.text.toString().ifEmpty { "Personalizado" }
        } else {
            spinnerGestures.selectedItem.toString()
        }
    }

    private fun exportData() {
        if (muestrasLista.isEmpty()) {
            Toast.makeText(this, "No hay muestras para subir", Toast.LENGTH_SHORT).show()
            return
        }

        val gestureName = getTargetName()
        val userId = "usuario_prueba_1" // Esto simula tu usuario actual

        Toast.makeText(this, "Subiendo datos de ${gestureName} a Firebase...", Toast.LENGTH_SHORT).show()

        // Ruta: Proyectos -> usuario_prueba_1 -> DatosRecoleccion -> Letra_A
        val dataRef = database.child("Proyectos").child(userId).child("DatosRecoleccion").child(gestureName)

        // Subimos toda la lista recolectada a Firebase
        dataRef.setValue(muestrasLista).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Si subió bien, avisamos que queremos entrenar
                database.child("Proyectos").child(userId).child("EstadoEntrenamiento").setValue("SOLICITADO")
                Toast.makeText(this, "¡Datos subidos con éxito!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun validateData() {
        if (muestrasLista.size == maxSamples) {
            Toast.makeText(this, "Datos válidos y completos (${maxSamples} muestras)", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Datos incompletos. Tienes ${muestrasLista.size} muestras.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearData() {
        samplesCollected = 0
        muestrasLista.clear() // Vaciamos la lista
        progressBar.progress = 0
        txtSamples.text = "Muestras: 0 / $maxSamples"
        txtRealTimeData.text = "Esperando datos..."
        Toast.makeText(this, "Memoria de sesión limpia", Toast.LENGTH_SHORT).show()
    }
}