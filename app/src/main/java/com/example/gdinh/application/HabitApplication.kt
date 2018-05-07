package com.example.gdinh.application

import android.app.Application

import com.example.gdinh.analytics.HabitAnalytics
import com.example.gdinh.sync.FirebaseSyncUtils

class HabitApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        HabitAnalytics.initAnalytics(this)
        HabitAnalytics.logAppOpen()

        FirebaseSyncUtils.setOfflineModeEnabled(true)
    }
}
