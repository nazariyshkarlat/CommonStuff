package com.games.commonappsstuff.presentation

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.games.commonappsstuff.BuildConfig
import com.games.commonappsstuff.Const
import com.games.commonappsstuff.PrefsUtils
import com.games.commonappsstuff.connection.ConnectionManager
import com.games.commonappsstuff.connection.backend.PostService
import com.games.commonappsstuff.presentation.SingleLiveEvent
import com.google.android.gms.tasks.OnCompleteListener
import com.google.common.util.concurrent.SettableFuture
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.coroutines.*

class ViewModel(private val application: Application, private val postService: PostService, private val connectionManager: ConnectionManager, private val preferences: SharedPreferences) : ViewModel() {

    private var pushFuture: SettableFuture<String?> = SettableFuture.create()
    val startAppState = SingleLiveEvent<StartAppStates>()

    private var job: Job? = null

    private var appsFlyerUID: String? = null

     init{
        val networkIsAbsent = connectionManager.isNetworkAbsent()
         startAppState.value =when {
             networkIsAbsent -> StartAppStates.ShowApp
             PrefsUtils.linkIsCached(preferences) -> StartAppStates.ShowWebView(PrefsUtils.getLinkCache(preferences), true)
             else -> StartAppStates.ShowSplashScreen
         }

        if(!networkIsAbsent) {
            if (!PrefsUtils.linkIsCached(preferences)) {
                initFirebase()
                initAppsFlyer(getAppsFlyerConversionListener())
            }
        }
    }

    private fun sendBackendMessage(fbclId: String?) {
        viewModelScope.launch {
            try {
                val result = postService.sendMessage(appsFlyerUID!!, "test.bundle.com", fbclId, pushFuture.get()!!)
                if (result.code() == 200){
                    PrefsUtils.setLinkCache(result.body()!!.message, preferences)
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


    private fun startAppIfWaitingTimeOver(){
        job = viewModelScope.launch{
            delay(Const.TIMEOUT)
            if(startAppState.value == StartAppStates.ShowSplashScreen) {
                startAppState.postValue(StartAppStates.ShowApp)
                Log.d("DEFERRED_DEEP_LINK", "waiting_time_is_over")
            }
        }
    }

    private fun initAppsFlyer(conversionListener: AppsFlyerConversionListener){
        AppsFlyerLib.getInstance().init(Const.APPSFLYER_DEV_KEY, conversionListener, application.applicationContext)
        AppsFlyerLib.getInstance().startTracking(application)
        appsFlyerUID = AppsFlyerLib.getInstance().getAppsFlyerUID(application)
        Log.d("DEFERRED_DEEP_LINK", "af_init_start")
        startAppIfWaitingTimeOver()
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

    private fun getAppsFlyerConversionListener() = object : AppsFlyerConversionListener {
        override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {}
        override fun onAttributionFailure(p0: String?) {}
        override fun onConversionDataSuccess(attrs: MutableMap<String, Any>) {
            if(startAppState.value == StartAppStates.ShowSplashScreen) {
                sendBackendMessage(attrs["fbclid"] as String?)
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