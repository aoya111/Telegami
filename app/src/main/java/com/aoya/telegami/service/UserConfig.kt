package com.aoya.telegami.service

import android.content.Context
import com.aoya.telegami.util.logd
import com.aoya.telegami.util.loge
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.highcapable.yukihookapi.hook.factory.prefs
import com.highcapable.yukihookapi.hook.xposed.prefs.YukiHookPrefsBridge

typealias UserId = Long

data class User(
    val id: Long = 0,
    val username: String = "",
)

data class ThemePrefs(
    var profileColor: Int? = null,
    var profileEmoji: Long? = null,
    var nameColor: Int? = null,
    var nameEmoji: Long? = null,
)

data class UserPref(
    var user: User = User(),
    var theme: ThemePrefs = ThemePrefs(),
)

object UserConfig {
    private var localConfig: UserPref = UserPref()
    private var userPref: YukiHookPrefsBridge? = null

    private var user: User = User()

    fun init(context: Context) {
        logd("Initializing Config")
        userPref = context.prefs("telegami")
        localConfig = readConfig()
    }

    fun setUser(user: User) {
        logd("Setting User")
        if (this.user.id != user.id) {
            logd("Setting user: ${user.username} (${user.id})")
            this.user = user
            localConfig = readConfig()
        } else {
            logd("Same user (${user.id}), skipping")
        }
    }

    @Synchronized
    fun readConfig(): UserPref {
        try {
            if (user.id != 0L) {
                val configStr = userPref?.getString(user.id.toString(), "{}") ?: "{}"
                val type = object : TypeToken<UserPref>() {}.type
                val conf = Gson().fromJson(configStr, type) ?: UserPref()
                localConfig = conf
                localConfig.user = user
                logd("User config read successfully for user ${user.id}")
            }

            return localConfig
        } catch (e: Exception) {
            loge("Error reading config", e)
            return UserPref().apply { this.user = user }
        }
    }

    @Synchronized
    fun writeConfig() {
        try {
            if (user.id != 0L) {
                userPref?.edit { putString(user.id.toString(), Gson().toJson(localConfig)) }
            }
            logd("Config written successfully")
        } catch (e: Exception) {
            loge("Error writing config", e)
        }
    }

    fun getCurrentUser(): User = user

    fun getProfileColor(): Int? = localConfig.theme.profileColor

    fun getProfileEmoji(): Long? = localConfig.theme.profileEmoji

    fun getNameColor(): Int? = localConfig.theme.nameColor

    fun getNameEmoji(): Long? = localConfig.theme.nameEmoji

    fun setProfileColor(color: Int) {
        localConfig.theme.profileColor = color
        writeConfig()
    }

    fun setProfileEmoji(emoji: Long) {
        localConfig.theme.profileEmoji = emoji
        writeConfig()
    }

    fun setNameColor(color: Int) {
        localConfig.theme.nameColor = color
        writeConfig()
    }

    fun setNameEmoji(emoji: Long) {
        localConfig.theme.nameEmoji = emoji
        writeConfig()
    }
}
