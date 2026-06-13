package com.example.quietroom.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.quietroom.model.Message

@Composable
fun MessagesList(
    messages: List<Message>,
    currentUserId: Long,
    modifier: Modifier = Modifier
) {

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {

        if (messages.isNotEmpty()) {

            listState.animateScrollToItem(
                messages.lastIndex
            )

        }

    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        state = listState
    ) {

        items(
            items = messages,
            key = { message -> message.id }
        ) { message ->

            MessageItem(
                message = message,
                currentUserId = currentUserId
            )

        }

    }

}
