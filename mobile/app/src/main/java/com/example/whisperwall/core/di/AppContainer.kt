package com.example.whisperwall.core.di

import android.content.Context
import com.example.whisperwall.core.network.ApiConfig
import com.example.whisperwall.core.repository.AdminRepository
import com.example.whisperwall.core.repository.AuthRepository
import com.example.whisperwall.core.repository.ConfessionRepository
import com.example.whisperwall.core.repository.ModeratorRepository
import com.example.whisperwall.core.repository.ReportRepository
import com.example.whisperwall.core.repository.UserRepository
import com.example.whisperwall.core.session.SessionManager

class AppContainer private constructor(context: Context) {
    val sessionManager = SessionManager(context.applicationContext)
    private val apiService = ApiConfig.createService(sessionManager)

    val authRepository = AuthRepository(apiService)
    val adminRepository = AdminRepository(apiService)
    val userRepository = UserRepository(apiService)
    val confessionRepository = ConfessionRepository(apiService)
    val reportRepository = ReportRepository(apiService)
    val moderatorRepository = ModeratorRepository(apiService)

    companion object {
        @Volatile
        private var instance: AppContainer? = null

        fun from(context: Context): AppContainer {
            return instance ?: synchronized(this) {
                instance ?: AppContainer(context).also { instance = it }
            }
        }
    }
}
