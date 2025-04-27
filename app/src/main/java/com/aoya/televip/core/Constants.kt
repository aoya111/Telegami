package com.aoya.televip.core

object Constants {
    val SUPPORTED_TELEGRAM_PACKAGES =
        arrayOf(
            "org.telegram.messenger",
            "org.telegram.messenger.web",
            "org.telegram.messenger.beta",
            "com.iMe.android",
            "tw.nekomimi.nekogram",
        )
    val SUPPORTED_TG_VARIANTS =
        mapOf(
            "Cherrygram" to Triple("uz.unnarsx.cherrygram", "11.5.3", 55110),
            "exteraGram" to Triple("org.exteragram.app", "11.5.3", 55119),
            "ForkClient Beta" to Triple("org.forkclient.messenger.beta", "11.5.3.0", 551109),
            "ForkGram" to Triple("org.forkgram.messenger", "11.5.3.0", 551108),
            "iMe" to Triple("com.iMe.android", "11.5.3", 11050302),
            "Nagram" to Triple("xyz.nextalone.nagram", "11.5.3", 1202),
            "Nekogram" to Triple("tw.nekomimi.nekogram", "11.5.3S", 55150),
            "Nicegram" to Triple("app.nicegram", "1.39.3", 1529),
            "Plus" to Triple("org.telegram.plus", "11.5.3.0", 21340),
            "Skygram" to Triple("com.skyGram.bestt", "10.20.6", 40639),
            "Telegram" to Triple("org.telegram.messenger", "11.6.1", 55852),
            "Telegram Beta" to Triple("org.telegram.messenger.beta", "11.6.2", 56159),
            "Telegram Web" to Triple("org.telegram.messenger.web", "11.6.1", 55859),
            "TG Connect" to Triple("org.tgconnect.app", "11.0.1", 1100109),
            "X Plus" to Triple("com.xplus.messenger", "11.5.4", 55159),
        )
    val FEATURES =
        arrayOf(
            "hide_seen_status",
            "hide_story_view_status",
            "hide_online_status",
            "hide_phone_number",
            "hide_typing",
            "show_deleted_messages",
            "prevent_secret_media_deletion",
            "unlock_channel_features",
            "allow_save_videos",
            "enable_premium",
        )
}
