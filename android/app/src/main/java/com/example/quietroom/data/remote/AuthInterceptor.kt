package com.example.quietroom.data.remote

import com.example.quietroom.data.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    override fun intercept(
        chain: Interceptor.Chain
    ): Response {

        val token = SessionManager.token

        val request =
            chain.request()
                .newBuilder()
                .apply {

                    if (token != null) {

                        addHeader(
                            "Authorization",
                            "Bearer $token"
                        )

                    }

                }
                .build()

        return chain.proceed(request)

    }

}