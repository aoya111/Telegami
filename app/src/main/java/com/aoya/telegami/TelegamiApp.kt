package com.aoya.telegami

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.aoya.telegami.service.PrefManager

lateinit var telegamiApp: TelegamiApp

class TelegamiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        telegamiApp = this
        PrefManager.init()

        runCatching {
            AppCompatDelegate.setDefaultNightMode(PrefManager.darkTheme)
        }
    }
}
