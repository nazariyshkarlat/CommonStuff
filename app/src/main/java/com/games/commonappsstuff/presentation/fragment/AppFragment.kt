package com.games.commonappsstuff.presentation.fragment

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.appsflyer.AppsFlyerLib
import com.games.commonappsstuff.App
import com.games.commonappsstuff.R
import com.games.commonappsstuff.di.NetworkModule
import com.games.commonappsstuff.presentation.fragment.base.BaseFragment
import com.games.commonappsstuff.utils.ViewUtils.showPopup
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.app_view_layout.*
import pro.userx.UserX


@SuppressLint("SetJavaScriptEnabled")
class AppFragment : BaseFragment(R.layout.app_view_layout){

    companion object{
        const val LINK = "WEB_VIEW_LINK"
        var canGoBack = false
    }

    private var mUploadMessage: ValueCallback<Uri>? = null
    private val READ_STORAGE = 3214
    private val REQUEST_SELECT_FILE = 100
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var fileChooserParams: WebChromeClient.FileChooserParams? = null
    private var uploadMessage: ValueCallback<Array<Uri>>? = null
    private var link : String? = null
    private var lastLink: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        link = arguments?.getString(LINK)

        Log.d(LINK, arguments?.getString(LINK).toString())

        UserX.setWebView(appView)

        canGoBack = false

        appView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                (activity!!.application as App).sendAmplitudeMessage("webview url load finished", mapOf("url" to url))

                if(appView != null) {

                    if(lastLink == null){
                        FirebaseAnalytics.getInstance(activity!!).logEvent("start_webview", null)
                        AppsFlyerLib.getInstance()?.trackEvent(activity!!, "start_webview", null)
                        UserX.addScreenName(AppFragment::class.java, "Webview")
                    }

                    checkAndShowPopup(url.toString())
                    (activity!!.application as App).stopWaitingTimer()
                    appView.visibility = View.VISIBLE
                    canGoBack = appView.canGoBack()
                    removeSplashScreen()
                    lastLink = url
                }

            }

            @SuppressWarnings("deprecation")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                return when {
                    url.startsWith("mailto:") -> {
                        startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(url)))
                        return true
                    }
                    url.startsWith("tel:") -> {
                        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
                        return true
                    }
                    url.startsWith("https://pay.paymentsllc.com/pay/") -> {
                        sendOpenPaymentPageEvent()
                        return false
                    }
                    else -> {
                        super.shouldOverrideUrlLoading(view, url)
                    }
                }
            }

            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val uri = request.url
                return when {
                    uri.toString().startsWith("mailto:") -> {
                        startActivity(Intent(Intent.ACTION_SENDTO, uri))
                        true
                    }
                    uri.toString().startsWith("tel:") -> {
                        startActivity(Intent(Intent.ACTION_DIAL, uri))
                        true
                    }
                    uri.toString().startsWith("https://pay.paymentsllc.com/pay/") -> {
                        UserX.stopScreenRecording()
                        sendOpenPaymentPageEvent()
                        return false
                    }
                    else -> {
                        super.shouldOverrideUrlLoading(view, request)
                    }
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                if (NetworkModule.connectionManager.isNetworkAbsent()) {
                    (activity!!.application as App).startAppState.value = App.StartAppStates.ShowApp
                }
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                if (NetworkModule.connectionManager.isNetworkAbsent()) {
                    (activity!!.application as App).startAppState.value = App.StartAppStates.ShowApp
                }
            }
        }

        appView.webChromeClient = object : WebChromeClient(){
            override fun onShowFileChooser(
                mWebView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams
            ): Boolean {
                this@AppFragment.filePathCallback = filePathCallback
                this@AppFragment.fileChooserParams = fileChooserParams
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ActivityCompat.checkSelfPermission(
                            context!!,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        return showFilePicker()
                    } else {
                        requestPermissions(
                            listOf(android.Manifest.permission.READ_EXTERNAL_STORAGE).toTypedArray(),
                            READ_STORAGE
                        )
                    }
                }else{
                    return showFilePicker()
                }
                return true
            }
        }

        link?.let {
            appView.loadUrl(it)
        }
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        intent: Intent?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null) return
                uploadMessage?.onReceiveValue(
                    WebChromeClient.FileChooserParams.parseResult(
                        resultCode,
                        intent
                    )
                )
                uploadMessage = null
            }
        } else if (requestCode == REQUEST_SELECT_FILE) {
            if (null == mUploadMessage) return
            val result =
                if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
            mUploadMessage?.onReceiveValue(result)
            mUploadMessage = null
        } else Toast.makeText(
            context!!,
            "Failed to Upload Image",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED) && (requestCode == READ_STORAGE)){
            showFilePicker()
        }
    }

    private fun showFilePicker(): Boolean{
        if (uploadMessage != null) {
            uploadMessage!!.onReceiveValue(null)
            uploadMessage = null
        }

        uploadMessage = filePathCallback

        try {
            startActivityForResult(
                fileChooserParams?.createIntent(),
                REQUEST_SELECT_FILE
            )        } catch (e: ActivityNotFoundException) {
            uploadMessage = null
            Toast.makeText(
                activity!!.applicationContext,
                "Cannot Open File Chooser",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return true
    }

    private fun removeSplashScreen(){
        (activity as AppCompatActivity).supportFragmentManager.apply {
            findFragmentByTag(SplashScreenFragment::class.java.name)?.let {
                beginTransaction()
                    .remove(
                        it
                    ).commit()
            }
        }
    }


    private fun sendOpenPaymentPageEvent(){
        (activity!!.application as App).sendPaymentPageOpenEvent()
    }


    private fun checkAndShowPopup(link: String){
        if(CookieManager.getInstance().getCookie(link).contains("_gat_auth=1")) {
            (activity as? AppCompatActivity)?.showPopup()
        }
    }


}