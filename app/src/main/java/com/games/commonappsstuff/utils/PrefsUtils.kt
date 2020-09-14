package com.games.commonappsstuff.utils

import com.games.commonappsstuff.di.PrefsModule
import com.games.commonappsstuff.presentation.fragment.AppFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PrefsUtils {

    private const val APPS_FLYER_ID = "APPS_FLYER_ID"
    private const val APPS_FLYER_ATTRS = "APPS_FLYER_ATTRS"
    private const val FIREBASE_PUSH_TOKEN = "FIREBASE_PUSH_TOKEN"

    fun linkIsCached(): Boolean = PrefsModule.sharedPreferences.contains(AppFragment.LINK)


    fun getLinkCache(): String? = PrefsModule.sharedPreferences.getString(AppFragment.LINK, null)

    fun getAppsFlyerId(): String? = PrefsModule.sharedPreferences.getString(APPS_FLYER_ID, null)

    fun setLinkCache(link: String) {
        PrefsModule.sharedPreferences.edit().putString(AppFragment.LINK, link).apply()
    }

    fun setPushToken(fbPushToken: String) {
        PrefsModule.sharedPreferences.edit().putString(FIREBASE_PUSH_TOKEN, fbPushToken).apply()
    }

    fun setAppsFlyerId(appsFlyerId: String) {
        PrefsModule.sharedPreferences.edit().putString(APPS_FLYER_ID, appsFlyerId).apply()
    }

    fun cacheAppsFlyerAttributes(attrs: MutableMap<String, Any>){
        PrefsModule.sharedPreferences.edit().putString(APPS_FLYER_ATTRS, Gson().toJson(attrs)).apply()
    }

    fun getAppsFlyerAttributes() = PrefsModule.sharedPreferences.getString(APPS_FLYER_ATTRS, null)?.let{
        Gson().fromJson<Map<String, Any>>(it, object : TypeToken<Map<String, Any>>(){}.type)
    }
}