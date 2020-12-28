package com.games.commonappsstuff.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import com.amplitude.api.Amplitude
import com.amplitude.api.Identify
import com.appsflyer.AFLogger
import com.appsflyer.AppsFlyerLib
import com.games.commonappsstuff.App
import com.games.commonappsstuff.BuildConfig
import com.games.commonappsstuff.R
import com.games.commonappsstuff.di.NetworkModule.connectionManager
import com.games.commonappsstuff.presentation.MainActivity.Companion.NOTIFICATIONS_REQUEST_CODE
import com.games.commonappsstuff.presentation.SingleLiveEvent
import com.games.commonappsstuff.utils.IDUtils
import com.games.commonappsstuff.utils.PrefsUtils
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class FirebaseMessagingService : FirebaseMessagingService() {

    companion object{
        const val CHANNEL_ID = "appNotificationsId"
        val popupState = SingleLiveEvent<Pair<String, String>>()
    }

    override fun onMessageReceived(p0: RemoteMessage) {

        if(p0.data.containsKey("type")) {
            when {
                    p0.data["type"] == "notification" -> {
                    if (PrefsUtils.linkIsCached()) {
                        if (p0.data.containsKey("ititle") && p0.data.containsKey("ibody")) {
                            showNotification(
                                p0.data["ititle"]!!,
                                p0.data["ibody"]!!,
                                p0.data["img_url"],
                                p0.data["push_type"]
                            )
                        }
                    }
                }
                p0.data["type"] == "amplitude_event" -> {

                    if (Amplitude.getInstance() == null)
                        (application as App).initAmplitude(PrefsUtils.getAmplitudeApiKey()!!)

                    sendAmplitudeEvent(p0.data)
                }
                p0.data["type"] == "show_popup" -> {
                    popupState.postValue(p0.data["popup_text"]!! to p0.data["popup_button"]!!)
                }
            }
        }
    }

    private fun sendAmplitudeEvent(data: Map<String, String>){

        val params: HashMap<String, String>? = if(data.containsKey("event_params"))
            Gson().fromJson<HashMap<String, String>>(data["event_params"], object : TypeToken<HashMap<String, String>>(){}.type)
        else
            null

        Amplitude.getInstance().logEvent(data["event_name"], if(params != null) JSONObject(params as Map<*, *>) else null)
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        AppsFlyerLib.getInstance().updateServerUninstallToken(applicationContext, p0)
    }

    private fun showNotification(title: String, text: String, imageUrl: String?, pushType: String?){

        createNotificationChannel()

        val openNotificationsPendingIntent = PendingIntent.getActivity(
            this, NOTIFICATIONS_REQUEST_CODE,
            application.packageManager.getLaunchIntentForPackage(application.packageName)!!.apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("REQUEST_CODE", NOTIFICATIONS_REQUEST_CODE)
                putExtra("push_type", pushType)
            }, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val bitmap = if(imageUrl != null) getBitmapFromURL(imageUrl) else null

        val notification = NotificationCompat.Builder(
            this,
            CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .apply {
                if(bitmap != null) {
                    setLargeIcon(bitmap)
                        .setStyle(NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                            .bigLargeIcon(null))
                }
            }
            .setContentIntent(openNotificationsPendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)


        (application as App).sendAmplitudeMessage("show push", mapOf("title" to title, "body" to text, "push_type" to pushType, "img_url" to imageUrl))

        NotificationManagerCompat.from(this).notify(IDUtils.createID(), notification.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = this.resources.getString(R.string.notifications_channel_name)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            val notificationManager: NotificationManager =
                this.getSystemService(JobIntentService.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getBitmapFromURL(strURL: String): Bitmap? {
        return try {
            val url = URL(strURL)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            null
        }
    }
}