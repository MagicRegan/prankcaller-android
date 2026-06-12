package com.magicregan.prankcaller.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.magicregan.prankcaller.MainActivity

class PrankCallService : Service() {

    private var exoPlayer: ExoPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
        }

        val audioUrl = intent?.getStringExtra(EXTRA_AUDIO_URL) ?: return START_NOT_STICKY
        val prankName = intent.getStringExtra(EXTRA_PRANK_NAME) ?: "Prank Call"
        val phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER) ?: ""

        startForeground(NOTIFICATION_ID, createNotification(prankName, phoneNumber))

        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        @Suppress("DEPRECATION")
        audioManager.isSpeakerphoneOn = true

        exoPlayer?.release()
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            setMediaItem(MediaItem.fromUri(audioUrl))
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        val broadcast = Intent(BROADCAST_CALL_ENDED)
                        sendBroadcast(broadcast)
                    }
                }
            })
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
        releaseWakeLock()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Keep running in background even if app is swiped away
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "PrankCaller::CallWakeLock"
        ).apply {
            acquire(10 * 60 * 1000L) // 10 minutes max
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }

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

    private fun createNotification(prankName: String, phoneNumber: String): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, PrankCallService::class.java).apply {
                action = ACTION_STOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Prank Call Active")
            .setContentText("Playing \"$prankName\" to $phoneNumber")
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(openIntent)
            .addAction(android.R.drawable.ic_delete, "End Call", stopIntent)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "prank_call_channel"
        const val NOTIFICATION_ID = 1001
        const val EXTRA_AUDIO_URL = "audio_url"
        const val EXTRA_PRANK_NAME = "prank_name"
        const val EXTRA_PHONE_NUMBER = "phone_number"
        const val ACTION_STOP = "com.magicregan.prankcaller.STOP_CALL"
        const val BROADCAST_CALL_ENDED = "com.magicregan.prankcaller.CALL_ENDED"

        fun start(context: Context, audioUrl: String, prankName: String, phoneNumber: String) {
            val intent = Intent(context, PrankCallService::class.java).apply {
                putExtra(EXTRA_AUDIO_URL, audioUrl)
                putExtra(EXTRA_PRANK_NAME, prankName)
                putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, PrankCallService::class.java))
        }
    }
}
