package com.games.commonappsstuff

import android.content.SharedPreferences
import com.games.commonappsstuff.di.PrefsModule
import com.games.commonappsstuff.presentation.fragment.AppFragment

object PrefsUtils {

    private const val APPS_FLYER_ID = "APPS_FLYER_ID"

    fun linkIsCached(): Boolean = PrefsModule.sharedPreferences.contains(AppFragment.LINK)

    fun appsFlyerIdIsCached(): Boolean = PrefsModule.sharedPreferences.contains(APPS_FLYER_ID)

    fun getLinkCache(): String? = PrefsModule.sharedPreferences.getString(AppFragment.LINK, null)

    fun getAppsFlyerId(): String? = PrefsModule.sharedPreferences.getString(APPS_FLYER_ID, null)

    fun setLinkCache(link: String) {
        PrefsModule.sharedPreferences.edit().putString(AppFragment.LINK, link).apply()
    }

    fun setAppsFlyerId(appsFlyerId: String) {
        PrefsModule.sharedPreferences.edit().putString(APPS_FLYER_ID, appsFlyerId).apply()
    }
}