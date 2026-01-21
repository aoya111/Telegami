package com.aoya.telegami.virt.ui.components

import com.aoya.telegami.Telegami
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import com.aoya.telegami.core.obfuscate.ResolverManager as resolver

class BulletinFactory(
    private val instance: Any,
) {
    private val objPath = OBJ_PATH

    companion object {
        private const val OBJ_PATH = "org.telegram.ui.Components.BulletinFactory"

        fun createSaveToGalleryBulletin(
            ctx: Any,
            video: Boolean,
            arg3: Any?,
        ): Bulletin =
            Bulletin(
                callStaticMethod(
                    Telegami.loadClass(resolver.get(OBJ_PATH)),
                    resolver.getMethod(OBJ_PATH, "createSaveToGalleryBulletin"),
                    ctx,
                    video,
                    arg3,
                ),
            )
    }
}
