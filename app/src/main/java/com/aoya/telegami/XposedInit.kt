package com.aoya.telegami

import android.app.Application
import android.content.Context
import com.aoya.telegami.core.Constants
import com.aoya.telegami.hooks.AllowSaveVideos
import com.aoya.telegami.hooks.AllowScreenshots
import com.aoya.telegami.hooks.ApplyTheme
import com.aoya.telegami.hooks.BoostDownload
import com.aoya.telegami.hooks.DisableAds
import com.aoya.telegami.hooks.FakePremium
import com.aoya.telegami.hooks.HideStoryViewStatus
import com.aoya.telegami.hooks.HideUpdate
import com.aoya.telegami.hooks.LocaleController
import com.aoya.telegami.hooks.MarkMessages
import com.aoya.telegami.hooks.PreventSecretMediaDeletion
import com.aoya.telegami.hooks.Privacy
import com.aoya.telegami.hooks.ProfileDetails
import com.aoya.telegami.hooks.Settings
import com.aoya.telegami.hooks.ShowDeletedMessages
import com.aoya.telegami.hooks.UnlockChannelFeatures
import com.aoya.telegami.util.Logger
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam

class XposedInit : XposedModule() {
    override fun onPackageLoaded(param: PackageLoadedParam) {
        if (param.packageName !in Constants.SUPPORTED_TELEGRAM_PACKAGES) return
        Logger.attachXposedLogger { priority, tag, message, throwable ->
            if (throwable == null) {
                log(priority, tag, message)
            } else {
                log(priority, tag, message, throwable)
            }
        }
        val attach = Application::class.java.getDeclaredMethod("attach", Context::class.java)
        hook(attach).intercept { chain ->
            val result = chain.proceed()
            val application = chain.thisObject as Application
            Telegami.init(
                getModuleApplicationInfo().sourceDir,
                application,
                getRemotePreferences("features"),
            )
            installHooks(param.defaultClassLoader)
            result
        }
    }

    private fun installHooks(classLoader: ClassLoader) {
        fun install(
            name: String,
            hook: () -> Unit,
        ) {
            try {
                hook()
            } catch (throwable: Throwable) {
                Logger.e("Failed to install $name", throwable)
            }
        }

        install("Settings") { Settings.install(this, classLoader) }
        install("LocaleController") { LocaleController.install(this, classLoader) }
        install("MarkMessages") { MarkMessages.install(this, classLoader) }
        install("AllowScreenshots") { AllowScreenshots.install(this, classLoader) }
        install("ProfileDetails") { ProfileDetails.install(this, classLoader) }
        install("HideUpdate") { HideUpdate.install(this, classLoader) }
        install("Privacy") { Privacy.install(this, classLoader) }
        install("HideStoryViewStatus") { HideStoryViewStatus.install(this, classLoader) }
        install("ShowDeletedMessages") { ShowDeletedMessages.install(this, classLoader) }
        install("PreventSecretMediaDeletion") { PreventSecretMediaDeletion.install(this, classLoader) }
        install("UnlockChannelFeatures") { UnlockChannelFeatures.install(this, classLoader) }
        install("AllowSaveVideos") { AllowSaveVideos.install(this, classLoader) }
        install("DisableAds") { DisableAds.install(this, classLoader) }
        install("FakePremium") { FakePremium.install(this, classLoader) }
        install("BoostDownload") { BoostDownload.install(this, classLoader) }
        install("ApplyTheme") { ApplyTheme.install(this, classLoader) }
    }
}
