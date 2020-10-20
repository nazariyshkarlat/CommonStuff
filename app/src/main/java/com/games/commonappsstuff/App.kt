package com.games.commonappsstuff

import android.app.Application
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import com.games.commonappsstuff.di.DI
import com.games.commonappsstuff.utils.PrefsUtils
import com.google.common.util.concurrent.SettableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import pro.userx.UserX
import java.lang.Exception

open class App : Application(){

    companion object{

        val amplitudeLogsScope = CoroutineScope(Dispatchers.Default)
        val amplitudeClientFuture: SettableFuture<AmplitudeClient> = SettableFuture.create()

        var popupWasShown = false

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
    override fun onCreate() {
        super.onCreate()
        popupWasShown = false
        DI.initialize(this)
    }

}