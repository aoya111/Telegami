package com.aoya.televip.core

import android.content.Context
import com.aoya.televip.TeleVip
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.robv.android.xposed.XSharedPreferences
import kotlin.properties.Delegates

typealias UserId = Long

data class User(
    val id: Long = 0,
    val username: String = "",
    val phone: String = "",
)

data class UserConfig(
    var user: User = User(),
    val hooks: MutableMap<String, Boolean> = mutableMapOf(),
    val contacts: MutableMap<UserId, Contact> = mutableMapOf(),
)

data class Contact(
    val name: String = "",
    var newName: String = "",
)

object Config {
    private var localConfig: UserConfig = UserConfig()
    private var packageName = ""
    var onUserSet: ((User) -> Unit)? = null

    private var user: User by Delegates.observable(User()) { _, old, new ->
        if (old.id == 0L && new.id != 0L) {
            onUserSet?.invoke(new)
        }
    }

    fun initialize(
        packageName: String? = null,
        user: User? = null,
    ) {
        packageName?.let { this.packageName = it }
        user?. let { this.user = it }
        localConfig = readConfig()
    }

    fun hasConfig(): Boolean {
        if (user.id == 0L) return false

        val pref = TeleVip.context.getSharedPreferences("televip", Context.MODE_PRIVATE)
        val configStr = pref.getString(user.id.toString(), null)
        return !configStr.isNullOrEmpty() && configStr != "{}"
    }

    fun readConfig(): UserConfig {
        if (user.id == 0L) return UserConfig()
        val pref = XSharedPreferences(packageName, "televip")

        val type = object : TypeToken<UserConfig>() {}.type
        val conf = Gson().fromJson(pref.getString(user.id.toString(), "{}") ?: "{}", type) ?: UserConfig()
        conf.user = user
        return conf
    }

    fun writeConfig() {
        if (user.id == 0L) return
        val pref = TeleVip.context.getSharedPreferences("televip", Context.MODE_PRIVATE)
        pref.edit().putString(user.id.toString(), Gson().toJson(localConfig)).apply()
    }

    fun setHookEnabled(
        hookName: String,
        enabled: Boolean,
    ) {
        localConfig.hooks.put(hookName, enabled)
        writeConfig()
    }

    fun isHookEnabled(hookName: String): Boolean = localConfig.hooks.get(hookName) ?: false

    fun initHookSettings(
        name: String,
        state: Boolean,
    ) {
        val hooks = localConfig.hooks

        if (!hooks.contains(name)) {
            hooks.put(name, state)
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
}
