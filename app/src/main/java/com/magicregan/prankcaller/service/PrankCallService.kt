package com.magicregan.prankcaller.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class PrankCallService : Service() {

    private var exoPlayer: ExoPlayer? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val audioUrl = intent?.getStringExtra(EXTRA_AUDIO_URL) ?: return START_NOT_STICKY
        val prankName = intent.getStringExtra(EXTRA_PRANK_NAME) ?: "Prank Call"

        startForeground(NOTIFICATION_ID, createNotification(prankName))

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = true

        exoPlayer?.release()
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            setMediaItem(MediaItem.fromUri(audioUrl))
            prepare()
            play()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        exoPlayer?.release()
        exoPlayer = null
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_NORMAL
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Prank Calls",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Active prank call notifications"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(prankName: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Prank Call Active")
            .setContentText("Playing: $prankName")
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "prank_call_channel"
        const val NOTIFICATION_ID = 1001
        const val EXTRA_AUDIO_URL = "audio_url"
        const val EXTRA_PRANK_NAME = "prank_name"

        fun start(context: Context, audioUrl: String, prankName: String) {
            val intent = Intent(context, PrankCallService::class.java).apply {
                putExtra(EXTRA_AUDIO_URL, audioUrl)
                putExtra(EXTRA_PRANK_NAME, prankName)
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, PrankCallService::class.java))
        }
    }
}
