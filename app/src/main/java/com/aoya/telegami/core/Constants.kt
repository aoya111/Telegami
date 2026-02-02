package com.aoya.telegami.core

object Constants {
    const val GITHUB_REPO = "https://github.com/aoya111/Telegami"

    val SUPPORTED_TELEGRAM_PACKAGES =
        arrayOf(
            "it.octogram.android",
            "org.telegram.messenger",
            "org.telegram.messenger.beta",
            "org.telegram.messenger.web",
            "org.telegram.plus",
            "tw.nekomimi.nekogram",
            "xyz.nextalone.nagram",
        )

    val FEATURES =
        arrayOf(
            "hide_seen_status",
            "hide_story_view_status",
            "hide_online_status",
            "hide_phone",
            "hide_typing",
            "show_deleted_messages",
            "prevent_secret_media_deletion",
            "unlock_channel_features",
            "allow_save_videos",
        )

    private val EXCLUDED_FEATURES =
        mapOf(
            "it.octogram.android" to setOf("hide_phone"),
            "org.telegram.messenger" to emptySet(),
            "org.telegram.messenger.web" to emptySet(),
            "org.telegram.messenger.beta" to setOf("hide_phone"),
            "org.telegram.plus" to emptySet(),
            "tw.nekomimi.nekogram" to
                setOf(
                    "hide_phone",
                    "unlock_channel_features",
                ),
            "xyz.nextalone.nagram" to
                setOf(
                    "hide_phone",
                    "unlock_channel_features",
                ),
        )

    fun getFeaturesForPackage(packageName: String): Array<String> {
        val excluded = EXCLUDED_FEATURES[packageName] ?: emptySet()
        return FEATURES.filter { it !in excluded }.toTypedArray()
    }
}
