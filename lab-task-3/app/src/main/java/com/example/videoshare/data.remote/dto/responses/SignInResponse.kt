package com.example.videoshare.data.remote.dto.responses

data class SignInResponse(
    val token: String,
    val username: String,
    val role: String
)