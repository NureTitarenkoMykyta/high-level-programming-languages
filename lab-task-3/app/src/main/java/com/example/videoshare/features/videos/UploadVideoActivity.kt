package com.example.videoshare.features.videos

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.videoshare.R
import com.example.videoshare.data.remote.network.RetrofitClient
import com.example.videoshare.data.remote.source.VideoService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream

class UploadVideoActivity : AppCompatActivity() {

    private lateinit var videoService: VideoService
    private var selectedVideoUri: Uri? = null

    private lateinit var progressBar: ProgressBar
    private lateinit var tvUploadStatus: TextView
    private lateinit var btnUpload: Button
    private lateinit var btnSelectVideo: Button

    private val selectVideoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedVideoUri = uri
            findViewById<TextView>(R.id.tvSelectedFileName).text = getFileName(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_video)

        videoService = VideoService(RetrofitClient.videoApi)

        progressBar = findViewById(R.id.progressBar)
        tvUploadStatus = findViewById(R.id.tvUploadStatus)
        btnUpload = findViewById(R.id.btnUpload)
        btnSelectVideo = findViewById(R.id.btnSelectVideo)

        btnSelectVideo.setOnClickListener {
            selectVideoLauncher.launch("video/*")
        }

        btnUpload.setOnClickListener {
            val title = findViewById<EditText>(R.id.etVideoTitle).text.toString()
            if (title.isBlank() || selectedVideoUri == null) {
                Toast.makeText(this, "Please enter a title and select a video", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            uploadVideo(title, selectedVideoUri!!)
        }
    }

    private fun uploadVideo(title: String, uri: Uri) {
        setLoadingState(true)

        lifecycleScope.launch {
            val success = withContext(Dispatchers.IO) {
                try {
                    val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r", null) ?: return@withContext false
                    val inputStream = contentResolver.openInputStream(uri) ?: return@withContext false

                    var fileName = getFileName(uri)
                    if (!fileName.endsWith(".mp4", ignoreCase = true)) {
                        fileName = "$fileName.mp4"
                    }

                    val file = File(cacheDir, fileName)
                    val outputStream = FileOutputStream(file)
                    inputStream.copyTo(outputStream)

                    val mediaType = MediaType.parse("video/mp4")
                    val requestBody = RequestBody.create(mediaType, file)
                    val videoPart = MultipartBody.Part.createFormData("video", file.name, requestBody)

                    val username = "Current_User"
                    val response = videoService.uploadVideo(videoPart, title, username)

                    parcelFileDescriptor.close()
                    inputStream.close()
                    outputStream.close()

                    response != null && response.isSuccessful
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            setLoadingState(false)

            if (success) {
                Toast.makeText(this@UploadVideoActivity, "Uploaded successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@UploadVideoActivity, "Upload failed. Server rejected the file.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            tvUploadStatus.visibility = View.VISIBLE
            btnUpload.isEnabled = false
            btnSelectVideo.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            tvUploadStatus.visibility = View.GONE
            btnUpload.isEnabled = true
            btnSelectVideo.isEnabled = true
        }
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) result = it.getString(index)
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "video.mp4"
    }
}