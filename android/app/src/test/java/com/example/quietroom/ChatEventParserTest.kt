package com.example.quietroom

import com.example.quietroom.data.websocket.ChatEventParser
import com.example.quietroom.data.websocket.ChatServerEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatEventParserTest {

    @Test
    fun parsesMessageEvent() {
        val event = ChatEventParser.parse(
            """
            {
              "type": "message",
              "data": {
                "id": 42,
                "sender": {
                  "id": 7,
                  "username": "alice"
                },
                "text": "hello",
                "created_at": "2026-06-12T12:30:00Z"
              }
            }
            """.trimIndent()
        )

        assertTrue(
            event is ChatServerEvent.MessageReceived
        )
        event as ChatServerEvent.MessageReceived
        assertEquals(42L, event.message.id)
        assertEquals("alice", event.message.sender.username)
        assertEquals("hello", event.message.text)
    }

    @Test
    fun parsesPresenceAndTypingEvents() {
        val onlineEvent = ChatEventParser.parse(
            """{"type":"online_count","count":3}"""
        )
        val typingEvent = ChatEventParser.parse(
            """{"type":"typing","username":"bob"}"""
        )

        assertEquals(
            ChatServerEvent.OnlineCountChanged(3),
            onlineEvent
        )
        assertEquals(
            ChatServerEvent.UserTyping("bob"),
            typingEvent
        )
    }

    @Test
    fun ignoresUnknownEvents() {
        assertNull(
            ChatEventParser.parse(
                """{"type":"unknown"}"""
            )
        )
    }
}
