package com.games.commonappsstuff.presentation

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import com.games.commonappsstuff.App
import com.games.commonappsstuff.R
import com.games.commonappsstuff.messaging.FirebaseMessagingService
import com.games.commonappsstuff.presentation.fragment.AppFragment
import com.games.commonappsstuff.presentation.fragment.AppFragment.Companion.canGoBack
import com.games.commonappsstuff.presentation.fragment.SplashScreenFragment
import com.games.commonappsstuff.utils.ViewUtils.showPopup
import com.games.commonappsstuff.utils.addFragment
import pro.userx.UserX


abstract class MainActivity : AppCompatActivity() {

    abstract val startAppCallback: StartAppCallback

    private var doubleBackToExitPressedOnce = false

    companion object {
        const val NOTIFICATIONS_REQUEST_CODE = 7534
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_fragment_container)

        val app = application as App

        app.startAppState.observe(this, Observer {
            when (val state = it) {
                is App.StartAppStates.ShowSplashScreen -> {
                    app.startAppIfWaitingTimeOver()
                    addFragment(
                        SplashScreenFragment(),
                        R.id.mainFrameLayout
                    )

                    app.sendAmplitudeMessage("show splash screen")

                    UserX.addScreenName(SplashScreenFragment::class.java, "Splash Screen")
                }
                is App.StartAppStates.ShowApp -> {

                    app.stopWaitingTimer()
                    val currentFragment = supportFragmentManager.findFragmentById(R.id.mainFrameLayout)
                    if(currentFragment is AppFragment){
                        supportFragmentManager.beginTransaction().remove(currentFragment).commitNow()
                    }
                    startAppCallback.onShowApp()

                    app.sendAmplitudeMessage("start game")

                    UserX.stopScreenRecording()
                    Log.d("DEFERRED_DEEP_LINK", "start_game")
                }
                is App.StartAppStates.ShowWebView -> {

                    if(state.withSplashScreen){
                        app.startAppIfWaitingTimeOver()
                        addFragment(
                            SplashScreenFragment(),
                            R.id.mainFrameLayout
                        )

                        app.sendAmplitudeMessage("show splash screen")

                        UserX.addScreenName(SplashScreenFragment::class.java, "Splash Screen")
                    }

                    addFragment(AppFragment().apply {
                        arguments = bundleOf(AppFragment.LINK to state.link)
                    }, R.id.mainFrameLayout)

                    app.sendAmplitudeMessage("webview loading start")
                    Log.d("DEFERRED_DEEP_LINK", "start_webview")
                }
            }
        })

        FirebaseMessagingService.popupState.observe(this, Observer{
            showPopup()
        })

        if(savedInstanceState == null){
            if (intent.getIntExtra("REQUEST_CODE", 0) == NOTIFICATIONS_REQUEST_CODE){
                app.sendNotificationOpenEvent(intent.getStringExtra("push_type"))
            }
        }


    }


    override fun onBackPressed() {
        println("CAN GO BACK $canGoBack " +
                "PRESSED ONCE $doubleBackToExitPressedOnce")
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