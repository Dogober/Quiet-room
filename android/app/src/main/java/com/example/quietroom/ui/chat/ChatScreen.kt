package com.example.quietroom.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.quietroom.data.session.SessionManager
import com.example.quietroom.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit
) {

    val currentUserId =
        SessionManager.currentUser?.id
            ?: return
    val viewModel: ChatViewModel = viewModel(
        key = "chat_$currentUserId"
    )

    DisposableEffect(viewModel) {
        viewModel.connect()
        onDispose {
            viewModel.disconnect()
        }
    }

    Column(
        modifier = modifier.padding(16.dp).fillMaxSize().imePadding()
    ) {

        ChatHeader(
            currentUser = viewModel.currentUser,
            onlineCount = viewModel.onlineCount,
            onLogout = onLogout
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        if (viewModel.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.weight(1f)
            )
        } else {
            MessagesList(
                messages = viewModel.messages,
                currentUserId =
                    viewModel.currentUser.id,
                modifier = Modifier.weight(1f)
            )
        }

        viewModel.errorMessage?.let { error ->
            Text(
                text = error,
                color = Color.Red
            )
        }

        if (
            viewModel.typingUser != null
        ) {

            Text(
                text =
                    "${viewModel.typingUser} is typing..."
            )

        }

        MessageInput(
            messageText = viewModel.messageText,
            onMessageChange = viewModel::updateMessageText,
            onSend = viewModel::sendMessage,
            isSending = viewModel.isSending
        )

    }

}
