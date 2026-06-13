package com.example.quietroom.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MessageDto(
    val id: Long,
    val sender: UserDto,
    val text: String,

    @SerializedName("created_at")
    val createdAt: String? = null
)