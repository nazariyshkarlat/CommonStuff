package com.games.commonappsstuff.connection

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class UserAgentInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()
        val requestWithUserAgent: Request = originalRequest.newBuilder()
            .header("User-Agent", System.getProperty("http.agent")!!)
            .build()
        return chain.proceed(requestWithUserAgent)
    }

}