package com.example.videoshare.features.videos

import com.example.videoshare.R
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videoshare.data.remote.source.UserService
import com.example.videoshare.data.remote.network.RetrofitClient
import com.example.videoshare.data.remote.source.VideoService
import com.example.videoshare.features.auth.LoginActivity
import com.example.videoshare.features.admin.AdminPanelActivity // Import view entry
import kotlinx.coroutines.launch

class VideosActivity : AppCompatActivity() {

    private lateinit var videoService: VideoService
    private lateinit var userService: UserService
    private lateinit var rvVideos: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videos)

        videoService = VideoService(RetrofitClient.videoApi)
        userService = UserService(RetrofitClient.authApi, this)

        rvVideos = findViewById(R.id.rvVideos)
        rvVideos.layoutManager = LinearLayoutManager(this)

        // Setup Admin Button Toggle context checks
        val btnAdmin = findViewById<Button>(R.id.btnGoToAdmin)
        if (userService.getRole() == "admin") {
            btnAdmin.visibility = View.VISIBLE
            btnAdmin.setOnClickListener {
                startActivity(Intent(this, AdminPanelActivity::class.java))
            }
        }

        findViewById<Button>(R.id.btnGoToUpload).setOnClickListener {
            startActivity(Intent(this, UploadVideoActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            userService.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        loadVideos()
    }

    private fun loadVideos() {
        lifecycleScope.launch {
            val videoList = videoService.fetchAllVideos()
            if (videoList != null) {
                rvVideos.adapter = VideoAdapter(videoList) { video ->
                    val intent = Intent(this@VideosActivity, PlayerActivity::class.java)
                    intent.putExtra("FILENAME", video.filename)
                    intent.putExtra("TITLE", video.title)
                    startActivity(intent)
                }
            } else {
                android.util.Log.e("API_ERROR", "Failed to fetch videos")
            }
        }
    }
}