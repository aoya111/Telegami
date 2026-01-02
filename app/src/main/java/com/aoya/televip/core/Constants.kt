package com.aoya.televip.core

object Constants {
    val SUPPORTED_TELEGRAM_PACKAGES =
        arrayOf(
            "org.telegram.messenger",
            "tw.nekomimi.nekogram",
        )
    val SUPPORTED_TG_VARIANTS =
        mapOf(
            "Nekogram" to Triple("tw.nekomimi.nekogram", "12.2.10S", 63470),
            "Telegram" to Triple("org.telegram.messenger", "12.2.11", 63422),
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
            "fake_premium",
        )
}
