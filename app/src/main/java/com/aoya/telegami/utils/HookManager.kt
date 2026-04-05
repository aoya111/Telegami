package com.aoya.telegami.utils

import com.aoya.telegami.Telegami
import com.aoya.telegami.core.Config
import com.aoya.telegami.hooks.AllowSaveVideos
import com.aoya.telegami.hooks.AllowScreenshots
import com.aoya.telegami.hooks.ApplyColor
import com.aoya.telegami.hooks.BoostDownload
import com.aoya.telegami.hooks.DisableAds
import com.aoya.telegami.hooks.FakePremium
import com.aoya.telegami.hooks.HideBetaUpdate
import com.aoya.telegami.hooks.HideOnlineStatus
import com.aoya.telegami.hooks.HideSeenStatus
import com.aoya.telegami.hooks.HideStoryViewStatus
import com.aoya.telegami.hooks.HideTyping
import com.aoya.telegami.hooks.LocaleController
import com.aoya.telegami.hooks.MarkMessages
import com.aoya.telegami.hooks.NekoBlock
import com.aoya.telegami.hooks.PreventSecretMediaDeletion
import com.aoya.telegami.hooks.ProfileDetails
import com.aoya.telegami.hooks.Settings
import com.aoya.telegami.hooks.ShowDeletedMessages
import com.aoya.telegami.hooks.UnlockChannelFeatures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass

class HookManager {
    private val alwaysOnHooks =
        listOf(
            Settings(),
            LocaleController(),
            MarkMessages(),
            AllowScreenshots(),
            ApplyColor(),
            BoostDownload(),
            ProfileDetails(),
            NekoBlock(),
            HideBetaUpdate(),
        )

    private val configurableHooks =
        listOf(
            HideSeenStatus(),
            HideStoryViewStatus(),
            HideOnlineStatus(),
            HideTyping(),
            ShowDeletedMessages(),
            PreventSecretMediaDeletion(),
            UnlockChannelFeatures(),
            AllowSaveVideos(),
            DisableAds(),
            FakePremium(),
        )

    fun init() {
        logd("Initializing HookManager")

        // Always initialize always-on hooks
        logd("Initializing ${alwaysOnHooks.size} always-on hooks...")
        alwaysOnHooks.forEach { hook ->
            try {
                hook.init()
                logd("Initialized: ${hook.hookKey}")
            } catch (e: Exception) {
                loge("Failed to initialize ${hook.hookKey}", e)
            }
        }

        logd("Initializing configurable hooks")
        initConfigurableHooks()

        logd("HookManager initialization complete")
    }

    private fun initConfigurableHooks() {
        runBlocking(Dispatchers.IO) {
            logd("Registering ${configurableHooks.size} configurable hooks...")
            configurableHooks.forEach { hook ->
                try {
                    hook.init()
                    logd("Initialized: ${hook.hookKey}")
                } catch (e: Exception) {
                    loge("Failed to initialize ${hook.hookKey}", e)
                }
            }
        }
    }

    /**
     * Get all hook names grouped by type
     */
    fun getHookNames(): Map<String, List<String>> =
        mapOf(
            "alwaysOn" to alwaysOnHooks.map { it.hookKey },
            "configurable" to configurableHooks.map { it.hookKey },
        )
}
