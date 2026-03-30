package com.example.lexihand

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    // Identificadores de tipo
    private val TYPE_GUANTE = 1
    private val TYPE_PERSONA = 2

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].sender == SenderType.GUANTE) TYPE_GUANTE else TYPE_PERSONA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layout = if (viewType == TYPE_GUANTE)
            R.layout.item_message_guante else R.layout.item_message_persona

        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val txtMessage: TextView = view.findViewById(R.id.txtMessage)
        fun bind(message: Message) {
            txtMessage.text = message.text
        }
    }
}