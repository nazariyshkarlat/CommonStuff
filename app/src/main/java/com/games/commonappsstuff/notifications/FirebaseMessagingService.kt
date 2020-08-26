package com.games.commonappsstuff.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.appsflyer.AppsFlyerLib
import com.games.commonappsstuff.PrefsUtils
import com.games.commonappsstuff.R
import com.games.commonappsstuff.ext.IDUtils
import com.games.commonappsstuff.presentation.MainActivity
import com.games.commonappsstuff.presentation.MainActivity.Companion.NOTIFICATIONS_REQUEST_CODE
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {

    companion object{
        const val CHANNEL_ID = "appNotificationsId"
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        if(PrefsUtils.linkIsCached()) {
            if (p0.data.containsKey("ititle") && p0.data.containsKey("ibody")) {
                showNotification(p0.data["ititle"]!!, p0.data["ibody"]!!)
            }
        }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        AppsFlyerLib.getInstance().updateServerUninstallToken(applicationContext, p0)
    }

    private fun showNotification(title: String, text: String){

        createNotificationChannel()

        val openNotificationsPendingIntent = PendingIntent.getActivity(
            this, NOTIFICATIONS_REQUEST_CODE,
            application.packageManager.getLaunchIntentForPackage(application.packageName)!!.apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("REQUEST_CODE", NOTIFICATIONS_REQUEST_CODE)
            }, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(
            this,
            CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openNotificationsPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        NotificationManagerCompat.from(this).notify(IDUtils.createID(), notification.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = this.resources.getString(R.string.notifications_channel_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            val notificationManager: NotificationManager =
                this.getSystemService(JobIntentService.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}