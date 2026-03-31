package com.example.lexihand

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class TraductorFragment : Fragment() {

    private lateinit var messageList: MutableList<Message>
    private lateinit var adapter: ChatAdapter
    private lateinit var rvChat: RecyclerView
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_traductor, container, false)

        // 1. Inicializar lista y RecyclerView
        messageList = mutableListOf()
        rvChat = root.findViewById(R.id.rvChat)

        // Configuración del adaptador
        adapter = ChatAdapter(messageList)
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true // Los mensajes nuevos aparecen abajo
        rvChat.layoutManager = layoutManager
        rvChat.adapter = adapter

        // 2. VOZ DE PERSONA 2 (Micrófono)
        root.findViewById<FloatingActionButton>(R.id.btnMic).setOnClickListener {
            startVoiceRecognition()
        }

        // 3. Configurar TTS (Para que el celular "hable" lo que el guante traduce)
        tts = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("es", "MX"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Lenguaje no soportado")
                } else {
                    ttsReady = true
                }
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
            Toast.makeText(requireContext(), "Tu dispositivo no soporta dictado por voz", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val result = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = result?.get(0) ?: ""
            // Agregamos el mensaje como Persona 2 (Voz)
            addMessage(Message(spokenText, SenderType.PERSONA_2))
        }
    }

    /**
     * MÉTODO CLAVE: Llamado desde MainActivity cuando el Guante detecta una letra
     */
    fun onGestoDetectado(letra: String) {
        // Usamos requireActivity().runOnUiThread para asegurar que los cambios visuales ocurran en el hilo principal
        activity?.runOnUiThread {
            addMessage(Message(letra, SenderType.GUANTE))

            // Si el motor de voz está listo, vocaliza la letra/palabra
            if (ttsReady) {
                tts?.speak(letra, TextToSpeech.QUEUE_FLUSH, null, "ID_GUANTE")
            }
        }
    }

    private fun addMessage(message: Message) {
        messageList.add(message)

        // Mantener el chat fluido: Si hay más de 10 mensajes, quitamos el más viejo
        if (messageList.size > 10) {
            messageList.removeAt(0)
            adapter.notifyItemRemoved(0)
        }

        adapter.notifyDataSetChanged()

        // Scroll automático suave al último mensaje
        rvChat.smoothScrollToPosition(messageList.size - 1)
    }

    override fun onDestroy() {
        // Liberar memoria del motor de voz al cerrar el fragmento
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}