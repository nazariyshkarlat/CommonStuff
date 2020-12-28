package com.games.commonappsstuff.di

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import com.games.commonappsstuff.BuildConfig
import com.games.commonappsstuff.connection.*
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    private const val BASE_URL = "https://plagoo.cf"

    lateinit var connectionManager: ConnectionManager
    private lateinit var retrofit: Retrofit

    lateinit var context: Context

    fun initialize(app: Application) {
        context = app
        connectionManager =
            ConnectionManagerImpl(
                getConnectivityManager(app)
            )
        retrofit =
            getRetrofit(
                getOkHttpClient(
                    getInterceptor(),
                    UserAgentInterceptor(),
                    AcceptLanguageInterceptor()
                )
            )
    }

    fun <T> getService(className: Class<T>): T = retrofit.create(className)

    private fun getConnectivityManager(context: Context) =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

    private fun getRetrofit(client: OkHttpClient) =
        Retrofit.Builder()
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .baseUrl(BASE_URL)
            .build()

    private fun getOkHttpClient(loggingInterceptor: okhttp3.Interceptor, userAgentInterceptor: okhttp3.Interceptor, acceptLanguageInterceptor: Interceptor) =
        OkHttpClient().newBuilder()
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(acceptLanguageInterceptor)
            .addInterceptor(loggingInterceptor)
            .readTimeout(1, TimeUnit.MINUTES)
            .connectTimeout(2, TimeUnit.SECONDS)
            .build()


    private fun getInterceptor(): okhttp3.Interceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG)
                HttpLoggingInterceptor.Level.BASIC
            else
                HttpLoggingInterceptor.Level.NONE
        }
}