package com.aoya.telegami.utils

import com.aoya.telegami.Telegami
import com.aoya.telegami.core.Config
import com.aoya.telegami.hooks.AddChatNavigation
import com.aoya.telegami.hooks.AllowSaveVideos
import com.aoya.telegami.hooks.AllowScreenshots
import com.aoya.telegami.hooks.ApplyColor
import com.aoya.telegami.hooks.BoostDownload
import com.aoya.telegami.hooks.FakePremium
import com.aoya.telegami.hooks.HideOnlineStatus
import com.aoya.telegami.hooks.HidePhone
import com.aoya.telegami.hooks.HideSeenStatus
import com.aoya.telegami.hooks.HideStoryViewStatus
import com.aoya.telegami.hooks.HideTyping
import com.aoya.telegami.hooks.PreventSecretMediaDeletion
import com.aoya.telegami.hooks.ProfileDetails
import com.aoya.telegami.hooks.Settings
import com.aoya.telegami.hooks.ShowDeletedMessages
import com.aoya.telegami.hooks.UnlockChannelFeatures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass

class HookManager {
    private var alwaysOnHooks = mutableMapOf<KClass<out Hook>, Hook>()
    private var configurableHooks = mutableMapOf<KClass<out Hook>, Hook>()

    private val hookRegistry =
        mapOf<String, () -> Hook>(
            // Always-on hooks
            "settings" to { Settings() },
            "fake_premium" to { FakePremium() },
            "allow_screenshots" to { AllowScreenshots() },
            "apply_color" to { ApplyColor() },
            "boost_download" to { BoostDownload() },
            "profile_details" to { ProfileDetails() },
            "add_chat_navigation" to { AddChatNavigation() },
            // Configurable hooks
            "hide_seen_status" to { HideSeenStatus() },
            "hide_story_view_status" to { HideStoryViewStatus() },
            "hide_online_status" to { HideOnlineStatus() },
            "hide_phone" to { HidePhone() },
            "hide_typing" to { HideTyping() },
            "show_deleted_messages" to { ShowDeletedMessages() },
            "prevent_secret_media_deletion" to { PreventSecretMediaDeletion() },
            "unlock_channel_features" to { UnlockChannelFeatures() },
            "allow_save_videos" to { AllowSaveVideos() },
        )

    private val alwaysOnHookNames =
        setOf(
            "settings",
            "fake_premium",
            "allow_screenshots",
            "apply_color",
            "boost_download",
            "profile_details",
            "add_chat_navigation",
        )

    private fun initAlwaysOnHooks() {
        logd("Initializing always-on hooks...")
        alwaysOnHookNames.forEach { hookName ->
            hookRegistry[hookName]?.let { factory ->
                val hook = factory()
                hook.init()
                alwaysOnHooks[hook::class] = hook
                logd("Always-on hook initialized: ${hook.hookName}")
            }
        }
    }

    fun registerConfigurableHooks(initConfig: Boolean = false) {
        runBlocking(Dispatchers.IO) {
            val hookNames = hookRegistry.keys - alwaysOnHookNames

            if (initConfig) {
                logd("Initializing config for ${hookNames.size} configurable hooks")
                hookNames.forEach { hookName ->
                    Config.initHookSettings(hookName, true)
                }
            }

            hookNames.forEach { hookName ->
                if (isHookEnabled(hookName)) {
                    loadHook(hookName, true)
                }
            }
        }
    }

    fun reloadHooks() {
        runBlocking(Dispatchers.IO) {
            logd("Reloading all hooks...")
            alwaysOnHooks.values.forEach { it.cleanup() }
            alwaysOnHooks.clear()
            initAlwaysOnHooks()
            reloadConfigurableHooks()
            logd("All hooks reloaded successfully")
        }
    }

    fun reloadConfigurableHooks() {
        runBlocking(Dispatchers.IO) {
            logd("Reloading configurable hooks...")
            configurableHooks.values.forEach { it.cleanup() }
            configurableHooks.clear()
            registerConfigurableHooks()
            logd("Configurable hooks reloaded")
        }
    }

    fun init() {
        logd("Initializing HookManager")
        initAlwaysOnHooks()

        Config.onUserSet = { user ->
            logd("User set in config, registering configurable hooks")
            registerConfigurableHooks(true)
        }

        if (Config.isUserSet()) {
            logd("User already set, loading configurable hooks")
            registerConfigurableHooks()
        }
        logd("HookManager initialization complete")
    }

    /**
     * Unload a specific hook by name
     */
    fun unloadHook(hookName: String): Boolean =
        getAllActiveHooks().find { it.hookName == hookName }?.let { hook ->
            hook.cleanup()

            if (alwaysOnHookNames.contains(hookName)) {
                alwaysOnHooks.remove(hook::class)
            } else {
                configurableHooks.remove(hook::class)
            }

            logd("Unloaded hook: $hookName")
            true
        } ?: run {
            logw("Hook not found: $hookName")
            false
        }

    /**
     * Load a specific hook by name (if it's not already loaded)
     */
    fun loadHook(
        hookName: String,
        initialize: Boolean = false,
    ): Boolean {
        if (isHookLoaded(hookName)) {
            logd("Hook already loaded: $hookName")
            return false
        }

        return hookRegistry[hookName]?.let { factory ->
            val isAlwaysOn = alwaysOnHookNames.contains(hookName)

            // Check if configurable hook is enabled
            if (!isAlwaysOn && !isHookEnabled(hookName)) {
                logd("Hook is disabled in config: $hookName")
                return false
            }

            val hook = factory()
            if (initialize) {
                try {
                    hook.init()
                } catch (e: Exception) {
                    loge("Failed to initialize hook: $hookName", e)
                    return false
                }
            }

            // Store in appropriate map
            if (isAlwaysOn) {
                alwaysOnHooks[hook::class] = hook
            } else {
                configurableHooks[hook::class] = hook
            }

            logd("Loaded hook: $hookName")
            true
        } ?: run {
            loge("Unknown hook name: $hookName")
            false
        }
    }

    /**
     * Get all currently active hooks
     */
    private fun getAllActiveHooks(): List<Hook> = (alwaysOnHooks.values + configurableHooks.values).toList()

    /**
     * Check if a hook is currently loaded
     */
    fun isHookLoaded(hookName: String): Boolean = getAllActiveHooks().any { it.hookName == hookName }

    /**
     * Check if a hook is currently enabled
     */
    fun isHookEnabled(hookName: String): Boolean = Config.isHookEnabled(hookName)

    /**
     * Toggle a hook (unload if loaded, load if not loaded)
     */
    fun toggleHook(hookName: String): Boolean =
        if (isHookLoaded(hookName)) {
            unloadHook(hookName)
        } else {
            loadHook(hookName, true)
        }

    /**
     * Get loaded hook names grouped by type
     */
    fun getLoadedHookNames(): Map<String, List<String>> {
        val result =
            mapOf(
                "alwaysOn" to alwaysOnHooks.values.map { it.hookName },
                "configurable" to configurableHooks.values.map { it.hookName },
            )
        logd("Loaded hooks - Always-on: ${result["alwaysOn"]?.size}, Configurable: ${result["configurable"]?.size}")
        return result
    }
}
