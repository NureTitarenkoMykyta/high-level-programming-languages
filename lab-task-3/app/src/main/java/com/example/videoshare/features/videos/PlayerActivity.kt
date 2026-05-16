package com.example.videoshare.features.videos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.videoshare.data.remote.source.UserService
import com.example.videoshare.R
import com.example.videoshare.data.remote.dto.requests.SubscribeRequest
import com.example.videoshare.data.remote.network.RetrofitClient
import com.example.videoshare.data.remote.source.CommentService
import com.example.videoshare.features.auth.LoginActivity
import com.example.videoshare.services.SocketService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class PlayerActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var rvComments: RecyclerView
    private lateinit var etNewComment: EditText
    private lateinit var btnSendComment: Button
    private lateinit var btnSubscribe: Button
    private lateinit var btnLike: Button
    private lateinit var tvVideoTitle: TextView
    private lateinit var tvUploaderName: TextView

    private lateinit var commentService: CommentService
    private lateinit var userService: UserService

    private var filename: String? = null
    private var videoAuthor: String? = null
    private var currentUsername: String = ""
    private var role: String = "User"

    private var isSubscribed: Boolean = false
    private var isLiked: Boolean = false
    private var likesCount: Int = 0

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userService = UserService(RetrofitClient.authApi, this)

        if (userService.getToken().isNullOrEmpty()) {
            val loginIntent = Intent(this, LoginActivity::class.java)
            loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(loginIntent)
            finish()
            return
        }

        setContentView(R.layout.activity_player)

        commentService = CommentService(RetrofitClient.commentApi)

        playerView = findViewById(R.id.playerView)
        tvUploaderName = findViewById(R.id.tvUploaderName)
        rvComments = findViewById(R.id.rvComments)
        etNewComment = findViewById(R.id.etNewComment)
        btnSendComment = findViewById(R.id.btnSendComment)
        btnSubscribe = findViewById(R.id.btnSubscribe)
        btnLike = findViewById(R.id.btnLike)
        tvVideoTitle = findViewById(R.id.tvVideoTitle)
        val btnShareVideo = findViewById<Button>(R.id.btnShareVideo)

        rvComments.layoutManager = LinearLayoutManager(this)

        currentUsername = userService.getUsername() ?: ""
        role = userService.getRole() ?: "User"

        handleIncomingIntent()
        startSocketService()

        playerView.setFullscreenButtonClickListener { isFullscreen ->
            setFullscreen(isFullscreen)
        }

        btnShareVideo.setOnClickListener {
            handleShare(filename, tvVideoTitle.text.toString())
        }

        btnSubscribe.setOnClickListener {
            toggleSubscribe()
        }

        btnLike.setOnClickListener {
            toggleLike()
        }

        btnSendComment.setOnClickListener {
            handleSendComment()
        }

        fetchComments()
    }

    private fun handleIncomingIntent() {
        filename = intent.getStringExtra("FILENAME")
        val titleFromIntent = intent.getStringExtra("TITLE")

        if (filename == null && Intent.ACTION_VIEW == intent.action) {
            val data: Uri? = intent.data
            if (data != null) {
                val lastSegment = data.lastPathSegment
                if (!lastSegment.isNullOrEmpty()) {
                    filename = URLDecoder.decode(lastSegment, StandardCharsets.UTF_8.name())
                }
            }
        }

        tvVideoTitle.text = titleFromIntent ?: filename ?: "Loading..."

        if (!filename.isNullOrEmpty()) {
            initializePlayer(filename!!)
            fetchVideoDetails(filename!!)
        } else {
            Toast.makeText(this, "Video not found", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun startSocketService() {
        val serviceIntent = Intent(this, SocketService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun fetchVideoDetails(videoFilename: String) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.videoApi.getVideoDetails(videoFilename)
                }
                if (response.isSuccessful && response.body() != null) {
                    val videoData = response.body()!!
                    videoAuthor = videoData.username

                    tvVideoTitle.text = videoData.title
                    tvUploaderName.text = "Uploaded by $videoAuthor"

                    likesCount = videoData.likes.size
                    isLiked = videoData.likes.contains(currentUsername)
                    updateLikeButtonUi()

                    if (videoAuthor == currentUsername) {
                        btnSubscribe.visibility = View.GONE
                    } else {
                        btnSubscribe.visibility = View.VISIBLE
                        checkSubscription(videoAuthor!!)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun toggleLike() {
        val currentFilename = filename ?: return
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    if (isLiked) {
                        RetrofitClient.videoApi.unlikeVideo(currentFilename)
                    } else {
                        RetrofitClient.videoApi.likeVideo(currentFilename)
                    }
                }

                if (response.isSuccessful && response.body() != null) {
                    isLiked = response.body()!!.isLiked
                    likesCount = response.body()!!.likesCount
                    updateLikeButtonUi()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PlayerActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateLikeButtonUi() {
        btnLike.text = "Like ($likesCount)"
        if (isLiked) {
            btnLike.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark))
        } else {
            btnLike.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        }
    }

    private fun checkSubscription(authorName: String) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.userApi.getMe()
                }
                if (response.isSuccessful && response.body() != null) {
                    val subscriptions = response.body()!!.subscriptions
                    isSubscribed = subscriptions.contains(authorName)
                    updateSubscribeButtonUi()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun toggleSubscribe() {
        val author = videoAuthor ?: return
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    if (isSubscribed) {
                        RetrofitClient.userApi.unsubscribe(SubscribeRequest(author))
                    } else {
                        RetrofitClient.userApi.subscribe(SubscribeRequest(author))
                    }
                }

                if (response.isSuccessful) {
                    isSubscribed = !isSubscribed
                    updateSubscribeButtonUi()
                } else {
                    Toast.makeText(this@PlayerActivity, "Action failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@PlayerActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSubscribeButtonUi() {
        if (isSubscribed) {
            btnSubscribe.text = "Unsubscribe"
            btnSubscribe.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        } else {
            btnSubscribe.text = "Subscribe"
            btnSubscribe.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        }
    }

    private fun initializePlayer(videoFilename: String) {
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        val videoUrl = "http://10.0.2.2:5000/videos/stream/$videoFilename"
        val mediaItem = MediaItem.fromUri(videoUrl)
        player?.setMediaItem(mediaItem)
        player?.prepare()
        player?.playWhenReady = true
    }

    private fun handleShare(filename: String?, title: String) {
        if (filename == null) return

        lifecycleScope.launch(Dispatchers.Default) {
            val encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8.name())
                .replace("+", "%20")

            val webUrl = "http://localhost:3000/videos/$encodedFilename"
            val shareMessage = "Check out this video: $title\n$webUrl"

            withContext(Dispatchers.Main) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareMessage)
                }
                startActivity(Intent.createChooser(shareIntent, "Share video via"))
            }
        }
    }

    private fun fetchComments() {
        val currentFilename = filename ?: return
        lifecycleScope.launch {
            val commentList = withContext(Dispatchers.IO) {
                commentService.fetchComments(currentFilename)
            }

            if (commentList != null) {
                rvComments.adapter = CommentAdapter(commentList, role) { comment ->
                    handleDeleteComment(comment._id)
                }
            } else {
                Toast.makeText(this@PlayerActivity, "Failed to load comments", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleSendComment() {
        val text = etNewComment.text.toString().trim()
        val currentFilename = filename ?: return
        if (text.isEmpty()) return

        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                commentService.sendComment(currentFilename, text, currentUsername)
            }

            if (response != null && response.isSuccessful) {
                etNewComment.text.clear()
                fetchComments()
            } else {
                Toast.makeText(this@PlayerActivity, "Failed to send comment", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleDeleteComment(commentId: String) {
        AlertDialog.Builder(this)
            .setMessage("Delete this comment?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    val response = withContext(Dispatchers.IO) {
                        commentService.deleteComment(commentId)
                    }

                    if (response != null && response.isSuccessful) {
                        fetchComments()
                    } else {
                        Toast.makeText(this@PlayerActivity, "Not enough permissions", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }

    fun setFullscreen(isFullscreen: Boolean) {
        val params = playerView.layoutParams as ConstraintLayout.LayoutParams
        val layoutCommentInput = findViewById<View>(R.id.layoutCommentInput)

        if (isFullscreen) {
            params.height = ConstraintLayout.LayoutParams.MATCH_PARENT
            params.matchConstraintPercentHeight = 1.0f
            tvVideoTitle.visibility = View.GONE
            rvComments.visibility = View.GONE
            layoutCommentInput.visibility = View.GONE
            btnSubscribe.visibility = View.GONE
            btnLike.visibility = View.GONE
        } else {
            params.height = 0
            params.matchConstraintPercentHeight = 0.4f
            tvVideoTitle.visibility = View.VISIBLE
            rvComments.visibility = View.VISIBLE
            layoutCommentInput.visibility = View.VISIBLE
            btnLike.visibility = View.VISIBLE
            if (videoAuthor != currentUsername) btnSubscribe.visibility = View.VISIBLE
        }
        playerView.layoutParams = params
    }
}