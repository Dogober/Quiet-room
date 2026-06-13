package com.example.quietroom.model

data class Message(
    val id: Long,
    val sender: User,
    val text: String,
    val createdAt: String? = null
)