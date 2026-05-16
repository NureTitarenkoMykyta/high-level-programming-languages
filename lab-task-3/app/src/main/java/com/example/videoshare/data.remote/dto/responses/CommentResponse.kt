package com.example.videoshare.data.remote.dto.responses

import java.util.Date

data class CommentResponse(
    val _id: String,
    val videoFilename: String,
    val text: String,
    val username: String,
    val createdAt: Date
)