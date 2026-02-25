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
    val hooks: MutableMap<String, Boolean> = mutableMapOf(),
    var theme: ThemePrefs = ThemePrefs(),
)

object Config {
    private var localConfig: UserConfig = UserConfig()
    private var packageName = ""
    private var xPrefs: XSharedPreferences? = null

    var onUserSet: ((User) -> Unit)? = null

    private var user: User by Delegates.observable(User()) { _, old, new ->
        if (new.id != 0L && old.id != new.id) {
            logd("User set: ${new.username} (${new.id})")
            localConfig = readConfig()
            onUserSet?.invoke(new)
        }
    }

    fun initialize(
        packageName: String? = null,
        user: User? = null,
    ) {
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
        }
        user?.let {
            if (this.user.id != it.id) {
                logd("Setting user: ${it.username} (${it.id})")
                this.user = it
            } else {
                logd("Same user (${it.id}), skipping")
            }
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
        if (user.id == 0L) return UserConfig()
        try {
            reload()
            val configStr = xPrefs?.getString(user.id.toString(), "{}") ?: "{}"
            val type = object : TypeToken<UserConfig>() {}.type
            val conf = Gson().fromJson(configStr, type) ?: UserConfig()
            conf.user = user
            logd("Config read successfully for user ${user.id}")
            return conf
        } catch (e: Exception) {
            loge("Error reading config for user ${user.id}", e)
            return UserConfig().apply { this.user = user }
        }
    }

    @Synchronized
    fun writeConfig() {
        if (user.id == 0L) {
            logw("Cannot write config: no user set")
            return
        }

        try {
            val pref = Telegami.context.getSharedPreferences("telegami", Context.MODE_PRIVATE)
            pref.edit().putString(user.id.toString(), Gson().toJson(localConfig)).apply()
            reload()
            logd("Config written successfully for user ${user.id}")
        } catch (e: Exception) {
            loge("Error writing config for user ${user.id}", e)
        }
    }

    fun setHookEnabled(
        hookName: String,
        enabled: Boolean,
    ) {
        if (user.id == 0L) {
            logw("Cannot set hook state: no user set")
            return
        }
        logd("Hook '$hookName' set to: $enabled")
        localConfig.hooks[hookName] = enabled
        writeConfig()
    }

    fun isHookEnabled(hookName: String): Boolean = localConfig.hooks[hookName] ?: false

    fun initHookSettings(
        name: String,
        state: Boolean,
    ) {
        if (user.id == 0L) {
            logw("Cannot init hook settings: no user set")
            return
        }
        val hooks = localConfig.hooks
        if (!hooks.containsKey(name)) {
            hooks[name] = state
            writeConfig()
            logd("Hook '$name' initialized with state: $state")
        }
    }

    fun getHooksSettings(): Map<String, Boolean> = localConfig.hooks

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
