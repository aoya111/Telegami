package com.aoya.telegami.virt.ui.components

import com.aoya.telegami.Telegami
import com.highcapable.kavaref.KavaRef.Companion.resolve
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
                (Telegami.loadClass(resolver.get(OBJ_PATH)) as Class<Any>)
                    .resolve()
                    .firstMethod {
                        name = resolver.getMethod(OBJ_PATH, "createSaveToGalleryBulletin")
                        parameterCount = 3
                    }.invoke(ctx, video, arg3)!!,
            )
    }
}
