package com.aoya.telegami.core

object Constants {
    const val GITHUB_REPO = "https://github.com/aoya111/Telegami"

    val SUPPORTED_TELEGRAM_PACKAGES =
        mapOf(
            "it.octogram.android" to "Octogram",
            "nu.gpu.nagram" to "NagramX",
            "org.forkclient.messenger.beta" to "Forkgram GH",
            "org.forkgram.messenger" to "Forkgram",
            "org.telegram.group" to "Turrit",
            "org.telegram.messenger" to "Telegram",
            "org.telegram.messenger.beta" to "Telegram Beta",
            "org.telegram.messenger.web" to "Telegram Web",
            "org.telegram.plus" to "Telegram Plus",
            "tw.nekomimi.nekogram" to "Nekogram",
            "uz.unnarsx.cherrygram" to "Cherrygram",
            "xyz.nextalone.nagram" to "Nagram",
        )

    val SUPPORTED_TELEGRAM_PACKAGES_LIST = SUPPORTED_TELEGRAM_PACKAGES.keys.toList()
}
