package com.example.videoshare.data.remote.dto.requests

data class CommentRequest(
    val videoFilename: String,
    val text: String,
    val username: String
)