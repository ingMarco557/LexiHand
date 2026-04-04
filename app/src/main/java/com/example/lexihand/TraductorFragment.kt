package com.example.lexihand

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class TraductorFragment : Fragment() {

    private var messageList = mutableListOf<Message>()
    private lateinit var adapter: ChatAdapter
    private lateinit var rvChat: RecyclerView
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var estaHablando = false

    private var palabraActual = ""
    private var ultimaLetraAgregada = ""
    private lateinit var txtWorkbench: TextView
    private lateinit var btnAccion: MaterialButton
    private var modoEnviarActivo = false

    private val guanteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "GUANTE_AI_DATA" && isAdded) {
                if (estaHablando) return
                val letra = intent.getStringExtra("letra") ?: return
                val confianza = intent.getIntExtra("confianza", 0)

                if (confianza >= 85) {
                    onGestoDetectado(letra)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_traductor, container, false)

        rvChat = root.findViewById(R.id.rvChat)
        txtWorkbench = root.findViewById(R.id.txtPalabraEnFormacion)
        btnAccion = root.findViewById(R.id.btnEnviarPalabra)
        val btnBorrar = root.findViewById<ImageButton>(R.id.btnBorrarLetra)
        val btnMic = root.findViewById<FloatingActionButton>(R.id.btnMic)

        adapter = ChatAdapter(messageList)
        rvChat.layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        rvChat.adapter = adapter

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

        btnMic.setOnClickListener { startVoiceRecognition() }

        initTTS()
        return root
    }

    private fun initTTS() {
        tts = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("es", "MX")
                ttsReady = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(id: String?) { estaHablando = true }
                    override fun onDone(id: String?) { estaHablando = false }
                    override fun onError(id: String?) { estaHablando = false }
                })
            }
        }
    }

    private fun onGestoDetectado(letra: String) {
        if (letra == ultimaLetraAgregada) return

        activity?.runOnUiThread {
            if (letra == "S") {
                if (palabraActual.isNotEmpty() && !palabraActual.endsWith(" ")) {
                    palabraActual += " "
                }
            } else {
                palabraActual += letra
            }

            txtWorkbench.text = palabraActual
            ultimaLetraAgregada = letra
            if (!modoEnviarActivo) actualizarEstadoBoton(true)
        }
    }

    private fun actualizarEstadoBoton(activarEnviar: Boolean) {
        modoEnviarActivo = activarEnviar
        btnAccion.text = if (activarEnviar) "Enviar" else "Escribir"
        val color = if (activarEnviar) "#4CAF50" else "#757575"
        btnAccion.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
    }

    private fun enviarPalabraFinal(palabra: String) {
        val newMessage = Message(palabra, SenderType.GUANTE)
        messageList.add(newMessage)
        adapter.notifyItemInserted(messageList.size - 1)
        rvChat.scrollToPosition(messageList.size - 1)

        if (ttsReady) {
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ID_G")
            tts?.speak(palabra, TextToSpeech.QUEUE_FLUSH, params, "ID_G")
        }

        palabraActual = ""
        ultimaLetraAgregada = ""
        txtWorkbench.text = ""
        actualizarEstadoBoton(false)
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        try {
            startActivityForResult(intent, 100)
        } catch (e: Exception) {
            Toast.makeText(context, "Microfono no disponible", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val res = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!res.isNullOrEmpty()) {
                messageList.add(Message(res[0], SenderType.PERSONA_2))
                adapter.notifyItemInserted(messageList.size - 1)
                rvChat.scrollToPosition(messageList.size - 1)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter("GUANTE_AI_DATA")
        // Corrección para Android 13/14+: Especificar RECEIVER_EXPORTED o NOT_EXPORTED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(guanteReceiver, filter, ContextCompat.RECEIVER_EXPORTED)
        } else {
            requireContext().registerReceiver(guanteReceiver, filter)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            requireContext().unregisterReceiver(guanteReceiver)
        } catch (e: Exception) { }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}