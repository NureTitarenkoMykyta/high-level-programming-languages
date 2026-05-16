package com.example.videoshare

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.videoshare.data.remote.source.UserService
import com.example.videoshare.data.remote.network.RetrofitClient
import com.example.videoshare.features.auth.LoginActivity
import com.example.videoshare.features.videos.VideosActivity
import com.example.videoshare.services.SocketService

class MainActivity : AppCompatActivity() {
    private lateinit var userService: UserService
    private val NOTIFICATION_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userService = UserService(RetrofitClient.authApi, this)
        RetrofitClient.tokenProvider = {
            userService.getToken()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_REQUEST_CODE)
            } else {
                handleServiceAndNavigation()
            }
        } else {
            handleServiceAndNavigation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_REQUEST_CODE) {
            handleServiceAndNavigation()
        }
    }

    private fun handleServiceAndNavigation() {
        val token = userService.getToken()
        val currentUsername = userService.getUsername() ?: ""

        if (token.isNullOrEmpty()) {
            navigateToLogin()
            return
        }

        if (currentUsername.isNotEmpty()) {
            Log.i("MainActivity", "Starting SocketService for user: $currentUsername")
            val serviceIntent = Intent(this, SocketService::class.java).apply {
                putExtra("USERNAME", currentUsername)
            }
            ContextCompat.startForegroundService(this, serviceIntent)
        }

        val intent = Intent(this, VideosActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}