package com.example.firechat.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.firechat.ChatActivity
import com.example.firechat.R
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.opencensus.trace.MessageEvent
import org.greenrobot.eventbus.EventBus

//Service to receive notifications and handle them
class MyFirebaseMessagingService: FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    //Firebase handles the notifications received here. When app is in background firebase send notification.
    //When app is in foreground you can handle it here.
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        Log.d(TAG, "From: ${remoteMessage?.from}")

        // Check if message contains a data payload an log it.
        remoteMessage?.data?.isNotEmpty()?.let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)

            // Handle message within 10 seconds
            // Shows Snackbar with message sendt to room user is subscribed to, if user is in the apps room list.
            EventBus.getDefault().post(remoteMessage.notification?.title + ": " + remoteMessage.notification?.body)
        }

        // Check if message contains a notification payload and log it.
        remoteMessage?.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }

        // Remake the standard used notification build to own personal preferences
        // See method sendNotification further down in this class
    }


    // Receive new tokens here when updated.
    // This is where you should register the token when app is started and if app gets a new token,
    // to make sure user will receive notifications from rooms he is subscribed to
    override fun onNewToken(token: String?) {
        Log.d(TAG, "Refreshed token: $token")

        sendRegistrationToServer(token)
    }

    // Use this method to handle new tokens
    private fun sendRegistrationToServer(token: String?) {
    }

    // Notification build to override the standard build used when receiving notifications
    // Remember channelId if used on devices with OREO where this is required
    private fun sendNotification(messageBody: String) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("id", "showmessage");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = getString(R.string.default_notification_channel_id)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_chat)
            .setContentTitle(getString(R.string.fcm_message))
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
}