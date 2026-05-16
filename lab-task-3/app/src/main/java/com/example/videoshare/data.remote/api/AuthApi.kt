package com.example.videoshare.data.remote.api

import com.example.videoshare.data.remote.dto.requests.AuthRequest
import com.example.videoshare.data.remote.dto.responses.SignInResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): Response<SignInResponse>

    @POST("auth/register")
    suspend fun register(@Body request: AuthRequest): Response<ResponseBody>
}