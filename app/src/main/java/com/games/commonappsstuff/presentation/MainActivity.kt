package com.games.commonappsstuff.presentation

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.appsflyer.AppsFlyerLib
import com.games.commonappsstuff.BuildConfig
import com.games.commonappsstuff.R
import com.games.commonappsstuff.di.ViewModelModule
import com.games.commonappsstuff.ext.addFragment
import com.games.commonappsstuff.presentation.fragment.AppFragment
import com.games.commonappsstuff.presentation.fragment.SplashScreenFragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging

abstract class MainActivity : AppCompatActivity() {

    abstract val startAppCallback: StartAppCallback
    abstract val isTest: Boolean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_fragment_container)

        val viewModel = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel?> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return (ViewModelModule.viewModel).apply {
                    isTest = if(BuildConfig.DEBUG) this@MainActivity.isTest else false
                } as T
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
                    val currentFragment = supportFragmentManager.findFragmentById(R.id.mainFrameLayout)
                    if(currentFragment is AppFragment){
                        supportFragmentManager.beginTransaction().remove(currentFragment).commitNow()
                    }
                    startAppCallback.onShowApp()
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
                        setTargetFragment(this@MainActivity.supportFragmentManager.findFragmentById(R.id.mainFrameLayout), 213)
                        arguments = bundleOf(AppFragment.LINK to state.link)
                    }, R.id.mainFrameLayout)
                    FirebaseAnalytics.getInstance(this).logEvent("start_webview", null)
                    AppsFlyerLib.getInstance().trackEvent(this,  "start_webview", null)
                    Log.d("DEFERRED_DEEP_LINK", "start_webview")
                }
            }
        })

    }

    interface StartAppCallback{
        fun onShowApp()
    }

}