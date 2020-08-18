package com.games.commonappsstuff.presentation.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.games.commonappsstuff.R
import com.games.commonappsstuff.presentation.fragment.base.BaseFragment
import kotlinx.android.synthetic.main.app_view_layout.*


@SuppressLint("SetJavaScriptEnabled")
class AppFragment : BaseFragment(R.layout.app_view_layout){

    companion object{
        const val LINK = "WEB_VIEW_LINK"
    }

    private var link : String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        link = arguments?.getString(LINK)

        Log.d(LINK, arguments?.getString(LINK).toString())

        appView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                appView.visibility = View.VISIBLE
                targetFragment?.let {
                    (activity as AppCompatActivity).supportFragmentManager.beginTransaction()
                        .remove(
                            it
                        ).commit()
                }
            }
        }

        link?.let {
            appView.loadUrl(it)
        }
    }


}