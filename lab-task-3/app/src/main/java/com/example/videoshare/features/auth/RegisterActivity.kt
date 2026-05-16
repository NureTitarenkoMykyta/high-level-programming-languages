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
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var userService: UserService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        userService = UserService(RetrofitClient.authApi, this)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnToLogin = findViewById<Button>(R.id.btnToLogin)

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val response = userService.register(AuthRequest(email, password))
                if (response) {
                    Toast.makeText(this@RegisterActivity, "Registration Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@RegisterActivity, "Registration Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        btnToLogin.setOnClickListener {
            finish()
        }
    }
}