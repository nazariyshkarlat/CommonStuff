package com.games.commonappsstuff

import android.app.Application
import com.games.commonappsstuff.di.DI

open class App : Application(){

    override fun onCreate() {
        super.onCreate()
        DI.initialize(this)
    }

}