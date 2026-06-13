package com.example.quietroom.data.repository

import com.example.quietroom.model.Message
import com.example.quietroom.model.User
import com.example.quietroom.data.mapper.toMessage
import com.example.quietroom.data.remote.RetrofitClient
import com.example.quietroom.data.remote.dto.CreateMessageRequest
import com.example.quietroom.data.session.SessionManager
import com.example.quietroom.data.websocket.ChatWebSocket

class ChatRepository {

    private val chatWebSocket =
        ChatWebSocket(

            onMessageReceived = { messageDto ->

                onMessageReceived?.invoke(
                    messageDto.toMessage()
                )

            },

            onOnlineCountReceived = { count ->

                onOnlineCountReceived?.invoke(
                    count
                )

            },

            onTypingReceived = { username ->

                onTypingReceived?.invoke(
                    username
                )

            },

            onConnectionError = {
                onConnectionError?.invoke()
            }

        )

    private var onMessageReceived:
            ((Message) -> Unit)? = null

    private var onOnlineCountReceived:
            ((Int) -> Unit)? = null

    private var lastTypingTime = 0L

    private var onTypingReceived:
            ((String) -> Unit)? = null

    private var onConnectionError:
            (() -> Unit)? = null

    fun sendTyping() {

        val now =
            System.currentTimeMillis()

        if (
            now - lastTypingTime < 1000
        ) {
            return
        }

        lastTypingTime = now

        if (SessionManager.currentUser == null) {
            return
        }

        chatWebSocket.sendTyping()

    }

    suspend fun sendMessage(text: String) {

        RetrofitClient
            .chatApi
            .sendMessage(
                CreateMessageRequest(
                    text = text
                )
            )

    }

    fun getCurrentUser(): User? {
        return SessionManager.currentUser
    }

    suspend fun getMessages(): List<Message> {

        return RetrofitClient
            .chatApi
            .getMessages()
            .map { it.toMessage() }

    }

    fun connect(
        onMessageReceived: (Message) -> Unit,
        onOnlineCountReceived: (Int) -> Unit,
        onTypingReceived: (String) -> Unit,
        onConnectionError: () -> Unit
    ) {

        this.onMessageReceived =
            onMessageReceived

        this.onOnlineCountReceived =
            onOnlineCountReceived

        this.onTypingReceived =
            onTypingReceived

        this.onConnectionError =
            onConnectionError

        chatWebSocket.connect()

    }

    fun disconnect() {
        chatWebSocket.disconnect()
    }
}
