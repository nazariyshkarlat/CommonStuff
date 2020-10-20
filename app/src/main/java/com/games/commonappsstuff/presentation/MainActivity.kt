package com.games.commonappsstuff.presentation

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.amplitude.api.Amplitude
import com.appsflyer.AppsFlyerLib
import com.appsflyer.AppsFlyerLibCore
import com.games.commonappsstuff.App
import com.games.commonappsstuff.BuildConfig
import com.games.commonappsstuff.Const
import com.games.commonappsstuff.R
import com.games.commonappsstuff.connection.backend.BackendService
import com.games.commonappsstuff.di.NetworkModule
import com.games.commonappsstuff.messaging.FirebaseMessagingService
import com.games.commonappsstuff.presentation.fragment.AppFragment
import com.games.commonappsstuff.presentation.fragment.AppFragment.Companion.canGoBack
import com.games.commonappsstuff.presentation.fragment.SplashScreenFragment
import com.games.commonappsstuff.utils.ViewUtils.showPopup
import com.games.commonappsstuff.utils.addFragment
import com.games.commonappsstuff.utils.getLauncherActivityName
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.main_fragment_container.*
import org.json.JSONObject
import pro.userx.UserX


abstract class MainActivity : AppCompatActivity() {

    abstract val startAppCallback: StartAppCallback
    abstract val isTest: Boolean

    private var doubleBackToExitPressedOnce = false

    companion object {
        const val NOTIFICATIONS_REQUEST_CODE = 7534
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_fragment_container)

        val viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel?> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return (ViewModel(application,
                    NetworkModule.getService(BackendService::class.java),
                    NetworkModule.connectionManager).apply {
                    isTest = if(BuildConfig.DEBUG) this@MainActivity.isTest else false
                } as T)
            }
        })[ViewModel::class.java]

        viewModel.startAppState.observe(this, Observer {
            when (val state = it) {
                is ViewModel.StartAppStates.ShowSplashScreen -> {
                    viewModel.startAppIfWaitingTimeOver()
                    addFragment(
                        SplashScreenFragment(),
                        R.id.mainFrameLayout
                    )

                    App.sendAmplitudeMessage("show splash screen")

                    UserX.addScreenName(SplashScreenFragment::class.java, "Splash Screen")
                }
                is ViewModel.StartAppStates.ShowApp -> {

                    viewModel.stopWaitingTimer()
                    val currentFragment = supportFragmentManager.findFragmentById(R.id.mainFrameLayout)
                    if(currentFragment is AppFragment){
                        supportFragmentManager.beginTransaction().remove(currentFragment).commitNow()
                    }
                    startAppCallback.onShowApp()

                    App.sendAmplitudeMessage("start game")

                    UserX.stopScreenRecording()
                    Log.d("DEFERRED_DEEP_LINK", "start_game")
                }
                is ViewModel.StartAppStates.ShowWebView -> {

                    if(state.withSplashScreen){
                        viewModel.startAppIfWaitingTimeOver()
                        addFragment(
                            SplashScreenFragment(),
                            R.id.mainFrameLayout
                        )

                        App.sendAmplitudeMessage("show splash screen")

                        UserX.addScreenName(SplashScreenFragment::class.java, "Splash Screen")
                    }

                    addFragment(AppFragment().apply {
                        arguments = bundleOf(AppFragment.LINK to state.link)
                    }, R.id.mainFrameLayout)

                    App.sendAmplitudeMessage("webview loading start")
                    Log.d("DEFERRED_DEEP_LINK", "start_webview")
                }
            }
        })

        FirebaseMessagingService.popupState.observe(this, Observer{
            showPopup()
        })

        if(savedInstanceState == null){
            if (intent.getIntExtra("REQUEST_CODE", 0) == NOTIFICATIONS_REQUEST_CODE){
                viewModel.sendNotificationOpenEvent(intent.getStringExtra("push_type"))
            }
        }


    }


    override fun onBackPressed() {
        if(!canGoBack) {
            if (doubleBackToExitPressedOnce) {
                finishAffinity()
                return
            }
            doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
            Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        }else
            super.onBackPressed()
    }


    interface StartAppCallback{
        fun onShowApp()
    }

}