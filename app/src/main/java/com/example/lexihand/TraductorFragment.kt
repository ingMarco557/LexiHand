package com.example.lexihand

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
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

    // --- NUEVO: Bandera para saber si la App está hablando ---
    private var estaHablando = false

    private var palabraActual = ""
    private var ultimaLetraAgregada = ""
    private lateinit var txtWorkbench: TextView
    private lateinit var btnAccion: MaterialButton
    private var modoEnviarActivo = false

    private val guanteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "GUANTE_AI_DATA") {
                // SI LA APP ESTÁ HABLANDO, IGNORAMOS EL GUANTE
                if (estaHablando) return

                val letra = intent.getStringExtra("letra") ?: "--"
                val confianza = intent.getIntExtra("confianza", 0)

                if (confianza >= 85 && letra != "--") {
                    onGestoDetectado(letra)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_traductor, container, false)

        messageList = mutableListOf()
        rvChat = root.findViewById(R.id.rvChat)
        adapter = ChatAdapter(messageList)
        rvChat.layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        rvChat.adapter = adapter

        txtWorkbench = root.findViewById(R.id.txtPalabraEnFormacion)
        btnAccion = root.findViewById(R.id.btnEnviarPalabra)
        val btnBorrar = root.findViewById<ImageButton>(R.id.btnBorrarLetra)

        btnAccion.setOnClickListener {
            if (modoEnviarActivo && palabraActual.isNotEmpty()) {
                enviarPalabraFinal(palabraActual)
            }
        }

        btnBorrar.setOnClickListener {
            if (!estaHablando && palabraActual.isNotEmpty()) {
                palabraActual = palabraActual.dropLast(1)
                txtWorkbench.text = palabraActual
                if (palabraActual.isEmpty()) {
                    actualizarEstadoBoton(false)
                    ultimaLetraAgregada = ""
                }
            }
        }

        root.findViewById<FloatingActionButton>(R.id.btnMic).setOnClickListener {
            startVoiceRecognition()
        }

        // --- CONFIGURACIÓN DE VOZ CON DETECTOR DE SILENCIO ---
        tts = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("es", "MX")
                ttsReady = true

                // Configuramos el "oído" del TTS
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        estaHablando = true
                        activity?.runOnUiThread {
                            txtWorkbench.setTextColor(Color.GRAY) // Visualmente indicamos pausa
                        }
                    }

                    override fun onDone(utteranceId: String?) {
                        estaHablando = false
                        activity?.runOnUiThread {
                            txtWorkbench.setTextColor(Color.BLACK)
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        estaHablando = false
                    }
                })
            }
        }

        return root
    }

    private fun onGestoDetectado(letra: String) {
        activity?.runOnUiThread {
            if (letra == ultimaLetraAgregada) return@runOnUiThread

            if (letra == "S") {
                if (palabraActual.isNotEmpty() && !palabraActual.endsWith(" ")) {
                    palabraActual += " "
                    txtWorkbench.text = palabraActual
                }
                ultimaLetraAgregada = letra
                return@runOnUiThread
            }

            palabraActual += letra
            txtWorkbench.text = palabraActual
            ultimaLetraAgregada = letra

            if (!modoEnviarActivo) {
                actualizarEstadoBoton(true)
            }
        }
    }

    private fun actualizarEstadoBoton(activarEnviar: Boolean) {
        modoEnviarActivo = activarEnviar
        if (activarEnviar) {
            btnAccion.text = "Enviar"
            btnAccion.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
        } else {
            btnAccion.text = "Escribir"
            btnAccion.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#757575"))
        }
    }

    private fun enviarPalabraFinal(palabra: String) {
        addMessage(Message(palabra, SenderType.GUANTE))

        if (ttsReady) {
            // Es vital pasar un ID (en este caso "ID_GUANTE") para que el Listener funcione
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ID_GUANTE")
            tts?.speak(palabra, TextToSpeech.QUEUE_FLUSH, params, "ID_GUANTE")
        }

        palabraActual = ""
        ultimaLetraAgregada = ""
        txtWorkbench.text = ""
        actualizarEstadoBoton(false)
    }

    private fun addMessage(message: Message) {
        messageList.add(message)
        adapter.notifyDataSetChanged()
        rvChat.smoothScrollToPosition(messageList.size - 1)
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Escuchando...")
        }
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val spokenText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            addMessage(Message(spokenText, SenderType.PERSONA_2))
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter("GUANTE_AI_DATA")
        ContextCompat.registerReceiver(requireContext(), guanteReceiver, filter, ContextCompat.RECEIVER_EXPORTED)
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(guanteReceiver)
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}