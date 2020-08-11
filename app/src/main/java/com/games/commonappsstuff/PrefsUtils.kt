package com.games.commonappsstuff

import android.content.SharedPreferences
import com.games.commonappsstuff.presentation.fragment.AppFragment

object PrefsUtils {

    fun linkIsCached(preferences: SharedPreferences): Boolean = preferences.contains(AppFragment.LINK)

    fun getLinkCache(preferences: SharedPreferences): String? = preferences.getString(AppFragment.LINK, null)

    fun setLinkCache(link: String, preferences: SharedPreferences) {
        preferences.edit().putString(AppFragment.LINK, link).apply()
    }
}