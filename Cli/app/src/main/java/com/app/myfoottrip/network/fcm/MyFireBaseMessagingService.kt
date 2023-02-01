package com.app.myfoottrip.network.fcm

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.app.myfoottrip.ui.view.main.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFireBaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Log.d("FCM Log", "Refreshed token: $token")
    }

//    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        if (remoteMessage.notification != null) {
//            Log.d("FCM Log", "알림 메시지: " + remoteMessage.notification!!.body)
//            val messageBody = remoteMessage.notification!!.body
//            val messageTitle = remoteMessage.notification!!.title
//            val intent = Intent(this, MainActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//            val pendingIntent =
//                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
//            val channelId = "Channel ID"
//            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
//            val notificationBuilder: NotificationCompat.Builder = Notification.Builder(
//                this,
//                channelId
//            )
//                .setSmallIcon(R.mipmap.)
//                .setContentTitle(messageTitle)
//                .setContentText(messageBody)
//                .setAutoCancel(true)
//                .setSound(defaultSoundUri)
//                .setContentIntent(pendingIntent)
//            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                val channelName = "Channel Name"
//                val channel =
//                    NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
//                notificationManager.createNotificationChannel(channel)
//            }
//            notificationManager.notify(0, notificationBuilder.build())
//        }
//    }
}