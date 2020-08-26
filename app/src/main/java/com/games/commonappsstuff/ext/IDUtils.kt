package com.games.commonappsstuff.ext

import java.text.SimpleDateFormat
import java.util.*

object IDUtils {

    fun createID(): Int {
        val now = Date()
        return SimpleDateFormat("ddHHmmssSS", Locale.US).format(now).toLong().toInt()
    }

}