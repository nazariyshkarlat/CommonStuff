package com.games.commonappsstuff.presentation

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.games.commonappsstuff.Const
import com.games.commonappsstuff.PrefsUtils
import com.games.commonappsstuff.connection.ConnectionManager
import com.games.commonappsstuff.connection.backend.BackendService
import com.games.commonappsstuff.di.PrefsModule
import com.google.android.gms.tasks.OnCompleteListener
import com.google.common.util.concurrent.SettableFuture
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.*

class ViewModel(private val application: Application, private val backendService: BackendService, private val connectionManager: ConnectionManager) : ViewModel() {

    private var pushFuture: SettableFuture<String?> = SettableFuture.create()
    val startAppState = SingleLiveEvent<StartAppStates>()
    var isTest = false

    private var appsFlyerUID: String? = null

     init{
        val networkIsAbsent = connectionManager.isNetworkAbsent()

         initFirebase()
         initAppsFlyer(if(!PrefsUtils.linkIsCached() && !networkIsAbsent) getAppsFlyerConversionListener() else null)

         startAppState.value =when {
             networkIsAbsent -> StartAppStates.ShowApp
             PrefsUtils.linkIsCached() -> StartAppStates.ShowWebView(PrefsUtils.getLinkCache(), true)
             else -> StartAppStates.ShowSplashScreen
         }
    }

    private fun sendBackendMessage(attrs: MutableMap<String, Any>) {
        viewModelScope.launch {
            try {
                val result = backendService.sendFirstOpenMessage(
                    appsFlyerUID!!,
                    if(isTest) "test.bundle.com" else application.packageName,
                    pushFuture.get()!!,
                    attrs["campaign_name"] as String?,
                    attrs["campaign_id"] as String?,
                    attrs["adset_name"] as String?,
                    attrs["adset_id"] as String?,
                    attrs["adgroup_name"] as String?,
                    attrs["adgroup_id"] as String?,
                    attrs["af_channel"] as String?,
                    attrs["user_country"] as String?
                    )
                if (result.code() == 200){
                    if(!isTest)
                        PrefsUtils.setLinkCache(result.body()!!.message)
                    startAppState.postValue(StartAppStates.ShowWebView(result.body()!!.message))
                }else{
                    startAppState.postValue(StartAppStates.ShowApp)
                }
                Log.d("DEFERRED_DEEP_LINK", "result: ${result.body()} code: ${result.code()}")
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }


    fun startAppIfWaitingTimeOver(){
        viewModelScope.launch{
            delay(Const.TIMEOUT)
            if(startAppState.value == StartAppStates.ShowSplashScreen || (startAppState.value is StartAppStates.ShowWebView && (startAppState.value as StartAppStates.ShowWebView).withSplashScreen)) {
                startAppState.postValue(StartAppStates.ShowApp)
                Log.d("DEFERRED_DEEP_LINK", "waiting_time_is_over")
            }
        }
    }

    private fun initAppsFlyer(conversionListener: AppsFlyerConversionListener?){
        AppsFlyerLib.getInstance().init(Const.APPSFLYER_DEV_KEY, conversionListener, application.applicationContext)
        AppsFlyerLib.getInstance().startTracking(application)
        appsFlyerUID = AppsFlyerLib.getInstance().getAppsFlyerUID(application)
        PrefsUtils.setAppsFlyerId(appsFlyerUID!!)
        Log.d("DEFERRED_DEEP_LINK", "af_init_start")
    }

    private fun initFirebase(){
        FirebaseApp.initializeApp(application)
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("DEFERRED_DEEP_LINK", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                pushFuture.set(task.result?.token)
                Log.d("DEFERRED_DEEP_LINK", "firebase push token: ${task.result?.token}")
            })
    }

    fun sendNotificationOpenEvent(){
        viewModelScope.launch {
            backendService.setNotificationOpenEvent(PrefsUtils.getAppsFlyerId()!!)
            Log.d("DEFERRED_DEEP_LINK", "send notification open intent, appsFlyerId: ${PrefsUtils.getAppsFlyerId()}")
        }
    }

    private fun getAppsFlyerConversionListener() = object : AppsFlyerConversionListener {
        override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {}
        override fun onAttributionFailure(p0: String?) {}
        override fun onConversionDataSuccess(attrs: MutableMap<String, Any>) {
            if(startAppState.value == StartAppStates.ShowSplashScreen) {
                sendBackendMessage(attrs)
                Log.d("DEFERRED_DEEP_LINK", "af_сonversion_data_success: $attrs")
            }
        }
        override fun onConversionDataFail(p0: String?) {
            startAppState.postValue(StartAppStates.ShowApp)
            Log.d("DEFERRED_DEEP_LINK", "af_сonversion_data_fail: $p0")
        }
    }

    sealed class StartAppStates{
        object ShowSplashScreen: StartAppStates()
        object ShowApp: StartAppStates()
        class ShowWebView(val link: String?, val withSplashScreen: Boolean = false): StartAppStates()
    }

}