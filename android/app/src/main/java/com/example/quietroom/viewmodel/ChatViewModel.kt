package com.example.quietroom.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quietroom.data.repository.ChatRepository
import com.example.quietroom.model.Message
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val repository = ChatRepository()
    private var typingClearJob: Job? = null

    val currentUser =
        repository.getCurrentUser()
            ?: error("User not loaded")

    var messageText by mutableStateOf("")
        private set

    var messages by mutableStateOf<List<Message>>(
        emptyList()
    )
        private set

    var onlineCount by mutableIntStateOf(0)
        private set

    var typingUser by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    var isSending by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun connect() {
        repository.connect(
            onMessageReceived = { message ->
                mergeMessages(listOf(message))
                errorMessage = null
            },
            onOnlineCountReceived = { count ->
                onlineCount = count
                errorMessage = null
            },
            onTypingReceived = { username ->
                if (username != currentUser.username) {
                    showTyping(username)
                }
            },
            onConnectionError = {
                errorMessage =
                    "Live connection lost. Reconnecting..."
            }
        )
        loadMessages()
    }

    fun updateMessageText(text: String) {
        messageText = text
        if (text.isNotBlank()) {
            repository.sendTyping()
        }
    }

    fun sendMessage() {
        val text = messageText.trim()
        if (
            isSending ||
            text.isEmpty() ||
            text.length > 2000
        ) {
            return
        }

        viewModelScope.launch {
            isSending = true
            errorMessage = null
            try {
                repository.sendMessage(text)
                messageText = ""
            } catch (e: Exception) {

                Log.e(
                    "CHAT",
                    "Send failed",
                    e
                )

                errorMessage =
                    "Message could not be sent"

            } finally {
                isSending = false
            }
        }
    }

    fun disconnect() {
        repository.disconnect()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            isLoading = true
            try {
                mergeMessages(
                    repository.getMessages()
                )
            } catch (_: Exception) {
                errorMessage =
                    "Message history could not be loaded"
            } finally {
                isLoading = false
            }
        }
    }

    private fun mergeMessages(
        incomingMessages: List<Message>
    ) {
        messages = (messages + incomingMessages)
            .associateBy { message -> message.id }
            .values
            .sortedWith(
                compareBy<Message> {
                    it.createdAt ?: ""
                }.thenBy { it.id }
            )
    }

    private fun showTyping(username: String) {
        typingUser = username
        typingClearJob?.cancel()
        typingClearJob = viewModelScope.launch {
            delay(3000)
            typingUser = null
        }
    }

    override fun onCleared() {
        disconnect()
        super.onCleared()
    }
}
