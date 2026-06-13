package com.example.quietroom.data.mapper

import com.example.quietroom.data.remote.dto.MessageDto
import com.example.quietroom.data.remote.dto.UserDto
import com.example.quietroom.model.Message
import com.example.quietroom.model.User

fun MessageDto.toMessage(): Message {

    return Message(
        id = id,
        sender = User(
            id = sender.id,
            username = sender.username
        ),
        text = text,
        createdAt = createdAt
    )

}

fun Message.toDto(): MessageDto {

    return MessageDto(
        id = id,
        sender = UserDto(
            id = sender.id,
            username = sender.username
        ),
        text = text,
        createdAt = createdAt
    )

}