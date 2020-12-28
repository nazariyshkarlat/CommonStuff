package com.games.commonappsstuff.utils

import com.games.commonappsstuff.connection.backend.PopupInfoEntity
import com.games.commonappsstuff.di.PrefsModule
import com.games.commonappsstuff.presentation.fragment.AppFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PrefsUtils {

    private const val APPS_FLYER_ID = "APPS_FLYER_ID"
    private const val APPS_FLYER_ATTRS = "APPS_FLYER_ATTRS"
    private const val FIREBASE_PUSH_TOKEN = "FIREBASE_PUSH_TOKEN"
    private const val POPUP_WAS_SHOWN = "POPUP_WAS_SHOWN"
    private const val FB_APP_ID = "FB_APP_ID"
    private const val POPUP_INFO = "POPUP_INFO"
    private const val AMPLITUDE_API_KEY = "AMPLITUDE_API_KEY"
    private const val ADVERTISING_ID = "ADVERTISING_ID"

    fun popupWasShown(): Boolean = PrefsModule.sharedPreferences.getBoolean(POPUP_WAS_SHOWN, false)

    fun linkIsCached(): Boolean = PrefsModule.sharedPreferences.contains(AppFragment.LINK)

    fun getLinkCache(): String? = PrefsModule.sharedPreferences.getString(AppFragment.LINK, null)

    fun getAdvertisingId(): String? = PrefsModule.sharedPreferences.getString(ADVERTISING_ID, null)

    fun getAppsFlyerId(): String? = PrefsModule.sharedPreferences.getString(APPS_FLYER_ID, null)

    fun getAmplitudeApiKey(): String? = PrefsModule.sharedPreferences.getString(AMPLITUDE_API_KEY, null)

    fun getFbAppId(): String? = PrefsModule.sharedPreferences.getString(FB_APP_ID, null)

    fun getPopupInfo() : PopupInfoEntity? {
        return PrefsModule.sharedPreferences.getString(POPUP_INFO, null)?.let{
            Gson().fromJson(it, PopupInfoEntity::class.java)
        }
    }

    fun setLinkCache(link: String) {
        PrefsModule.sharedPreferences.edit().putString(AppFragment.LINK, link).apply()
    }

    fun setAdvertisingId(advertisingId: String) {
        PrefsModule.sharedPreferences.edit().putString(ADVERTISING_ID, advertisingId).apply()
    }

    fun setPushToken(fbPushToken: String) {
        PrefsModule.sharedPreferences.edit().putString(FIREBASE_PUSH_TOKEN, fbPushToken).apply()
    }

    fun setPopupWasShown() {
        PrefsModule.sharedPreferences.edit().putBoolean(POPUP_WAS_SHOWN, true).apply()
    }

    fun setPopupInfo(popupInfoEntity: PopupInfoEntity) {
        PrefsModule.sharedPreferences.edit().putString(POPUP_INFO, Gson().toJson(popupInfoEntity)).apply()
    }

    fun setAppsFlyerId(appsFlyerId: String) {
        PrefsModule.sharedPreferences.edit().putString(APPS_FLYER_ID, appsFlyerId).apply()
    }

    fun setAmplitudeApiKey(amplitudeApiKey: String) {
        PrefsModule.sharedPreferences.edit().putString(AMPLITUDE_API_KEY, amplitudeApiKey).apply()
    }

    fun setFbAppId(id: String?){
        PrefsModule.sharedPreferences.edit().putString(FB_APP_ID, id).apply()
    }

    fun cacheAppsFlyerAttributes(attrs: MutableMap<String, Any>){
        PrefsModule.sharedPreferences.edit().putString(APPS_FLYER_ATTRS, Gson().toJson(attrs)).apply()
    }

    fun getAppsFlyerAttributes() = PrefsModule.sharedPreferences.getString(APPS_FLYER_ATTRS, null)?.let{
        Gson().fromJson<Map<String, Any>>(it, object : TypeToken<Map<String, Any>>(){}.type)
    }
}