package com.aoya.telegami.virt.ui.adapters

import com.aoya.telegami.Telegami
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.getIntField
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.newInstance
import java.util.ArrayList
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class DrawerLayoutAdapter(
    private val instance: Any,
) {
    private val objPath = "org.telegram.ui.Adapters.DrawerLayoutAdapter"

    class Item(
        private val instance: Any,
    ) {
        private val objPath = "org.telegram.ui.Adapters.DrawerLayoutAdapter\$Item"

        constructor(id: Int, text: String, icon: Int) : this(
            newInstance(
                Telegami.loadClass(resolver.get("org.telegram.ui.Adapters.DrawerLayoutAdapter\$Item")),
                id,
                text,
                icon,
            ),
        )

        val id: Int
            get() = getIntField(instance, resolver.getField(objPath, "id"))

        val icon: Int
            get() = getIntField(instance, resolver.getField(objPath, "icon"))

        fun getNativeInstance(): Any = instance
    }

    val items: MutableList<Item?>
        get() {
            val its = getObjectField(instance, resolver.getField(objPath, "items")) as ArrayList<Any?>
            return its.map { it?.let { obj -> Item(obj) } }.toMutableList()
        }

    fun addItem(item: Item) {
        val its = getObjectField(instance, resolver.getField(objPath, "items")) as ArrayList<Any?>
        its.add(item.getNativeInstance())
    }

    fun addItem(
        idx: Int,
        item: Item,
    ) {
        val its = getObjectField(instance, resolver.getField(objPath, "items")) as ArrayList<Any?>
        its.add(idx, item.getNativeInstance())
    }

    fun getId(position: Int) = callMethod(instance, resolver.getMethod(objPath, "getId"), position) as Int
}
