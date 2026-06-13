package com.example.quietroom.data.remote.api

import com.example.quietroom.data.remote.dto.LoginRequest
import com.example.quietroom.data.remote.dto.LoginResponse
import com.example.quietroom.data.remote.dto.RegisterRequest
import com.example.quietroom.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET

interface AuthApi {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): LoginResponse

    @GET("auth/me")
    suspend fun me(): UserDto

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    )

}