package com.example.quietroom.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quietroom.data.repository.AuthRepository
import com.example.quietroom.data.repository.AuthResult
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val repository = AuthRepository()

    var username by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun updateUsername(value: String) {
        username = value
        errorMessage = null
    }

    fun updatePassword(value: String) {
        password = value
        errorMessage = null
    }

    fun register(onSuccess: () -> Unit) {
        val normalizedUsername = username.trim()
        when {
            normalizedUsername.length !in 3..32 -> {
                errorMessage =
                    "Username must contain 3 to 32 characters"
                return
            }

            !normalizedUsername.matches(
                Regex("^[A-Za-z0-9_]+$")
            ) -> {
                errorMessage =
                    "Use only letters, numbers and underscore"
                return
            }

            password.length !in 8..72 -> {
                errorMessage =
                    "Password must contain at least 8 characters"
                return
            }

            isLoading -> return
        }

        viewModelScope.launch {
            isLoading = true
            when (
                val result = repository.register(
                    normalizedUsername,
                    password
                )
            ) {
                AuthResult.Success -> {
                    errorMessage = null
                    password = ""
                    onSuccess()
                }

                is AuthResult.Failure -> {
                    errorMessage = result.message
                }
            }
            isLoading = false
        }
    }
}
