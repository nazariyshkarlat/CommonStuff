package com.games.commonappsstuff.utils

import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity


fun Application.getLauncherActivityName(): Class<AppCompatActivity> {
    val intent = packageManager.getLaunchIntentForPackage(packageName)
    val activityList = packageManager.queryIntentActivities(intent!!, 0)
    return Class.forName(activityList[0].activityInfo.name) as Class<AppCompatActivity>
}