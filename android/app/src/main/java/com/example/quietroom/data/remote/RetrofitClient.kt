package com.example.quietroom.data.remote

import com.example.quietroom.BuildConfig
import com.example.quietroom.data.remote.api.ChatApi
import com.example.quietroom.data.remote.api.AuthApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object RetrofitClient {

    val okHttpClient: OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                AuthInterceptor()
            )
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .pingInterval(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()


    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(
            okHttpClient
        )
        .addConverterFactory(
            GsonConverterFactory.create()
        )
        .build()

    val chatApi: ChatApi =
        retrofit.create(ChatApi::class.java)

    val authApi: AuthApi =
        retrofit.create(AuthApi::class.java)

}
