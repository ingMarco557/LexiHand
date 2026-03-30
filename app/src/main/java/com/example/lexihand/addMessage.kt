package com.example.lexihand

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TranslatorFragment : Fragment() {

    private lateinit var messageList: MutableList<Message>
    private lateinit var adapter: ChatAdapter
    private lateinit var rvChat: RecyclerView

    // ... (onCreateView y resto de lógica de botones) ...

    /**
     * Añade un mensaje al chat y gestiona la regla de los 6 mensajes máximos.
     * Esto permite una conversación fluida eliminando lo antiguo automáticamente.
     */
    private fun addMessage(message: Message) {
        // 1. Añadimos el nuevo mensaje (ya sea del Guante o de Persona 2)
        messageList.add(message)

        // 2. REGLA DE NEGOCIO: Si superamos los 6 mensajes, borramos el más viejo
        if (messageList.size > 6) {
            // Eliminamos el primer elemento de la lista (el más antiguo)
            messageList.removeAt(0)

            // Notificamos al adaptador de ambos cambios para que la animación sea fluida
            adapter.notifyItemRemoved(0)
            adapter.notifyItemInserted(messageList.size - 1)
        } else {
            // Si aún no llegamos a 6, solo notificamos la nueva inserción
            adapter.notifyItemInserted(messageList.size - 1)
        }

        // 3. Scroll automático: Desplazamos la vista al último mensaje recibido
        // Usamos smoothScroll para que el movimiento sea agradable a la vista
        rvChat.smoothScrollToPosition(messageList.size - 1)
    }

    /**
     * Ejemplo de uso cuando el sistema BLE detecta una seña traducida por la IA
     */
    fun onGestoTraducido(texto: String) {
        val nuevoMensaje = Message(texto, SenderType.GUANTE)
        addMessage(nuevoMensaje)
    }
}