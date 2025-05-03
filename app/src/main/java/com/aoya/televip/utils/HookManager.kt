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
import com.aoya.televip.hooks.HidePhoneNumber
import com.aoya.televip.hooks.HideSeenStatus
import com.aoya.televip.hooks.HideStoryViewStatus
import com.aoya.televip.hooks.HideTyping
// import com.aoya.televip.hooks.MarkDeletedMessages
import com.aoya.televip.hooks.PreventSecretMediaDeletion
import com.aoya.televip.hooks.ProfileDetails
import com.aoya.televip.hooks.ShowDeletedMessages
import com.aoya.televip.hooks.UnlockChannelFeatures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass

class HookManager {
    private var hooks = mutableMapOf<KClass<out Hook>, Hook>()

    fun registerHooks(init: Boolean = true) {
        runBlocking(Dispatchers.IO) {
            val hookList =
                listOf(
                    AddGhostModeOption(),
                    AllowScreenshots(),
                    ApplyColor(),
                    BoostDownload(),
                    ProfileDetails(),
                    // MarkDeletedMessages(),
                    AddChatNavigation(),
                )

            if (!init) return@runBlocking

            hooks = hookList.associateBy { it::class }.toMutableMap()

            hooks.values.forEach { hook ->
                hook.init()
            }
        }
        runBlocking(Dispatchers.IO) {
            val hookList =
                mutableListOf(
                    HideSeenStatus(),
                    HideStoryViewStatus(),
                )

            if (TeleVip.packageName != "tw.nekomimi.nekogram") {
                hookList.add(HideOnlineStatus())
            }

            hookList.addAll(
                listOf(
                    HidePhoneNumber(),
                    HideTyping(),
                    ShowDeletedMessages(),
                    PreventSecretMediaDeletion(),
                    UnlockChannelFeatures(),
                    AllowSaveVideos(),
                    FakePremium(),
                ),
            )

            Config.onUserSet = { _ ->
                hookList.forEach { hook ->
                    Config.initHookSettings(hook.hookName, true)
                }

                if (init) {
                    hooks = hookList.associateBy { it::class }.toMutableMap()

                    hooks.values.forEach { hook ->
                        if (!Config.isHookEnabled(hook.hookName)) return@forEach
                        hook.init()
                    }
                }
            }
        }
    }

    fun reloadHooks() {
        runBlocking(Dispatchers.IO) {
            hooks.values.forEach { hook -> hook.cleanup() }
            hooks.clear()
            registerHooks()
        }
    }

    fun init() {
        registerHooks()
    }
}
