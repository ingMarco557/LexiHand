package com.example.lexihand

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class TraductorFragment : Fragment() {

    private lateinit var messageList: MutableList<Message>
    private lateinit var adapter: ChatAdapter
    private lateinit var rvChat: RecyclerView
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    // Variables de control de escritura
    private var palabraActual = ""
    private lateinit var txtWorkbench: TextView
    private lateinit var btnAccion: MaterialButton
    private var modoEnviarActivo = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_traductor, container, false)

        // 1. Inicializar Chat
        messageList = mutableListOf()
        rvChat = root.findViewById(R.id.rvChat)
        adapter = ChatAdapter(messageList)
        rvChat.layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        rvChat.adapter = adapter

        // 2. Inicializar Workbench
        txtWorkbench = root.findViewById(R.id.txtPalabraEnFormacion)
        btnAccion = root.findViewById(R.id.btnEnviarPalabra)
        val btnBorrar = root.findViewById<ImageButton>(R.id.btnBorrarLetra)

        // Lógica del botón de Acción (Escribir / Enviar)
        btnAccion.setOnClickListener {
            if (modoEnviarActivo) {
                enviarPalabraFinal(palabraActual)
            } else {
                Toast.makeText(requireContext(), "Usa el guante para empezar a escribir", Toast.LENGTH_SHORT).show()
            }
        }

        btnBorrar.setOnClickListener {
            if (palabraActual.isNotEmpty()) {
                palabraActual = palabraActual.dropLast(1)
                txtWorkbench.text = if (palabraActual.isEmpty()) "" else palabraActual
                if (palabraActual.isEmpty()) actualizarEstadoBoton(false)
            }
        }

        // 3. Micrófono
        root.findViewById<FloatingActionButton>(R.id.btnMic).setOnClickListener {
            startVoiceRecognition()
        }

        // 4. Configurar Voz (TTS)
        tts = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("es", "MX")
                ttsReady = true
            }
        }

        return root
    }

    /**
     * Se llama desde MainActivity cuando llega una letra del guante
     */
    fun onGestoDetectado(letra: String) {
        activity?.runOnUiThread {
            // Evitar repeticiones inmediatas
            if (palabraActual.endsWith(letra)) return@runOnUiThread

            // Si es la primera letra, cambiamos el botón a modo ENVIAR
            if (!modoEnviarActivo) {
                actualizarEstadoBoton(true)
            }

            palabraActual += letra
            txtWorkbench.text = palabraActual
        }
    }

    private fun actualizarEstadoBoton(activarEnviar: Boolean) {
        modoEnviarActivo = activarEnviar
        if (activarEnviar) {
            btnAccion.text = "Enviar"
            btnAccion.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50")) // Verde
        } else {
            btnAccion.text = "Escribir"
            btnAccion.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#757575")) // Gris
        }
    }

    private fun enviarPalabraFinal(palabra: String) {
        addMessage(Message(palabra, SenderType.GUANTE))

        if (ttsReady) {
            tts?.speak(palabra, TextToSpeech.QUEUE_FLUSH, null, "ID_GUANTE")
        }

        // Resetear todo
        palabraActual = ""
        txtWorkbench.text = ""
        actualizarEstadoBoton(false)
    }

    private fun addMessage(message: Message) {
        messageList.add(message)
        if (messageList.size > 10) messageList.removeAt(0)
        adapter.notifyDataSetChanged()
        rvChat.smoothScrollToPosition(messageList.size - 1)
    }

    private fun startVoiceRecognition() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Escuchando...")
            }
            startActivityForResult(intent, 100)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error en reconocimiento", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val spokenText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            addMessage(Message(spokenText, SenderType.PERSONA_2))
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}