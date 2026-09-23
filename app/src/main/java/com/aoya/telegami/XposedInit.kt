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
import com.aoya.telegami.hooks.HideUpdate
import com.aoya.telegami.hooks.HideStoryViewStatus
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
        Settings.install(this, classLoader)
        LocaleController.install(this, classLoader)
        MarkMessages.install(this, classLoader)
        AllowScreenshots.install(this, classLoader)
        ProfileDetails.install(this, classLoader)
        HideUpdate.install(this, classLoader)
        Privacy.install(this, classLoader)
        HideStoryViewStatus.install(this, classLoader)
        ShowDeletedMessages.install(this, classLoader)
        PreventSecretMediaDeletion.install(this, classLoader)
        UnlockChannelFeatures.install(this, classLoader)
        AllowSaveVideos.install(this, classLoader)
        DisableAds.install(this, classLoader)
        FakePremium.install(this, classLoader)
        BoostDownload.install(this, classLoader)
        ApplyTheme.install(this, classLoader)
    }
}
