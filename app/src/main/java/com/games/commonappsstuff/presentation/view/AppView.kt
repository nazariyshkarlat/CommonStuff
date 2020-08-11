package com.games.commonappsstuff.presentation.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.KeyEvent
import android.webkit.*

@SuppressLint("SetJavaScriptEnabled")
class AppView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    init {

        this.settings.domStorageEnabled = true
        settings.javaScriptEnabled = true
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

        isFocusableInTouchMode = true
        requestFocus()
        setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_BACK -> {
                        if (this.canGoBack()) {
                            this.goBack()
                        } else {
                            (context as Activity).finish()
                        }
                        return@setOnKeyListener true
                    }
                }
            }
            return@setOnKeyListener false
        }
    }

}