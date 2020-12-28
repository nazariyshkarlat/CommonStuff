package com.games.commonappsstuff.presentation.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import com.games.commonappsstuff.presentation.fragment.AppFragment

@SuppressLint("SetJavaScriptEnabled")
class AppView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    init {

        settings.domStorageEnabled = true
        settings.javaScriptEnabled = true
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        settings.loadWithOverviewMode = true
        settings.userAgentString = System.getProperty("http.agent")
        settings.useWideViewPort = true
        settings.allowFileAccess = true
        settings.mixedContentMode = 0
        setLayerType(View.LAYER_TYPE_HARDWARE, null)

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

        isFocusableInTouchMode = true
        requestFocus()
        setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_BACK -> {
                        AppFragment.canGoBack = canGoBack()
                        if (this.canGoBack()) {
                            this.goBack()
                        } else {
                            (context as Activity).onBackPressed()
                        }
                        return@setOnKeyListener true
                    }
                }
            }
            return@setOnKeyListener false
        }

    }

}