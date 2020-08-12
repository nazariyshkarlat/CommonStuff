package com.games.commonappsstuff.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

object PrefsModule {
    private const val PREFS_NAME = "app_cache"
    lateinit var sharedPreferences: SharedPreferences


    fun initialize(app: Application) {
        sharedPreferences = app.getSharedPreferences(
            PREFS_NAME, Context.MODE_PRIVATE
        )
    }

}
