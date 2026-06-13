package com.example.quietroom.data.websocket

import com.example.quietroom.data.remote.dto.MessageDto
import com.example.quietroom.data.remote.dto.OnlineCountDto
import com.example.quietroom.data.remote.dto.TypingDto
import com.google.gson.Gson
import com.google.gson.JsonParser

sealed interface ChatServerEvent {
    data class MessageReceived(
        val message: MessageDto
    ) : ChatServerEvent

    data class OnlineCountChanged(
        val count: Int
    ) : ChatServerEvent

    data class UserTyping(
        val username: String
    ) : ChatServerEvent
}

object ChatEventParser {

    private val gson = Gson()

    fun parse(text: String): ChatServerEvent? {
        val envelope = JsonParser
            .parseString(text)
            .asJsonObject

        return when (
            envelope.get("type")?.asString
        ) {
            "message" -> {
                ChatServerEvent.MessageReceived(
                    gson.fromJson(
                        envelope.get("data"),
                        MessageDto::class.java
                    )
                )
            }

            "online_count" -> {
                ChatServerEvent.OnlineCountChanged(
                    gson.fromJson(
                        envelope,
                        OnlineCountDto::class.java
                    ).count
                )
            }

            "typing" -> {
                ChatServerEvent.UserTyping(
                    gson.fromJson(
                        envelope,
                        TypingDto::class.java
                    ).username
                )
            }

            else -> null
        }
    }
}
