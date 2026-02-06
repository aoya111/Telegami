package com.aoya.telegami.utils

import com.aoya.telegami.Telegami
import com.aoya.telegami.core.Config
import com.aoya.telegami.hooks.AllowSaveVideos
import com.aoya.telegami.hooks.AllowScreenshots
import com.aoya.telegami.hooks.ApplyColor
import com.aoya.telegami.hooks.BoostDownload
import com.aoya.telegami.hooks.FakePremium
import com.aoya.telegami.hooks.HideBetaUpdate
import com.aoya.telegami.hooks.HideOnlineStatus
import com.aoya.telegami.hooks.HidePhone
import com.aoya.telegami.hooks.HideSeenStatus
import com.aoya.telegami.hooks.HideStoryViewStatus
import com.aoya.telegami.hooks.HideTyping
import com.aoya.telegami.hooks.MarkMessages
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
            FakePremium(),
            MarkMessages(),
            AllowScreenshots(),
            ApplyColor(),
            BoostDownload(),
            ProfileDetails(),
            HideBetaUpdate(),
        )

    private val configurableHooks =
        listOf(
            HideSeenStatus(),
            HideStoryViewStatus(),
            HideOnlineStatus(),
            HidePhone(),
            HideTyping(),
            ShowDeletedMessages(),
            PreventSecretMediaDeletion(),
            UnlockChannelFeatures(),
            AllowSaveVideos(),
        )

    fun init() {
        logd("Initializing HookManager")

        // Always initialize always-on hooks
        logd("Initializing ${alwaysOnHooks.size} always-on hooks...")
        alwaysOnHooks.forEach { hook ->
            try {
                hook.init()
                logd("Initialized: ${hook.hookName}")
            } catch (e: Exception) {
                loge("Failed to initialize ${hook.hookName}", e)
            }
        }

        // Initialize configurable hooks when user is set
        Config.onUserSet = { user ->
            logd("User set, initializing configurable hooks")
            initConfigurableHooks(initConfig = true)
        }

        if (Config.isUserSet()) {
            logd("User already set, initializing configurable hooks")
            initConfigurableHooks()
        }

        logd("HookManager initialization complete")
    }

    private fun initConfigurableHooks(initConfig: Boolean = false) {
        runBlocking(Dispatchers.IO) {
            if (initConfig) {
                logd("Initializing config for ${configurableHooks.size} configurable hooks")
                configurableHooks.forEach { hook ->
                    Config.initHookSettings(hook.hookName, true)
                }
            }

            logd("Registering ${configurableHooks.size} configurable hooks...")
            configurableHooks.forEach { hook ->
                try {
                    hook.init()
                    logd("Initialized: ${hook.hookName}")
                } catch (e: Exception) {
                    loge("Failed to initialize ${hook.hookName}", e)
                }
            }
        }
    }

    /**
     * Get all hook names grouped by type
     */
    fun getHookNames(): Map<String, List<String>> =
        mapOf(
            "alwaysOn" to alwaysOnHooks.map { it.hookName },
            "configurable" to configurableHooks.map { it.hookName },
        )
}
