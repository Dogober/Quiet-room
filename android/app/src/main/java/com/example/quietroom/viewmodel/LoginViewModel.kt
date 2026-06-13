package com.example.quietroom.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quietroom.data.repository.AuthRepository
import com.example.quietroom.data.repository.AuthResult
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

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

    fun login(
        context: Context,
        onSuccess: () -> Unit
    ) {
        if (username.isBlank() || password.isBlank()) {
            errorMessage = "Fill in all fields"
            return
        }

        if (isLoading) {
            return
        }

        viewModelScope.launch {
            isLoading = true
            when (
                val result = repository.login(
                    context = context,
                    username = username.trim(),
                    password = password
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
