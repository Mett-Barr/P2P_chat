package com.example.p2pstream.chatlogic

import java.time.LocalDateTime

data class MessageData(
    val message: String,
    val time: LocalDateTime,
    val sender: Sender
) {}

enum class Sender {
    ME, OTHERS
}