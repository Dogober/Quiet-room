package com.example.quietroom.data.repository

import android.content.Context
import com.example.quietroom.data.mapper.toUser
import com.example.quietroom.data.remote.RetrofitClient
import com.example.quietroom.data.remote.dto.LoginRequest
import com.example.quietroom.data.remote.dto.RegisterRequest
import com.example.quietroom.data.session.SessionManager
import com.example.quietroom.data.session.TokenStorage
import com.example.quietroom.model.User
import java.io.IOException
import retrofit2.HttpException

sealed interface AuthResult {
    data object Success : AuthResult
    data class Failure(
        val message: String
    ) : AuthResult
}

class AuthRepository {

    suspend fun login(
        context: Context,
        username: String,
        password: String
    ): AuthResult {
        return try {
            val response = RetrofitClient.authApi.login(
                LoginRequest(
                    username = username,
                    password = password
                )
            )

            SessionManager.token = response.accessToken
            TokenStorage.saveToken(
                context.applicationContext,
                response.accessToken
            )
            SessionManager.currentUser = getCurrentUser()
            AuthResult.Success
        } catch (error: Exception) {
            TokenStorage.clearToken(
                context.applicationContext
            )
            SessionManager.token = null
            SessionManager.currentUser = null
            AuthResult.Failure(
                error.toUserMessage(
                    unauthorizedMessage =
                        "Invalid username or password"
                )
            )
        }
    }

    suspend fun getCurrentUser(): User {
        return RetrofitClient
            .authApi
            .me()
            .toUser()
    }

    suspend fun restoreSession(): Boolean {
        return try {
            SessionManager.currentUser =
                getCurrentUser()
            true
        } catch (_: Exception) {
            SessionManager.currentUser = null
            false
        }
    }

    suspend fun register(
        username: String,
        password: String
    ): AuthResult {
        return try {
            RetrofitClient.authApi.register(
                RegisterRequest(
                    username = username,
                    password = password
                )
            )
            AuthResult.Success
        } catch (error: Exception) {
            AuthResult.Failure(
                error.toUserMessage(
                    conflictMessage =
                        "This username is already taken"
                )
            )
        }
    }

    private fun Exception.toUserMessage(
        unauthorizedMessage: String =
            "Your session has expired",
        conflictMessage: String =
            "The request conflicts with existing data"
    ): String {
        return when (this) {
            is IOException ->
                "Cannot connect to the server"

            is HttpException -> when (code()) {
                401 -> unauthorizedMessage
                409 -> conflictMessage
                in 500..599 ->
                    "The server is temporarily unavailable"

                else -> "Request failed"
            }

            else -> "Unexpected error"
        }
    }
}
