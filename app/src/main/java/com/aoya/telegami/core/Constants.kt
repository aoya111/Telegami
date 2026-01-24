package com.aoya.telegami.core

object Constants {
    const val GITHUB_REPO = "https://github.com/aoya111/Telegami"

    val SUPPORTED_TELEGRAM_PACKAGES =
        arrayOf(
            "org.telegram.messenger",
            "org.telegram.messenger.web",
            "tw.nekomimi.nekogram",
        )

    val SUPPORTED_TG_VARIANTS =
        mapOf(
            "Nekogram" to Triple("tw.nekomimi.nekogram", "12.2.10S", 63470),
            "Telegram" to Triple("org.telegram.messenger", "12.3.1", 63859),
            "TelegramWeb" to Triple("org.telegram.messenger.web", "12.3.1", 63859),
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
            "tw.nekomimi.nekogram" to
                setOf(
                    "hide_phone",
                    "unlock_channel_features",
                ),
            "org.telegram.messenger" to emptySet(),
            "org.telegram.messenger.web" to emptySet(),
        )

    fun getFeaturesForPackage(packageName: String): Array<String> {
        val excluded = EXCLUDED_FEATURES[packageName] ?: emptySet()
        return FEATURES.filter { it !in excluded }.toTypedArray()
    }
}
