package com.example.videoshare.data.remote.api

import com.example.videoshare.data.remote.dto.requests.CommentRequest
import com.example.videoshare.data.remote.dto.responses.CommentResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CommentApi {
    @GET("comments/{filename}")
    suspend fun getComments(
        @Path("filename") filename: String
    ): Response<List<CommentResponse>>

    @POST("comments")
    suspend fun sendComment(
        @Body request: CommentRequest
    ): Response<Void>

    @DELETE("comments/{id}")
    suspend fun deleteComment(
        @Path("id") commentId: String
    ): Response<Void>
}