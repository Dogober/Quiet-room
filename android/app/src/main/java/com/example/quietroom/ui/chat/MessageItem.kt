package com.example.quietroom.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quietroom.model.Message

@Composable
fun MessageItem(
    message: Message,
    currentUserId: Long
) {

    fun formatTime(
        dateTime: String?
    ): String {

        if (dateTime == null) {
            return ""
        }

        if (dateTime.length < 16) {
            return ""
        }

        return dateTime.substring(11, 16)
    }

    val isCurrentUser =
        message.sender.id == currentUserId

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            if (isCurrentUser)
                Arrangement.End
            else
                Arrangement.Start
    ) {
        Column {

            Text(
                text = formatTime(message.createdAt),
                fontSize = 10.sp,
                color = Color.Gray,
            )

            if (!isCurrentUser) {

                Text(
                    text = message.sender.username,
                    fontSize = 12.sp,
                    color = Color.Gray,
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )
            }

            Card {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(12.dp)
                )

            }
        }

    }
}
