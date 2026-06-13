package com.example.quietroom.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quietroom.model.User

@Composable
fun ChatHeader(
    currentUser: User,
    onlineCount: Int,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "QuietRoom")

        TextButton(onClick = onLogout) {
            Text("Logout")
        }
    }

    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp)
    )

    Text(
        text =
            "Signed in as ${currentUser.username} | Online: $onlineCount",
        fontSize = 12.sp
    )
}
