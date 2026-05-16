package com.example.videoshare.features.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.videoshare.data.remote.source.UserService
import com.example.videoshare.R
import com.example.videoshare.data.remote.dto.requests.AuthRequest
import com.example.videoshare.data.remote.network.RetrofitClient
import com.example.videoshare.features.videos.VideosActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var userService: UserService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        userService = UserService(RetrofitClient.authApi, this)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnToRegister = findViewById<Button>(R.id.btnToRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            lifecycleScope.launch {
                val response = userService.login(AuthRequest(email, password))
                if (response != null) {
                    userService.saveToken(response.token)
                    userService.saveUsername(response.username)
                    userService.saveRole(response.role)

                    val intent = Intent(this@LoginActivity, VideosActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid Credentials", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}