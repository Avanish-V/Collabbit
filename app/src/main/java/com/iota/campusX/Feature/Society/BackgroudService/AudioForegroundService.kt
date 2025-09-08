package com.iota.campusX.feature.society.backgroundservice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.iota.campusX.Feature.Society.BackgroudService.LeaveRoomReceiver
import com.iota.campusX.MainActivity
import com.iota.campusX.R

class AudioForegroundService : Service() {

    companion object {
        const val CHANNEL_ID = "audio_room_channel"
        const val CHANNEL_NAME = "Audio Room"
        const val NOTIFICATION_ID = 0x1001
        const val ACTION_STOP = "com.iota.campusX.action.STOP_FOREGROUND"

        const val EXTRA_TITLE = "extra_title"

        const val EXTRA_TEXT = "extra_text"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE) // ✅ replaces stopForeground(true)
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                val title = intent?.getStringExtra(EXTRA_TITLE) ?: ""
                val text = intent?.getStringExtra(EXTRA_TEXT) ?: ""

                val notification = buildNotification(title = title, text = text)
                startForeground(NOTIFICATION_ID, notification)
            }
        }
        return START_STICKY
    }
    private fun buildNotification(title: String, text: String): Notification {
        // Tap opens the app (MainActivity)
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val openPending = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Stop action — lets user stop background keepalive
        val stopIntent = Intent(this, AudioForegroundService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPending = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        //-----------------------------Leave Room ----------------------------------------------
        val leaveIntent = Intent(this, LeaveRoomReceiver::class.java).apply {
            action = LeaveRoomReceiver.ACTION_LEAVE_ROOM
        }
        val leavePending = PendingIntent.getBroadcast(
            this, 1, leaveIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        //---------------------------------------------------------------------------------------
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title) // add in strings.xml
            .setContentText(text)
            .setSmallIcon(R.drawable.round_mic_24) // <-- must exist (png or proper vector as notification icon)
            .setContentIntent(openPending)
            .setVibrate(longArrayOf(300))
            .addAction(R.drawable.check_circle, "Leave", leavePending)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
            channel.description = "Audio room notification"
            mgr.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mgr.cancel(NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE) // ✅ replaces stopForeground(true)
        super.onDestroy()
    }
}
