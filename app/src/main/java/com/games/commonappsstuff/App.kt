package com.games.commonappsstuff

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.util.Log
import android.view.inputmethod.InputMethodInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodSubtype
import androidx.lifecycle.MutableLiveData
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import com.amplitude.api.Identify
import com.appsflyer.AFLogger
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.facebook.FacebookSdk
import com.facebook.LoggingBehavior
import com.facebook.appevents.AppEventsLogger
import com.games.commonappsstuff.connection.backend.BackendService
import com.games.commonappsstuff.di.DI
import com.games.commonappsstuff.di.NetworkModule
import com.games.commonappsstuff.utils.BatteryUtils
import com.games.commonappsstuff.utils.PrefsUtils
import com.games.commonappsstuff.utils.getLauncherActivityName
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.common.util.concurrent.SettableFuture
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.*
import org.json.JSONObject
import pro.userx.UserX
import java.util.*

open class App : Application(){

    val amplitudeLogsScope = CoroutineScope(Dispatchers.Default)
    val amplitudeClientFuture: SettableFuture<AmplitudeClient> = SettableFuture.create()

    var popupWasShown = false

    private val backendService: BackendService by lazy { NetworkModule.getService(BackendService::class.java) }
    private val pushFuture: SettableFuture<String?> = SettableFuture.create()
    val startAppState = MutableLiveData<StartAppStates>()
    private var waitingJob: Job? = null
    private var backendRequestJob : Job? = null

    private var advertisingId: SettableFuture<String?> = SettableFuture.create()

    private var appsFlyerUID: String? = null

    override fun onCreate() {
        super.onCreate()
        DI.initialize(this)

        val networkIsAbsent = NetworkModule.connectionManager.isNetworkAbsent()

        startAppState.value =when {
            networkIsAbsent -> StartAppStates.ShowApp
            PrefsUtils.linkIsCached() -> StartAppStates.ShowWebView(
                PrefsUtils.getLinkCache(), true
            )
            else -> StartAppStates.ShowSplashScreen
        }

        if(!networkIsAbsent) {

            if(!PrefsUtils.linkIsCached())
                getAdvertisingId()

            initAppsFlyer(resources.getString(R.string.appsflyer_dev_key))
            getApiKeys()
            initFirebase()
        }
    }

