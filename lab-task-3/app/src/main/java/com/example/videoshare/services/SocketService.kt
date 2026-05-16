package com.example.videoshare.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.videoshare.features.videos.PlayerActivity
import com.example.videoshare.utils.NotificationHelper
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

class SocketService : Service() {
    private val TAG = "SocketService"
    private var mSocket: Socket? = null
    private val ONGOING_NOTIFICATION_ID = 1001
    private val SERVICE_CHANNEL_ID = "socket_service_channel"

    override fun onCreate() {
        super.onCreate()
        Log.i("SocketService", "Create socket service")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand() received execution context.")

        val currentUsername = intent?.getStringExtra("USERNAME") ?: ""

        createNotificationChannel()
        startForeground(ONGOING_NOTIFICATION_ID, createForegroundNotification())

        if (currentUsername.isNotEmpty() && mSocket == null) {
            setupSocket(currentUsername)
        }

        return START_STICKY
    }

    private fun setupSocket(username: String) {
        try {
            mSocket = IO.socket("http://10.0.2.2:5000")
            mSocket?.connect()
            mSocket?.emit("identify", username)

            mSocket?.on("new_video_notification") { args ->
                if (args.isNotEmpty()) {
                    val data = args[0] as JSONObject
                    val author = data.getString("author")
                    val title = data.getString("title")
                    val file = data.getString("filename")

                    NotificationHelper.showNotification(applicationContext, author, title, file)
                }
            }
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }

    private fun createForegroundNotification(): Notification {
        val intent = Intent(this, PlayerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, SERVICE_CHANNEL_ID)
            .setContentTitle("VideoShare Sync")
            .setContentText("Listening for new video uploads...")
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                "Socket Connection Service",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mSocket?.disconnect()
        mSocket?.off("new_video_notification")
        mSocket = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}