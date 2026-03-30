package com.example.lexihand

data class Message(
    val text: String,
    val sender: SenderType
)

enum class SenderType {
    GUANTE, PERSONA_2
}