package com.games.commonappsstuff.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import com.appsflyer.AppsFlyerLib
import com.games.commonappsstuff.R
import com.games.commonappsstuff.ext.addFragment
import com.games.commonappsstuff.presentation.fragment.AppFragment
import com.games.commonappsstuff.presentation.fragment.SplashScreenFragment

abstract class MainActivity : AppCompatActivity() {

    abstract val viewModel: ViewModel
    abstract val startAppCallback: StartAppCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel.startAppState.observe(this, Observer {
            when (val state = it) {
                is ViewModel.StartAppStates.ShowSplashScreen -> addFragment(
                    SplashScreenFragment(),
                    R.id.mainFrameLayout
                )
                is ViewModel.StartAppStates.ShowApp -> {
                    startAppCallback.onShowApp()
                    AppsFlyerLib.getInstance().trackEvent(this, "start_game", null)
                    Log.d("DEFERRED_DEEP_LINK", "start_game")
                }
                is ViewModel.StartAppStates.ShowWebView -> {

                    if(state.withSplashScreen){
                        addFragment(
                            SplashScreenFragment(),
                            R.id.mainFrameLayout
                        )
                    }

                    addFragment(AppFragment().apply {
                        setTargetFragment(this@MainActivity.supportFragmentManager.findFragmentById(R.id.mainFrameLayout), 213)
                        arguments = bundleOf(AppFragment.LINK to state.link)
                    }, R.id.mainFrameLayout)
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