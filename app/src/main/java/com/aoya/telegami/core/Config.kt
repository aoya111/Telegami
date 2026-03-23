package com.aoya.telegami.core

import android.content.Context
import com.aoya.telegami.BuildConfig
import com.aoya.telegami.Telegami
import com.aoya.telegami.utils.logd
import com.aoya.telegami.utils.loge
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.robv.android.xposed.XSharedPreferences

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

data class UserConfig(
    var user: User = User(),
    var theme: ThemePrefs = ThemePrefs(),
)

object Config {
    private var localConfig: UserConfig = UserConfig()
    private val packageName = BuildConfig.APPLICATION_ID
    private var hookedPackageName = ""
    private var xPrefs: XSharedPreferences? = null

    private val featureCache = mutableMapOf<String, Boolean>()

    private var user: User = User()

    fun init(pkgName: String) {
        logd("Initializing Config")
        this.hookedPackageName = pkgName
        xPrefs = XSharedPreferences(pkgName, "telegami")
        logd("XSharedPreferences initialized for package: $pkgName")
        localConfig = readConfig()
        loadFeatureCache()
    }

    private fun loadFeatureCache() {
        val prefs = XSharedPreferences(packageName, "features")
        prefs.all.forEach { (key, value) ->
            if (value is Boolean) {
                featureCache[key] = value
            }
        }
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
    fun reload() {
        xPrefs?.reload()
    }

    @Synchronized
    fun readConfig(): UserConfig {
        try {
            reload()

            if (user.id != 0L) {
                val configStr = xPrefs?.getString(user.id.toString(), "{}") ?: "{}"
                val type = object : TypeToken<UserConfig>() {}.type
                val conf = Gson().fromJson(configStr, type) ?: UserConfig()
                localConfig = conf
                localConfig.user = user
                logd("User config read successfully for user ${user.id}")
            }

            return localConfig
        } catch (e: Exception) {
            loge("Error reading config", e)
            return UserConfig().apply { this.user = user }
        }
    }

    @Synchronized
    fun writeConfig() {
        try {
            val pref = Telegami.context.getSharedPreferences("telegami", Context.MODE_PRIVATE)
            val editor = pref.edit()

            if (user.id != 0L) {
                editor.putString(user.id.toString(), Gson().toJson(localConfig))
            }

            editor.apply()
            reload()
            logd("Config written successfully")
        } catch (e: Exception) {
            loge("Error writing config", e)
        }
    }

    fun setFeatureEnabled(
        context: Context,
        featureKey: String,
        enabled: Boolean,
    ) {
        val prefs = context.getSharedPreferences("features", Context.MODE_WORLD_READABLE)
        prefs.edit().putBoolean(featureKey, enabled).apply()
    }

    fun isFeatureEnabled(featureKey: String): Boolean = featureCache[featureKey] ?: false

    fun isFeatureEnabledInActivity(
        context: Context,
        featureKey: String,
    ): Boolean {
        val prefs = context.getSharedPreferences("features", Context.MODE_WORLD_READABLE)
        return prefs.getBoolean(featureKey, false)
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
