package com.aoya.telegami.core

import android.content.Context
import com.aoya.telegami.Telegami
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import kotlin.properties.Delegates

typealias UserId = Long

data class User(
    val id: Long = 0,
    val username: String = "",
    val phone: String = "",
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
    val contacts: MutableMap<UserId, Contact> = mutableMapOf(),
    var theme: ThemePrefs = ThemePrefs(),
)

data class Contact(
    val name: String = "",
    var newName: String = "",
)

object Config {
    private var localConfig: UserConfig = UserConfig()
    private var packageName = ""
    private var xPrefs: XSharedPreferences? = null

    var onUserSet: ((User) -> Unit)? = null

    private var user: User by Delegates.observable(User()) { _, old, new ->
        if (old.id == 0L && new.id != 0L) {
            XposedBridge.log("User set: ${new.id}, reloading config")
            localConfig = readConfig()
            onUserSet?.invoke(new)
        }
    }

    fun initialize(
        packageName: String? = null,
        user: User? = null,
    ) {
        packageName?.let {
            this.packageName = it
            xPrefs =
                XSharedPreferences(it, "telegami").apply {
                    makeWorldReadable()
                }
        }
        user?. let { this.user = it }
    }

    @Synchronized
    fun reload() {
        xPrefs?.reload()
    }

    @Synchronized
    fun hasConfig(): Boolean {
        if (user.id == 0L) return false
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
            return conf
        } catch (e: Exception) {
            XposedBridge.log("Error reading config: ${e.message}")
            return UserConfig().apply { this.user = user }
        }
    }

    @Synchronized
    fun writeConfig() {
        if (user.id == 0L) return

        try {
            val pref = Telegami.context.getSharedPreferences("telegami", Context.MODE_PRIVATE)
            pref.edit().putString(user.id.toString(), Gson().toJson(localConfig)).apply()
            reload()
        } catch (e: Exception) {
            XposedBridge.log("Error writing config: ${e.message}")
        }
    }

    fun setHookEnabled(
        hookName: String,
        enabled: Boolean,
    ) {
        if (user.id == 0L) return
        localConfig.hooks[hookName] = enabled
        writeConfig()
    }

    fun isHookEnabled(hookName: String): Boolean = localConfig.hooks[hookName] ?: false

    fun initHookSettings(
        name: String,
        state: Boolean,
    ) {
        if (user.id == 0L) return
        val hooks = localConfig.hooks
        if (!hooks.containsKey(name)) {
            hooks[name] = state
            writeConfig()
        }
    }

    fun getHooksSettings(): Map<String, Boolean> = localConfig.hooks

    fun getContactNewName(id: Long): String {
        val contact = localConfig.contacts.getOrPut(id) { Contact() }
        return contact.newName
    }

    fun setContactNewName(
        id: Long,
        name: String,
    ) {
        val contact = localConfig.contacts.getOrPut(id) { Contact() }
        contact.newName = name
        writeConfig()
    }

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
