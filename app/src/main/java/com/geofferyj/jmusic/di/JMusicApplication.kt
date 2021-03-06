package com.geofferyj.jmusic.di

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class JMusicApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
        startKoin {
            androidContext(this@JMusicApplication)
            modules(listOf(appModule, serviceModule))
        }
    }
}