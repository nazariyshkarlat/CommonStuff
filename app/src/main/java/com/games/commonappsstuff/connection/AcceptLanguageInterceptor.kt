package com.games.commonappsstuff.connection

import android.os.Build
import android.os.LocaleList
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.util.*


class AcceptLanguageInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()
        val requestWithHeaders: Request = originalRequest.newBuilder()
            .header("Accept-Language", language)
            .build()
        return chain.proceed(requestWithHeaders)
    }

    private val language: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList.getDefault().toLanguageTags()
        } else {
            Locale.getDefault().toLanguageTag()
        }
}