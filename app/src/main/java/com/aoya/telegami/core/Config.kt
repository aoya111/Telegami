package com.aoya.telegami.core

import android.content.Context
import com.aoya.telegami.Telegami
import com.aoya.telegami.utils.logd
import com.aoya.telegami.utils.loge
import com.aoya.telegami.utils.logw
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.robv.android.xposed.XSharedPreferences
import kotlin.properties.Delegates

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
    private val hooks: MutableMap<String, Boolean> = mutableMapOf()
    private var packageName = ""
    private var xPrefs: XSharedPreferences? = null

    private var user: User = User()

    fun init(packageName: String? = null) {
        logd("Initializing Config")
        packageName?.let {
            if (this.packageName != it) {
                logd("Initializing package: $it")
                this.packageName = it
                xPrefs =
                    XSharedPreferences(it, "telegami").apply {
                        makeWorldReadable()
                    }
                logd("XSharedPreferences initialized for package: $it")
            }
            localConfig = readConfig()
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
    fun hasConfig(): Boolean {
        if (user.id == 0L) {
            logd("No user set, config unavailable")
            return false
        }
        reload()
        val configStr = xPrefs?.getString(user.id.toString(), null)
        return !configStr.isNullOrEmpty() && configStr != "{}"
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

            hooks.clear()
            xPrefs?.all?.forEach { (key, value) ->
                if (value is Boolean && key != "settings_selected_variant") {
                    hooks[key] = value
                }
            }
            logd("Hooks loaded: ${hooks.size} hooks")

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

            hooks.forEach { (hookName, enabled) ->
                editor.putBoolean(hookName, enabled)
            }

            editor.apply()
            reload()
            logd("Config written successfully")
        } catch (e: Exception) {
            loge("Error writing config", e)
        }
    }

    fun setHookEnabled(
        hookName: String,
        enabled: Boolean,
    ) {
        logd("Hook '$hookName' set to: $enabled")
        hooks[hookName] = enabled
        writeConfig()
    }

    fun isHookEnabled(hookName: String): Boolean = hooks[hookName] ?: true

    fun initHookSettings(
        name: String,
        state: Boolean,
    ) {
        if (!hooks.containsKey(name)) {
            hooks[name] = state
            writeConfig()
            logd("Hook '$name' initialized with state: $state")
        }
    }

    fun getHooksSettings(): Map<String, Boolean> = hooks

    fun getCurrentUser(): User = user

    fun isUserSet(): Boolean = user.id != 0L

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
