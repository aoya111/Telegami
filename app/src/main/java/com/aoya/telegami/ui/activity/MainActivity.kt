package com.aoya.telegami.ui.activity

import android.content.res.Resources
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toDrawable
import com.aoya.telegami.R
import com.aoya.telegami.databinding.ActivityMainBinding
import com.aoya.telegami.service.PrefManager
import com.aoya.telegami.ui.util.ThemeUtils
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions

class MainActivity : AppCompatActivity() {
    var readyToKill: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        DynamicColors.applyToActivityIfAvailable(
            this,
            DynamicColorsOptions
                .Builder()
                .also {
                    if (!ThemeUtils.isSystemAccent) {
                        it.setThemeOverlay(ThemeUtils.getColorThemeStyleRes(this))
                    }
                }.build(),
        )

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onApplyThemeResource(
        theme: Resources.Theme,
        resid: Int,
        first: Boolean,
    ) {
        super.onApplyThemeResource(theme, resid, first)
        if (!DynamicColors.isDynamicColorAvailable()) {
            theme.applyStyle(ThemeUtils.getColorThemeStyleRes(this), true)
        }

        theme.applyStyle(ThemeUtils.getOverlayThemeStyleRes(this), true)

        applyWallpaperBackgroundColor()
    }

    fun applyWallpaperBackgroundColor(value: Int = PrefManager.systemWallpaperAlpha) {
        if (PrefManager.systemWallpaper) {
            val color =
                (value shl 24) +
                    if (ThemeUtils.isNightMode(this)) {
                        0x00000000
                    } else {
                        0x00FFFFFF
                    }

            window.setBackgroundDrawable(color.toDrawable())
        }
    }
}
