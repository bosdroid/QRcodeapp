package com.expert.qrgenerator.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.expert.qrgenerator.R
import com.expert.qrgenerator.ui.activities.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMIntentService : FirebaseMessagingService() {
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "10001"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        super.onMessageReceived(remoteMessage)
        val message = remoteMessage.data["message"]
        generateNotification(this, message ?: "")
    }

    private fun generateNotification(context: Context, message: String) {
        val icon = R.mipmap.ic_launcher
        val title = getString(R.string.app_name)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationIntent = Intent(this, MainActivity::class.java).apply {
                putExtra("notireferrer", "Notification")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

            val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, title, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = ""
                vibrationPattern = longArrayOf(1000, 1000, 1000, 1000, 1000)
                setSound(uri, audioAttributes)
            }

            notificationManager.createNotificationChannel(channel)

            val builder = Notification.Builder(this, NOTIFICATION_CHANNEL_ID).apply {
                setSmallIcon(icon)
                setContentTitle(title)
                setContentText(message)
                setAutoCancel(true)
                setContentIntent(pendingIntent)
            }

            notificationManager.notify(0, builder.build())
        } else {
            val notificationIntent = Intent(this, MainActivity::class.java).apply {
                putExtra("notireferrer", "Notification")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

            val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val notificationBuilder = NotificationCompat.Builder(this, "channel_id")
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(NotificationCompat.BigTextStyle())
                .setSound(uri)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                .setSmallIcon(icon)
                .setAutoCancel(true)

            notificationManager.notify(0, notificationBuilder.build())
        }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }
}
