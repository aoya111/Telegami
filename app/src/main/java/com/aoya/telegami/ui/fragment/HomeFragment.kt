package com.aoya.telegami.ui.fragment

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.aoya.telegami.BuildConfig
import com.aoya.telegami.R
import com.aoya.telegami.databinding.FragmentHomeBinding
import com.aoya.telegami.service.PrefManager
import com.aoya.telegami.ui.util.ThemeUtils.attrDrawable
import com.aoya.telegami.ui.util.ThemeUtils.getColor
import com.aoya.telegami.ui.util.ThemeUtils.homeItemBackgroundColor
import com.aoya.telegami.ui.util.ThemeUtils.themeColor
import com.aoya.telegami.ui.util.navigate
import com.aoya.telegami.ui.util.setEdge2EdgeFlags
import com.aoya.telegami.ui.util.setupToolbar
import dev.androidbroadcast.vbpd.viewBinding

class HomeFragment : Fragment(R.layout.fragment_home) {
    private val binding by viewBinding(FragmentHomeBinding::bind)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        with(binding.toolbar) {
            setupToolbar(
                toolbar = binding.toolbar,
                title = getString(R.string.app_name),
            )
        }

        setEdge2EdgeFlags(binding.root)
    }

    override fun onStart() {
        super.onStart()

        setupStatusCard()
        setupHomeItems()
    }

    fun setupStatusCard() {
        val version = PrefManager.getActiveVersion()
        var color =
            when {
                version == 0 -> getColor(R.color.invalid)
                else -> themeColor(android.R.attr.colorPrimary)
            }

        if (PrefManager.systemWallpaper) color -= 0x55000000

        with(binding.statusCard) {
            root.setCardBackgroundColor(color)
            root.outlineAmbientShadowColor = color
            root.outlineSpotShadowColor = color

            if (version > 0) {
                moduleStatusIcon.setImageResource(R.drawable.sentiment_calm_24px)
                val versionNameSimple = BuildConfig.VERSION_NAME.substringBefore(".r")
                moduleStatus.text =
                    getString(R.string.home_xposed_activated, versionNameSimple)
            } else {
                moduleStatusIcon.setImageResource(R.drawable.sentiment_very_dissatisfied_24px)
                moduleStatus.setText(R.string.home_xposed_not_activated)
            }
        }
    }

    fun setupHomeItems() {
        with(binding.navFeatures.root.parent as ViewGroup) {
            val childCount = childCount

            val softCorner: Float = resources.displayMetrics.density * 24
            val squareCorner: Float = resources.displayMetrics.density * 8
            val pad = (resources.displayMetrics.density * 16).toInt()

            for (i in 0..<childCount) {
                getChildAt(i).apply {
                    (this as ViewGroup).apply {
                        val textColor =
                            themeColor(
                                com.google.android.material.R.attr.colorOnSurface,
                            )

                        findViewById<TextView>(android.R.id.text1).setTextColor(textColor)
                        findViewById<ImageView>(android.R.id.icon).setColorFilter(textColor)
                    }

                    (layoutParams as LinearLayout.LayoutParams).apply {
                        setMargins(pad, 0, pad, 0)
                    }

                    val backgroundDrawable = GradientDrawable()
                    backgroundDrawable.setColor(homeItemBackgroundColor())

                    if (i == 0) {
                        backgroundDrawable.setCornerRadii(
                            floatArrayOf(
                                softCorner,
                                softCorner,
                                softCorner,
                                softCorner,
                                squareCorner,
                                squareCorner,
                                squareCorner,
                                squareCorner,
                            ),
                        )
                    } else if (i == childCount - 1) {
                        backgroundDrawable.setCornerRadii(
                            floatArrayOf(
                                squareCorner,
                                squareCorner,
                                squareCorner,
                                squareCorner,
                                softCorner,
                                softCorner,
                                softCorner,
                                softCorner,
                            ),
                        )
                    } else {
                        backgroundDrawable.setCornerRadii(
                            floatArrayOf(
                                squareCorner,
                                squareCorner,
                                squareCorner,
                                squareCorner,
                                squareCorner,
                                squareCorner,
                                squareCorner,
                                squareCorner,
                            ),
                        )
                    }

                    val ripple = attrDrawable(android.R.attr.selectableItemBackground)
                    val layerDrawable =
                        LayerDrawable(
                            arrayOf(
                                backgroundDrawable,
                                ripple,
                            ),
                        )

                    background = layerDrawable
                    clipToOutline = true
                }
            }
        }

        with(binding.navFeatures) {
            text1.text = getString(R.string.title_features)
            icon.setImageResource(R.drawable.outline_extension_24)
            root.setOnClickListener {
                navigate(R.id.nav_features)
            }
        }

        with(binding.navSettings) {
            text1.text = getString(R.string.title_settings)
            icon.setImageResource(R.drawable.outline_settings_24)
            root.setOnClickListener {
                navigate(R.id.nav_settings)
            }
        }
    }
}
