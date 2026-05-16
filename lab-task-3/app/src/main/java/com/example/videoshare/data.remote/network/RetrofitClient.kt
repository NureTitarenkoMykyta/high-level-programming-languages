package com.example.videoshare.data.remote.network

import com.example.videoshare.data.remote.api.AdminApi
import com.example.videoshare.data.remote.api.AuthApi
import com.example.videoshare.data.remote.api.CommentApi
import com.example.videoshare.data.remote.api.UserApi
import com.example.videoshare.data.remote.api.VideoApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:5000/"

    var tokenProvider: (() -> String?) = { null }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { tokenProvider() })
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val videoApi: VideoApi by lazy { retrofit.create(VideoApi::class.java) }
    val commentApi: CommentApi by lazy { retrofit.create(CommentApi::class.java) }
    val userApi: UserApi by lazy { retrofit.create(UserApi::class.java) }

    val adminApi: AdminApi by lazy { retrofit.create(AdminApi::class.java) }
}