    private fun getApiKeys(){
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val result = backendService.getApiKeys(if (BuildConfig.DEBUG) "test.bundle.com" else packageName)
                val body = result.body()!!
                if(body.result){
                    initAmplitude(
                        body.amplitudeApiKey
                    )
                    initUserX(body.userXApiKey)

                    body.facebookApiKey?.let { initFBSdk(it) }

                    if(PrefsUtils.linkIsCached())
                        sendShowPopupMessage()

                    if(startAppState.value == StartAppStates.ShowApp){
                        FirebaseAnalytics.getInstance(this@App).logEvent("start_game", null)
                        AppsFlyerLib.getInstance().trackEvent(this@App, "start_game", null)
                    }
                }
            }catch (e: java.lang.Exception){
                e.printStackTrace()
                sendAmplitudeMessage(
                    "exception when get api keys",
                    mapOf("exception_message" to e.message)
                )
                amplitudeClientFuture.set(null)
            }
        }
    }

    fun initAmplitude(apiKey: String){
        PrefsUtils.setAmplitudeApiKey(apiKey)
        Amplitude.getInstance()
            .initialize(applicationContext, apiKey)
            .enableForegroundTracking(this)
            .enableLogging(BuildConfig.DEBUG)
            .identify(
                Identify().set(
                    "bundle_id",
                    if (BuildConfig.DEBUG) "test.bundle.com" else packageName
                ).set(
                    "advertising_id",
                    if (!PrefsUtils.linkIsCached()) advertisingId.get() else PrefsUtils.getAdvertisingId()
                )
            )
        amplitudeClientFuture.set(Amplitude.getInstance())
        Log.d("DEFERRED_DEEP_LINK", "amplitude was initialized")
        sendAmplitudeMessage(
            "amplitude was initialized",
            mapOf("network_is_absent" to NetworkModule.connectionManager.isNetworkAbsent())
        )
    }

    private fun initUserX(userXKey: String){
        UserX.init(userXKey)
        UserX.startScreenRecording()
        sendAmplitudeMessage(
            "userX was initialized", mapOf(
                "network_is_absent" to NetworkModule.connectionManager.isNetworkAbsent(),
                "user_x_key" to userXKey
            )
        )
    }

    private fun initFBSdk(facebookAppId: String) {
        FacebookSdk.setApplicationId(facebookAppId)
        FacebookSdk.sdkInitialize(applicationContext)
        FacebookSdk.fullyInitialize()
        AppEventsLogger.activateApp(this)
        if(BuildConfig.DEBUG){
            FacebookSdk.setIsDebugEnabled(true)
            FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS)
        }
        sendAmplitudeMessage(
            "facebook sdk was initialized",
            mapOf(
                "network_is_absent" to NetworkModule.connectionManager.isNetworkAbsent(),
                "facebook_app_id" to FacebookSdk.getApplicationId()
            )
        )
    }

    private fun sendBackendMessage(attrs: Map<String, Any>) {
        backendRequestJob = GlobalScope.launch {
            try {
                sendAmplitudeMessage(
                    "url request was sent"
                )
                val result =
                    backendService.sendFirstOpenMessage(
                        appsFlyerUID!!,
                        if (BuildConfig.DEBUG) "test.bundle.com" else packageName,
                        pushFuture.get()!!,
                        attrs["campaign"] as String? ?: attrs["campaign_name"] as String?,
                        attrs["adset"] as String? ?: attrs["adset_name"] as String?,
                        attrs["adgroup"] as String? ?: attrs["adgroup_name"] as String?,
                        attrs["af_status"] as String?,
                        Locale.getDefault().country,
                        (getSystemService(BATTERY_SERVICE) as BatteryManager).getIntProperty(
                           BatteryManager.BATTERY_PROPERTY_CAPACITY),
                        BatteryUtils.isConnected(this@App),
                        advertisingId.get()
                    )
                sendAmplitudeMessage(
                    "url result received", mapOf(
                        "request_url" to result.raw().request().url(),
                        "result_code" to result.code(),
                        "result_body" to result.body()
                    )
                )
                if (result.code() == 200) {
                    sendShowPopupMessage()
                    PrefsUtils.setLinkCache(result.body()!!.message!!)

                    if (waitingJob?.isCompleted == false)
                        startAppState.postValue(StartAppStates.ShowWebView(result.body()!!.message))
                    else
                        startAppActivity()
                } else {
                    if (waitingJob?.isCompleted == false)
                        startAppState.postValue(StartAppStates.ShowApp)
                }
                Log.d("DEFERRED_DEEP_LINK", "result: ${result.body()} code: ${result.code()}")
            } catch (e: Exception) {

                sendAmplitudeMessage(
                    "exception when send url request",
                    mapOf("exception_message" to e.message)
                )

                if (waitingJob?.isCompleted == false)
                    startAppState.postValue(StartAppStates.ShowApp)
                e.printStackTrace()
            }
        }
    }

    private fun sendShowPopupMessage(){
        GlobalScope.launch {
            try {
                val result = backendService.getPopupInfo(appsFlyerUID!!).body()!!
                if(result.result){
                    PrefsUtils.setPopupInfo(result)
                    Log.d("DEFERRED_DEEP_LINK", "popup info was received")
                    sendAmplitudeMessage(
                        "popup info was received", mapOf(
                            "popup_text" to result.popupText,
                            "switch_text" to result.switchText,
                            "error_text" to result.errorText
                        )
                    )
                }else{
                    Log.d("DEFERRED_DEEP_LINK", "popup was rejected")
                    sendAmplitudeMessage("popup was rejected")
                }
            }catch (e: java.lang.Exception){
                sendAmplitudeMessage(
                    "exception when get popup info",
                    mapOf("exception_message" to e.message)
                )
                e.printStackTrace()
            }
        }
    }

    private fun getAdvertisingId(){
        GlobalScope.launch {
            try {
                val idInfo = AdvertisingIdClient.getAdvertisingIdInfo(applicationContext)
                advertisingId.set(idInfo?.id)
                idInfo?.id?.let { PrefsUtils.setAdvertisingId(it) }

                Log.d("DEFERRED_DEEP_LINK", "advertising id is ${advertisingId.get()}")
                sendAmplitudeMessage(
                    "advertising id was received",
                    mapOf("advertising_id" to idInfo?.id)
                )

            } catch (e: java.lang.Exception) {

                sendAmplitudeMessage(
                    "exception when get advertising id",
                    mapOf("exception_message" to e.message)
                )

                advertisingId.set(null)
                e.printStackTrace()
            }
        }
    }

    fun startAppIfWaitingTimeOver(){
        waitingJob = GlobalScope.launch{
            delay(Const.TIMEOUT)
            startAppState.postValue(StartAppStates.ShowApp)

            sendAmplitudeMessage("timeout... open app", mapOf("seconds" to Const.TIMEOUT / 1000))

            Log.d("DEFERRED_DEEP_LINK", "waiting_time_is_over")
        }
    }

    private fun initAppsFlyer(appsFlyerId: String){
        AppsFlyerLib.getInstance().init(
            appsFlyerId,
            if (!PrefsUtils.linkIsCached()) getAppsFlyerConversionListener() else null,
            this
        )
        AppsFlyerLib.getInstance().startTracking(this)

        if(BuildConfig.DEBUG) {
            AppsFlyerLib.getInstance().setDebugLog(true)
            AppsFlyerLib.getInstance().setLogLevel(AFLogger.LogLevel.DEBUG)
        }
        appsFlyerUID = AppsFlyerLib.getInstance().getAppsFlyerUID(this)
        UserX.setUserId(appsFlyerUID)
        PrefsUtils.setAppsFlyerId(appsFlyerUID!!)

        sendAmplitudeMessage(
            "appsflyer was initialized", mapOf(
                "network_is_absent" to NetworkModule.connectionManager.isNetworkAbsent(),
                "appsflyer_dev_key" to appsFlyerId
            )
        )

        Log.d("DEFERRED_DEEP_LINK", "af_init_start")
    }

    private fun initFirebase(){
        FirebaseApp.initializeApp(this)
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {

                    sendAmplitudeMessage(
                        "firebase get instance id failed", mapOf(
                            "network_is_absent" to NetworkModule.connectionManager.isNetworkAbsent(),
                            "exception_message" to task.exception
                        )
                    )

                    Log.w("DEFERRED_DEEP_LINK", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                pushFuture.set(task.result?.token)
                task.result?.token?.let { PrefsUtils.setPushToken(it) }

                sendAmplitudeMessage(
                    "firebase was initialized",
                    mapOf("push_token" to task.result?.token)
                )

                Log.d("DEFERRED_DEEP_LINK", "firebase push token: ${task.result?.token}")
            })
    }

    private fun getAppsFlyerConversionListener() = object : AppsFlyerConversionListener {
        override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {}
        override fun onAttributionFailure(p0: String?) {}
        override fun onConversionDataSuccess(attrs: MutableMap<String, Any>) {

            if(!PrefsUtils.linkIsCached()) {
                PrefsUtils.cacheAppsFlyerAttributes(attrs)

                sendAmplitudeMessage("appsflyer conversion success", attrs)

                if (startAppState.value == StartAppStates.ShowSplashScreen) {
                    sendBackendMessage(attrs)
                }
                Log.d("DEFERRED_DEEP_LINK", "af_сonversion_data_success: $attrs")
            }
        }
        override fun onConversionDataFail(p0: String?) {
            startAppState.postValue(StartAppStates.ShowApp)

            sendAmplitudeMessage("conversion data fail", mapOf("message" to p0))
            Log.d("DEFERRED_DEEP_LINK", "af_сonversion_data_fail: $p0")
        }
    }

    fun stopWaitingTimer(){
        waitingJob?.cancel()
    }

    fun sendNotificationOpenEvent(pushType: String?){
        GlobalScope.launch {
            try {
                backendService.setNotificationOpenEvent(
                    PrefsUtils.getAppsFlyerId()!!,
                    notificationType = pushType
                )
                sendAmplitudeMessage("notification open")
                Log.d(
                    "DEFERRED_DEEP_LINK",
                    "send notification open intent, appsFlyerId: ${PrefsUtils.getAppsFlyerId()}"
                )
            } catch (e: java.lang.Exception) {
                sendAmplitudeMessage(
                    "exception when send notification open event",
                    mapOf("exception_message" to e.message)
                )
                e.printStackTrace()
            }
        }
    }

    fun sendPaymentPageOpenEvent(){
        GlobalScope.launch {
            try {
                backendService.setPaymentPageOpenEvent(PrefsUtils.getAppsFlyerId()!!)
                sendAmplitudeMessage("payment page open")
                Log.d(
                    "DEFERRED_DEEP_LINK",
                    "send payment page open, appsFlyerId: ${PrefsUtils.getAppsFlyerId()}"
                )
                AppsFlyerLib.getInstance().trackEvent(this@App, appsFlyerUID, emptyMap())
            }catch (e: java.lang.Exception) {
                sendAmplitudeMessage(
                    "exception when send payment page open event",
                    mapOf("exception_message" to e.message)
                )
                e.printStackTrace()
            }
        }
    }

    sealed class StartAppStates{
        object ShowSplashScreen: StartAppStates()
        object ShowApp: StartAppStates()
        class ShowWebView(val link: String?, val withSplashScreen: Boolean = false): StartAppStates()
    }


    private fun startAppActivity(){
        startActivity(
            Intent(
                this,
                this.getLauncherActivityName()
            ).apply {
                flags = (Intent.FLAG_ACTIVITY_NEW_TASK
                        or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
    }

    fun sendAmplitudeMessage(message: String, params: Map<String, Any?>? = null){

        if(Amplitude.getInstance() != null) {
            amplitudeLogsScope.launch {

                try {
                    val amplitudeClient = amplitudeClientFuture.get()

                    if(amplitudeClient != null) {
                        withContext(Dispatchers.Main) {
                            if (params.isNullOrEmpty()) {
                                amplitudeClient.logEvent(message)
                            } else {
                                amplitudeClient
                                    .logEvent(message, JSONObject().apply {
                                        params.forEach {
                                            put(it.key, it.value)
                                        }
                                    })
                            }
                        }
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
    }

}