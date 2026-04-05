package com.aoya.telegami.ui.fragment

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.aoya.telegami.BuildConfig
import com.aoya.telegami.R
import com.aoya.telegami.core.Constants
import com.aoya.telegami.databinding.FragmentAboutBinding
import com.aoya.telegami.databinding.FragmentAboutListItemBinding
import com.aoya.telegami.ui.util.ThemeUtils.homeItemBackgroundColor
import com.aoya.telegami.ui.util.navController
import com.aoya.telegami.ui.util.setEdge2EdgeFlags
import com.aoya.telegami.utils.PackageHelper.findEnabledAppComponent
import com.bumptech.glide.Glide
import dev.androidbroadcast.vbpd.viewBinding

class AboutFragment : Fragment(R.layout.fragment_about) {
    private val binding by viewBinding(FragmentAboutBinding::bind)
    lateinit var tint: ColorStateList

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setEdge2EdgeFlags(binding.root, bottom = 0) { _, _, _, bottom ->
            binding.bottomPadding.minimumHeight = bottom
        }

        tint = ColorStateList.valueOf(homeItemBackgroundColor())

        setupHeader()

        with(binding.aboutDescription) {
            contentTitle.setText(R.string.title_about)
            contentDescription.setText(R.string.about_description)
            contentDescription.backgroundTintList = tint
        }

        with(binding.aboutForkDescription) {
            contentTitle.setText(R.string.title_about_fork)
            contentDescription.setText(R.string.about_fork_description)
            contentDescription.backgroundTintList = tint
        }

        setupDevSection()
    }

    fun setupHeader() {
        with(binding.aboutHeader) {
            with(backButton.parent as View) {
                setOnClickListener { navController.navigateUp() }
                backgroundTintList = tint
            }

            Glide
                .with(this@AboutFragment)
                .let {
                    val activityName = findEnabledAppComponent(requireContext())
                    return@let if (activityName == null) {
                        it.load(R.mipmap.ic_launcher)
                    } else {
                        it.load(requireContext().packageManager.getActivityIcon(activityName))
                    }
                }.circleCrop()
                .into(appIcon)

            appName.setText(R.string.app_name)
            appVersion.text = BuildConfig.VERSION_NAME

            setOnClickUrl(linkGithub, Constants.GITHUB_REPO)
            setOnClickUrl(linkTelegram, Constants.TELEGRAM_CHANNEL)

            appInfoTop.backgroundTintList = tint
            (appName.parent as View).backgroundTintList = tint
        }
    }

    fun setupDevSection() {
        with(binding.listDeveloper) {
            backgroundTintList = tint
            clipToOutline = true
        }

        with(binding.listTelegami) {
            addDevItem(this, R.drawable.cont_soul2x, "soul2x", "Telegami Developer", "https://github.com/aoya111")
        }
    }

    fun addDevItem(
        layout: LinearLayout,
        @DrawableRes avatarResId: Int,
        name: String,
        desc: String,
        url: String,
    ) {
        val newLayout = FragmentAboutListItemBinding.inflate(layoutInflater)
        setOnClickUrl(newLayout.root, url)

        newLayout.aboutPersonIcon.setImageDrawable(
            RoundedBitmapDrawableFactory
                .create(
                    resources,
                    BitmapFactory.decodeResource(resources, avatarResId),
                ).apply {
                    isCircular = true
                },
        )

        newLayout.text1.text = name
        newLayout.text2.text = desc
        layout.addView(newLayout.root)
    }

    fun setOnClickUrl(
        view: View,
        url: String,
    ) {
        view.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(url.toUri())
            startActivity(intent)
        }
    }
}
