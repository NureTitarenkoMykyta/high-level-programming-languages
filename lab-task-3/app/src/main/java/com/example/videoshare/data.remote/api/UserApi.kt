package com.example.videoshare.data.remote.api

import com.example.videoshare.data.remote.dto.requests.SubscribeRequest
import com.example.videoshare.data.remote.dto.responses.UserMeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UserApi {

    @GET("users/me")
    suspend fun getMe(): Response<UserMeResponse>

    @POST("users/subscribe")
    suspend fun subscribe(@Body request: SubscribeRequest): Response<Unit>

    @POST("users/unsubscribe")
    suspend fun unsubscribe(@Body request: SubscribeRequest): Response<Unit>
}