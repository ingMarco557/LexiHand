package com.example.lexihand

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputLayout

class DataCollectionActivity : AppCompatActivity() {

    // Variables de Estado
    private var isRecording = false
    private var samplesCollected = 0
    private val maxSamples = 100

    // UI Components
    private lateinit var spinnerGestures: Spinner
    private lateinit var layoutCustomGesture: TextInputLayout
    private lateinit var etCustomGesture: EditText
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var txtSamples: TextView
    private lateinit var btnStartStop: Button

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
        Toast.makeText(this, "Generando archivo CSV para ${getTargetName()}...", Toast.LENGTH_SHORT).show()
    }

    private fun validateData() {
        Toast.makeText(this, "Validando integridad de muestras...", Toast.LENGTH_SHORT).show()
    }

    private fun clearData() {
        samplesCollected = 0
        progressBar.progress = 0
        txtSamples.text = "Muestras: 0 / $maxSamples"
        Toast.makeText(this, "Memoria de sesión limpia", Toast.LENGTH_SHORT).show()
    }
}