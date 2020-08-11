package com.games.commonappsstuff.connection

import android.net.ConnectivityManager
import com.games.commonappsstuff.connection.ConnectionManager

class ConnectionManagerImpl(private val connectivityManager: ConnectivityManager?) :
    ConnectionManager {

    override fun isNetworkAbsent(): Boolean {
        val netInfo = connectivityManager?.activeNetworkInfo
        return netInfo == null || !netInfo.isConnected
    }
}