package com.example.videoshare.data.remote.source

import com.example.videoshare.data.remote.api.VideoApi
import com.example.videoshare.data.remote.dto.responses.VideoResponse
import okhttp3.MediaType
import okhttp3.MultipartBody
import retrofit2.Response
import okhttp3.RequestBody

class VideoService(private val api: VideoApi) {

    suspend fun fetchAllVideos(): List<VideoResponse>? {
        return try {
            val response = api.getAllVideos()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getVideoDetails(filename: String): Response<VideoResponse>? {
        return try {
            api.getVideoDetails(filename)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun uploadVideo(
        videoPart: MultipartBody.Part,
        title: String,
        username: String
    ): Response<VideoResponse>? {
        return try {
            val mediaType = MediaType.parse("text/plain")
            val titleRequestBody = RequestBody.create(mediaType, title)
            val usernameRequestBody = RequestBody.create(mediaType, username)

            api.uploadVideo(videoPart, titleRequestBody, usernameRequestBody)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}