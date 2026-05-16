package com.example.videoshare.data.remote.dto.responses

import com.google.gson.annotations.SerializedName

data class VideoResponse(
    @SerializedName("_id") val id: String,
    val filename: String,
    val title: String,
    val username: String,
    val likes: List<String> = emptyList(),
    val createdAt: String
)