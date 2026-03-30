package com.example.lexihand

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class TraductorFragment : Fragment() {

    private lateinit var messageList: MutableList<Message>
    private lateinit var adapter: ChatAdapter
    private lateinit var rvChat: RecyclerView
    private var tts: TextToSpeech? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_traductor, container, false)

        // 1. Inicializar lista y RecyclerView
        messageList = mutableListOf()
        rvChat = root.findViewById(R.id.rvChat)

        // Configuración del adaptador (necesitas crear la clase ChatAdapter)
        adapter = ChatAdapter(messageList)
        rvChat.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true // Los mensajes nuevos aparecen abajo
        }
        rvChat.adapter = adapter

        // 2. VOZ DE PERSONA 2 (Micrófono)
        root.findViewById<FloatingActionButton>(R.id.btnMic).setOnClickListener {
            startVoiceRecognition()
        }

        // 3. Configurar TTS (Para que el guante "hable")
        tts = TextToSpeech(requireContext()) { status ->
            if (status != TextToSpeech.ERROR) {
                tts?.language = Locale("es", "MX")
            }
        }

        return root
    }

    private fun startVoiceRecognition() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Persona 2: Hable ahora...")
            startActivityForResult(intent, 100)
        } catch (e: Exception) {
            // Manejar error si el dispositivo no soporta reconocimiento de voz
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = result?.get(0) ?: ""
            addMessage(Message(spokenText, SenderType.PERSONA_2))
        }
    }

    // Método llamado cuando el Guante envía datos procesados por la IA
    fun onGestoDetectado(palabra: String) {
        addMessage(Message(palabra, SenderType.GUANTE))
        // El celular vocaliza lo que el guante "escribió"
        tts?.speak(palabra, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun addMessage(message: Message) {
        messageList.add(message)

        // Lógica de "Borrando anteriores": Limitar a 6 mensajes para fluidez
        if (messageList.size > 6) {
            messageList.removeAt(0)
        }

        adapter.notifyDataSetChanged()
        // Scroll automático al último mensaje
        rvChat.scrollToPosition(messageList.size - 1)
    }

    override fun onDestroy() {
        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
        }
        super.onDestroy()
    }
}