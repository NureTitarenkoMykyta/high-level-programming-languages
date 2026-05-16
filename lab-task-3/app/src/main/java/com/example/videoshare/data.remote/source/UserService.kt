package com.example.videoshare.data.remote.source

import android.content.Context
import com.example.videoshare.data.remote.api.AuthApi
import com.example.videoshare.data.remote.dto.requests.AuthRequest
import com.example.videoshare.data.remote.dto.responses.SignInResponse

class UserService(private val api: AuthApi, private val context: Context) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString("jwtToken", token).apply()
    }

    fun getToken(): String? = prefs.getString("jwtToken", null)

    fun saveUsername(username: String) {
        prefs.edit().putString("username", username).apply()
    }

    fun getUsername(): String? = prefs.getString("username", null)

    fun saveRole(role: String) {
        prefs.edit().putString("role", role).apply()
    }

    fun getRole(): String? = prefs.getString("role", null)

    fun logout() {
        prefs.edit()
            .remove("jwtToken")
            .remove("username")
            .remove("isAdmin")
            .apply()
    }

    suspend fun login(request: AuthRequest): SignInResponse? {
        val response = api.login(request)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun register(request: AuthRequest): Boolean {
        val response = api.register(request)
        return response.isSuccessful
    }
}