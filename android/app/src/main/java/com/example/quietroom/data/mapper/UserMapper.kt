package com.example.quietroom.data.mapper

import com.example.quietroom.data.remote.dto.UserDto
import com.example.quietroom.model.User

fun UserDto.toUser(): User {
    return User(
        id = id,
        username = username
    )
}