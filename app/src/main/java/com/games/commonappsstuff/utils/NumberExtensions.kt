package com.games.commonappsstuff.utils

import android.content.res.Resources
import java.math.BigDecimal

fun Int.pxToDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()
fun Float.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
fun Int.pxToSp(): Int = (this / Resources.getSystem().displayMetrics.scaledDensity).toInt()
fun Float.spToPx(): Int = (this * Resources.getSystem().displayMetrics.scaledDensity).toInt()

