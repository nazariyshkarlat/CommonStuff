package com.games.commonappsstuff.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.appsflyer.AppsFlyerLib
import com.games.commonappsstuff.BuildConfig
import com.games.commonappsstuff.Const
import com.games.commonappsstuff.R
import com.games.commonappsstuff.connection.backend.BackendService
import com.games.commonappsstuff.di.NetworkModule
import com.games.commonappsstuff.presentation.fragment.AppFragment
import com.games.commonappsstuff.presentation.fragment.AppFragment.Companion.canGoBack
import com.games.commonappsstuff.presentation.fragment.SplashScreenFragment
import com.games.commonappsstuff.utils.addFragment
import com.games.commonappsstuff.utils.getLauncherActivityName
import com.google.firebase.analytics.FirebaseAnalytics


abstract class MainActivity : AppCompatActivity() {

    abstract val startAppCallback: StartAppCallback
    abstract val appsFlyerDevKey: String
    abstract val isTest: Boolean

    companion object {
        const val NOTIFICATIONS_REQUEST_CODE = 7534
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_fragment_container)

        val viewModel = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel?> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return (ViewModel(application,
                    NetworkModule.getService(BackendService::class.java),
                    NetworkModule.connectionManager,
                    appsFlyerDevKey).apply {
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
                }
                is ViewModel.StartAppStates.ShowApp -> {

                    viewModel.stopWaitingTimer()
                    val currentFragment = supportFragmentManager.findFragmentById(R.id.mainFrameLayout)
                    if(currentFragment is AppFragment){
                        supportFragmentManager.beginTransaction().remove(currentFragment).commitNow()
                    }
                    startAppCallback.onShowApp()
                    AppsFlyerLib.getInstance().init(appsFlyerDevKey, null, application.applicationContext)
                    AppsFlyerLib.getInstance().trackEvent(this, "start_game", null)
                    Log.d("DEFERRED_DEEP_LINK", "start_game")
                }
                is ViewModel.StartAppStates.ShowWebView -> {

                    if(state.withSplashScreen){
                        viewModel.startAppIfWaitingTimeOver()
                        addFragment(
                            SplashScreenFragment(),
                            R.id.mainFrameLayout
                        )
                    }
                    addFragment(AppFragment().apply {
                        arguments = bundleOf(AppFragment.LINK to state.link)
                    }, R.id.mainFrameLayout)
                    FirebaseAnalytics.getInstance(this).logEvent("start_webview", null)
                    AppsFlyerLib.getInstance().trackEvent(this,  "start_webview", null)
                    Log.d("DEFERRED_DEEP_LINK", "start_webview")
                }
            }
        })

        if(savedInstanceState == null){
            if (intent.getIntExtra("REQUEST_CODE", 0) == NOTIFICATIONS_REQUEST_CODE){
                viewModel.sendNotificationOpenEvent()
            }
        }

    }

    override fun onBackPressed() {
        if(!canGoBack)
            finishAffinity()
        else
            super.onBackPressed()
    }


    interface StartAppCallback{
        fun onShowApp()
    }

}