package com.example.quietroom.data.websocket

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.quietroom.BuildConfig
import com.example.quietroom.data.remote.RetrofitClient
import com.example.quietroom.data.remote.dto.MessageDto
import com.example.quietroom.data.session.SessionManager
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import kotlin.math.min

class ChatWebSocket(
    private val onMessageReceived: (MessageDto) -> Unit,
    private val onOnlineCountReceived: (Int) -> Unit,
    private val onTypingReceived: (String) -> Unit,
    private val onConnectionError: () -> Unit
) {

    private val client = RetrofitClient.okHttpClient
    private val mainHandler = Handler(Looper.getMainLooper())

    private var webSocket: WebSocket? = null
    private var isConnected = false
    private var isConnecting = false
    private var shouldReconnect = false
    private var reconnectAttempts = 0

    private val reconnectRunnable = Runnable {
        connect()
    }

    @Synchronized
    fun connect() {
        if (isConnected || isConnecting) {
            return
        }

        val token = SessionManager.token ?: return
        shouldReconnect = true
        isConnecting = true

        val request = Request.Builder()
            .url(BuildConfig.WEB_SOCKET_URL)
            .header(
                "Authorization",
                "Bearer $token"
            )
            .build()

        webSocket = client.newWebSocket(
            request,
            listener
        )
    }

    @Synchronized
    fun disconnect() {
        shouldReconnect = false
        isConnected = false
        isConnecting = false
        reconnectAttempts = 0
        mainHandler.removeCallbacks(reconnectRunnable)
        webSocket?.close(
            1000,
            "Client disconnected"
        )
        webSocket = null
    }

    fun sendTyping() {
        webSocket?.send(
            """{"type":"typing"}"""
        )
    }

    private val listener = object : WebSocketListener() {

        override fun onOpen(
            webSocket: WebSocket,
            response: Response
        ) {
            synchronized(this@ChatWebSocket) {
                isConnected = true
                isConnecting = false
                reconnectAttempts = 0
            }
        }

        override fun onMessage(
            webSocket: WebSocket,
            text: String
        ) {
            runCatching {
                when (val event =
                    ChatEventParser.parse(text)
                ) {
                    is ChatServerEvent.MessageReceived -> {
                        mainHandler.post {
                            onMessageReceived(
                                event.message
                            )
                        }
                    }

                    is ChatServerEvent.OnlineCountChanged -> {
                        mainHandler.post {
                            onOnlineCountReceived(
                                event.count
                            )
                        }
                    }

                    is ChatServerEvent.UserTyping -> {
                        mainHandler.post {
                            onTypingReceived(
                                event.username
                            )
                        }
                    }

                    null -> Unit
                }
            }.onFailure { error ->
                Log.e(
                    "WEBSOCKET",
                    "Invalid server event",
                    error
                )
            }
        }

        override fun onFailure(
            webSocket: WebSocket,
            throwable: Throwable,
            response: Response?
        ) {
            val shouldNotify =
                synchronized(this@ChatWebSocket) {
                isConnected = false
                isConnecting = false
                if (
                    response?.code == 401 ||
                    response?.code == 403
                ) {
                    shouldReconnect = false
                }
                shouldReconnect
            }
            if (!shouldNotify) {
                return
            }

            Log.e(
                "WEBSOCKET",
                "Connection failed",
                throwable
            )
            mainHandler.post(onConnectionError)
            scheduleReconnect()
        }

        override fun onClosed(
            webSocket: WebSocket,
            code: Int,
            reason: String
        ) {
            synchronized(this@ChatWebSocket) {
                isConnected = false
                isConnecting = false
                if (code == 1008) {
                    shouldReconnect = false
                }
            }
            if (code == 1008) {
                mainHandler.post(onConnectionError)
                return
            }
            scheduleReconnect()
        }
    }

    @Synchronized
    private fun scheduleReconnect() {
        if (!shouldReconnect) {
            return
        }

        mainHandler.removeCallbacks(reconnectRunnable)
        val delayMillis = min(
            30_000L,
            1_000L shl min(
                reconnectAttempts,
                5
            )
        )
        reconnectAttempts += 1
        mainHandler.postDelayed(
            reconnectRunnable,
            delayMillis
        )
    }
}
