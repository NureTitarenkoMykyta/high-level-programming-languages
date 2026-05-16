package com.example.videoshare.data.remote.source

import com.example.videoshare.data.remote.dto.requests.CommentRequest
import com.example.videoshare.data.remote.dto.responses.CommentResponse
import com.example.videoshare.data.remote.api.CommentApi
import retrofit2.Response

class CommentService(private val api: CommentApi) {

    suspend fun fetchComments(filename: String): List<CommentResponse>? {
        return try {
            val response = api.getComments(filename)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun sendComment(
        filename: String,
        text: String,
        username: String,
    ): Response<Void>? {
        return try {
            val request = CommentRequest(filename, text, username)
            api.sendComment(request)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun deleteComment(commentId: String): Response<Void>? {
        return try {
            api.deleteComment(commentId)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}