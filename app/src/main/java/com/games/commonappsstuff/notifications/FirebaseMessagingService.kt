package com.games.commonappsstuff.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import com.appsflyer.AppsFlyerLib
import com.facebook.appevents.AppEventsLogger
import com.games.commonappsstuff.R
import com.games.commonappsstuff.presentation.MainActivity.Companion.NOTIFICATIONS_REQUEST_CODE
import com.games.commonappsstuff.utils.IDUtils
import com.games.commonappsstuff.utils.PrefsUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class FirebaseMessagingService : FirebaseMessagingService() {

    companion object{
        const val CHANNEL_ID = "appNotificationsId"
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        if(p0.data.containsKey("type") && p0.data["type"] == "notification") {
            if(PrefsUtils.linkIsCached()) {
                if (p0.data.containsKey("ititle") && p0.data.containsKey("ibody")) {
                    showNotification(p0.data["ititle"]!!, p0.data["ibody"]!!)
                }
            }
        }else if(p0.data.containsKey("type") && p0.data["type"] == "fb_event"){
            sendFbEvent(p0.data)
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

    private fun sendFbEvent(data: Map<String, String>){
        val logger = AppEventsLogger.newLogger(application)

        val params: HashMap<String, Any>? = if(data.containsKey("event_params"))
            Gson().fromJson<HashMap<String, Any>>(data["event_params"], object : TypeToken<HashMap<String, Any>>(){}.type)
        else
            null

        val valueToSum = params?.get("_valueToSum") as? Double?

        if(params?.containsKey("_valueToSum") == true){
            params.remove("_valueToSum")
        }

        when {
            params.isNullOrEmpty()  && valueToSum == null -> {
                Log.d("FB_EVENT", "name: ${data["event_name"]}")
               logger.logEvent(data["event_name"])
            }
            params.isNullOrEmpty() && valueToSum != null -> {
                Log.d("FB_EVENT", "name: ${data["event_name"]}, valueToSum: $valueToSum")
                logger.logEvent(data["event_name"], valueToSum)
            }
            !params.isNullOrEmpty() && valueToSum == null -> {
                Log.d("FB_EVENT", "name: ${data["event_name"]}, params: ${bundleOf(*params.map { it.key to it.value }.toTypedArray())}")
                logger.logEvent(data["event_name"], bundleOf(*params.map { it.key to it.value }.toTypedArray()))
            }
            else -> {
                Log.d("FB_EVENT", "name: ${data["event_name"]}, valueToSum: $valueToSum, params: ${bundleOf(*params.map { it.key to it.value }.toTypedArray())}")
                logger.logEvent(data["event_name"], valueToSum!!, bundleOf(*params.map { it.key to it.value }.toTypedArray()))
            }
        }
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