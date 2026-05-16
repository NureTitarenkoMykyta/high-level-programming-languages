package com.example.videoshare.data.remote.api

import com.example.videoshare.data.remote.dto.responses.LikeResponse
import com.example.videoshare.data.remote.dto.responses.VideoResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface VideoApi {
    @GET("videos")
    suspend fun getAllVideos(): Response<List<VideoResponse>>

    @GET("videos/{filename}")
    suspend fun getVideoDetails(@Path("filename") filename: String): Response<VideoResponse>

    @Multipart
    @POST("videos/upload")
    suspend fun uploadVideo(
        @Part video: MultipartBody.Part,
        @Part("title") title: RequestBody,
        @Part("username") username: RequestBody
    ): Response<VideoResponse>

    @POST("videos/{filename}/like")
    suspend fun likeVideo(@Path("filename") filename: String): Response<LikeResponse>

    @DELETE("videos/{filename}/like")
    suspend fun unlikeVideo(@Path("filename") filename: String): Response<LikeResponse>
}