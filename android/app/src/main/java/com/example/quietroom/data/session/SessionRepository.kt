package com.example.quietroom.data.session

import android.content.Context
import com.example.quietroom.data.repository.AuthRepository

class SessionRepository(
    private val authRepository: AuthRepository
) {

    suspend fun restoreSession(
        context: Context
    ): Boolean {

        val token =
            TokenStorage.getToken(context)
                ?: return false

        SessionManager.token = token

        val restored =
            authRepository.restoreSession()

        if (!restored) {
            logout(context)
        }

        return restored

    }

    fun logout(
        context: Context
    ) {

        TokenStorage.clearToken(
            context
        )

        SessionManager.token = null

        SessionManager.currentUser = null

    }

}
