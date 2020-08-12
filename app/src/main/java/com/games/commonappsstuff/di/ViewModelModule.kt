package com.games.commonappsstuff.di

import android.app.Application
import com.games.commonappsstuff.connection.backend.PostService
import com.games.commonappsstuff.presentation.ViewModel

object ViewModelModule {
    lateinit var viewModel: ViewModel

    fun initialize(app: Application){
        viewModel = ViewModel(app,
            NetworkModule.getService(PostService::class.java),
            NetworkModule.connectionManager,
            PrefsModule.sharedPreferences)
    }

}