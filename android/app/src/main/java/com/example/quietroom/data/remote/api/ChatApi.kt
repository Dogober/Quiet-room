package com.example.quietroom.data.remote.api

import com.example.quietroom.data.remote.dto.CreateMessageRequest
import com.example.quietroom.data.remote.dto.MessageDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ChatApi {

    @GET("messages")
    suspend fun getMessages(): List<MessageDto>

    @POST("messages")
    suspend fun sendMessage(
        @Body request: CreateMessageRequest
    ): MessageDto

}