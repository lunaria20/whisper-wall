package com.example.whisperwall.core.network

import com.example.whisperwall.BuildConfig
import com.example.whisperwall.core.session.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiConfig {
    private val BASE_URL = BuildConfig.API_BASE_URL

    fun createService(sessionManager: SessionManager): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionManager))
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

private class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sessionManager.token
        val requestBuilder = chain.request().newBuilder()

        if (token.isNotBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        requestBuilder.addHeader("Content-Type", "application/json")

        return chain.proceed(requestBuilder.build())
    }
}
