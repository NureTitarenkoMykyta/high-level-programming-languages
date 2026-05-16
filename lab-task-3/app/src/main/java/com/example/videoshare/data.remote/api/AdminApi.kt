package com.example.videoshare.data.remote.api

import com.example.videoshare.data.remote.dto.requests.UpdateRoleRequest
import com.example.videoshare.data.remote.dto.responses.UserResponse
import retrofit2.Response
import retrofit2.http.*

interface AdminApi {
    @GET("admin/users")
    suspend fun fetchUsers(): Response<List<UserResponse>>

    @PATCH("admin/users/{id}/role")
    suspend fun updateRole(
        @Path("id") userId: String,
        @Body request: UpdateRoleRequest
    ): Response<Unit>

    @DELETE("admin/users/{id}")
    suspend fun deleteUser(@Path("id") userId: String): Response<Unit>
}