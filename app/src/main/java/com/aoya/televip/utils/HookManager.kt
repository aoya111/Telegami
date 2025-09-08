package com.aoya.televip.utils

import com.aoya.televip.TeleVip
import com.aoya.televip.core.Config
import com.aoya.televip.hooks.AddChatNavigation
import com.aoya.televip.hooks.AddGhostModeOption
import com.aoya.televip.hooks.AllowSaveVideos
import com.aoya.televip.hooks.AllowScreenshots
import com.aoya.televip.hooks.ApplyColor
import com.aoya.televip.hooks.BoostDownload
import com.aoya.televip.hooks.FakePremium
import com.aoya.televip.hooks.HideOnlineStatus
import com.aoya.televip.hooks.HidePhone
import com.aoya.televip.hooks.HideSeenStatus
import com.aoya.televip.hooks.HideStoryViewStatus
import com.aoya.televip.hooks.HideTyping
import com.aoya.televip.hooks.PreventSecretMediaDeletion
import com.aoya.televip.hooks.ProfileDetails
import com.aoya.televip.hooks.ShowDeletedMessages
import com.aoya.televip.hooks.UnlockChannelFeatures
import de.robv.android.xposed.XposedBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass

class HookManager {
    private var alwaysOnHooks = mutableMapOf<KClass<out Hook>, Hook>()
    private var configurableHooks = mutableMapOf<KClass<out Hook>, Hook>()

    private val hookRegistry =
        mapOf<String, () -> Hook>(
            // Always-on hooks
            "add_ghost_mode_option" to { AddGhostModeOption() },
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
            "fake_premium" to { FakePremium() },
        )

    private val alwaysOnHookNames =
        setOf(
            "add_ghost_mode_option",
            "allow_screenshots",
            "apply_color",
            "boost_download",
            "profile_details",
            "add_chat_navigation",
        )

    private fun initAlwaysOnHooks() {
        alwaysOnHookNames.forEach { hookName ->
            hookRegistry[hookName]?.let { factory ->
                val hook = factory()
                hook.init()
                alwaysOnHooks[hook::class] = hook
                XposedBridge.log("Always-on hook initialized: ${hook.hookName}")
            }
        }
    }

    fun registerConfigurableHooks(initConfig: Boolean = false) {
        runBlocking(Dispatchers.IO) {
            val hookNames = hookRegistry.keys - alwaysOnHookNames

            if (initConfig) {
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
            alwaysOnHooks.values.forEach { it.cleanup() }
            alwaysOnHooks.clear()
            initAlwaysOnHooks()
            reloadConfigurableHooks()
        }
    }

    fun reloadConfigurableHooks() {
        runBlocking(Dispatchers.IO) {
            configurableHooks.values.forEach { it.cleanup() }
            configurableHooks.clear()
            registerConfigurableHooks()
        }
    }

    fun init() {
        initAlwaysOnHooks()

        Config.onUserSet = { user ->
            registerConfigurableHooks(true)
        }

        if (Config.isUserSet()) {
            registerConfigurableHooks()
        }
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

            XposedBridge.log("Unloaded hook: $hookName")
            true
        } ?: run {
            XposedBridge.log("Hook not found: $hookName")
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
            XposedBridge.log("Hook already loaded: $hookName")
            return false
        }

        return hookRegistry[hookName]?.let { factory ->
            val isAlwaysOn = alwaysOnHookNames.contains(hookName)

            // Check if configurable hook is enabled
            if (!isAlwaysOn && !isHookEnabled(hookName)) {
                XposedBridge.log("Hook is disabled in config: $hookName")
                return false
            }

            val hook = factory()
            if (initialize) hook.init()

            // Store in appropriate map
            if (isAlwaysOn) {
                alwaysOnHooks[hook::class] = hook
            } else {
                configurableHooks[hook::class] = hook
            }

            XposedBridge.log("Loaded hook: $hookName")
            true
        } ?: run {
            XposedBridge.log("Unknown hook name: $hookName")
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
    fun getLoadedHookNames(): Map<String, List<String>> =
        mapOf(
            "alwaysOn" to alwaysOnHooks.values.map { it.hookName },
            "configurable" to configurableHooks.values.map { it.hookName },
        )
}
