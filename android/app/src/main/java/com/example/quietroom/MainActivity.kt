package com.example.quietroom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.quietroom.data.repository.AuthRepository
import com.example.quietroom.data.session.SessionRepository
import com.example.quietroom.ui.chat.ChatScreen
import com.example.quietroom.ui.login.LoginScreen
import com.example.quietroom.ui.register.RegisterScreen
import com.example.quietroom.ui.theme.QuietRoomTheme

class MainActivity : ComponentActivity() {

    private val sessionRepository =
        SessionRepository(AuthRepository())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            QuietRoomTheme {
                QuietRoomApp(sessionRepository)
            }
        }
    }
}

private enum class SessionState {
    Loading,
    Anonymous,
    Authenticated
}

@Composable
private fun QuietRoomApp(
    sessionRepository: SessionRepository
) {
    val context = LocalContext.current
    var sessionState by remember {
        mutableStateOf(SessionState.Loading)
    }
    var showRegister by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        sessionState =
            if (
                sessionRepository.restoreSession(
                    context.applicationContext
                )
            ) {
                SessionState.Authenticated
            } else {
                SessionState.Anonymous
            }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        when (sessionState) {
            SessionState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            SessionState.Authenticated -> {
                ChatScreen(
                    modifier = Modifier.padding(
                        innerPadding
                    ),
                    onLogout = {
                        sessionRepository.logout(
                            context.applicationContext
                        )
                        sessionState =
                            SessionState.Anonymous
                    }
                )
            }

            SessionState.Anonymous -> {
                if (showRegister) {
                    RegisterScreen(
                        modifier = Modifier.padding(
                            innerPadding
                        ),
                        onRegisterSuccess = {
                            showRegister = false
                        },
                        onBackToLogin = {
                            showRegister = false
                        }
                    )
                } else {
                    LoginScreen(
                        modifier = Modifier.padding(
                            innerPadding
                        ),
                        onLoginSuccess = {
                            sessionState =
                                SessionState.Authenticated
                        },
                        onRegisterClick = {
                            showRegister = true
                        }
                    )
                }
            }
        }
    }
}
