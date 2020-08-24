package com.games.commonappsstuff.di

import android.app.Application

object DI {

    fun initialize(app: Application){
        NetworkModule.initialize(app)
        PrefsModule.initialize(app)
    }

}