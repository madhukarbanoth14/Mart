package com.mart.distribution.demo.messaging

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mart.distribution.demo.MainActivity
import com.mart.distribution.demo.MartApplication
import com.mart.distribution.demo.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class MartFirebaseMessagingService : FirebaseMessagingService() {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val app = application as? MartApplication ?: return
        scope.launch {
            runCatching { app.container.pushTokenRegistrar.register(token) }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // System auto-displays "notification" messages while backgrounded; this path
        // covers the foreground case (and any data-only messages).
        val title =
            message.notification?.title
                ?: message.data["title"]
                ?: getString(R.string.app_name)
        val body =
            message.notification?.body
                ?: message.data["body"]
                ?: return
        showNotification(title, body, message.data)
    }

    private fun showNotification(
        title: String,
        body: String,
        data: Map<String, String>,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent =
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                data["type"]?.let { putExtra("notif_type", it) }
                data["orderId"]?.let { putExtra("notif_orderId", it) }
            }
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                Random.nextInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val notification =
            NotificationCompat.Builder(this, MartApplication.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(this, R.color.notification_accent))
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build()

        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(), notification)
    }
}
