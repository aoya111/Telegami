package com.aoya.televip.core.obfuscate

object Nekogram : Resolver {
    private val map =
        mapOf(
            "org.telegram.messenger.FileLoadOperation" to "org.telegram.messenger.p",
            "org.telegram.messenger.LocaleController" to "org.telegram.messenger.B",
            "org.telegram.messenger.MessagesController" to "org.telegram.messenger.H",
            "org.telegram.messenger.MessagesStorage" to "org.telegram.messenger.I",
            "org.telegram.messenger.NotificationCenter" to "org.telegram.messenger.J",
            "org.telegram.messenger.NotificationsController" to "org.telegram.messenger.K",
            "org.telegram.messenger.R\$drawable" to "CU2",
            "org.telegram.messenger.UserConfig" to "org.telegram.messenger.X",
            "org.telegram.tgnet.TLRPC.User" to "org.telegram.tgnet.TLRPC.XD",
            "org.telegram.ui.ActionBar.ActionBarMenuSubItem" to "org.telegram.ui.ActionBar.e",
            "org.telegram.ui.ActionBar.ActionBarPopupWindow\$GapView" to "org.telegram.ui.ActionBar.ActionBarPopupWindow\$d",
            "org.telegram.ui.ActionBar.AlertDialog\$OnButtonClickListener" to "org.telegram.ui.ActionBar.AlertDialog\$k",
            "org.telegram.ui.ActionBar.BaseFragment" to "org.telegram.ui.ActionBar.g",
            "org.telegram.ui.ActionBar.Theme" to "org.telegram.ui.ActionBar.q",
            "org.telegram.ui.Cells.ChatMessageCell" to "F80",
            "org.telegram.ui.ChatActivity" to "org.telegram.ui.p",
            "org.telegram.ui.ChatActivity\$13" to "org.telegram.ui.p\$H",
            "org.telegram.ui.ChatActivity\$ChatActivityEnterViewDelegate" to "org.telegram.ui.p\$h2",
            "org.telegram.ui.Components.MessagePrivateSeenView" to "org.telegram.ui.Components.H0",
            "org.telegram.ui.PeerColorActivity" to "org.telegram.ui.Z",
            "org.telegram.ui.ProfileActivity\$6" to "org.telegram.ui.ProfileActivity\$Z",
            "org.telegram.ui.Stories.PeerStoriesView\$StoryItemHolder" to "org.telegram.ui.Stories.c\$P",
            "org.telegram.ui.Stories.StoriesController" to "org.telegram.ui.Stories.g",
        )

    private val methodMap =
        mapOf(
            "org.telegram.messenger.FileLoadOperation" to mapOf("updateParams" to "b1"),
            "org.telegram.messenger.LocaleController" to mapOf("getFormatterDay" to "b1", "getInstance" to "q1"),
            "org.telegram.messenger.MessageObject" to mapOf("getDialogId" to "I0", "getId" to "m1"),
            "org.telegram.messenger.MessagesController" to
                mapOf(
                    "completeReadTask" to "H8",
                    "deleteMessages" to "T8",
                    "getChat" to "H9",
                    "getUser" to "kb",
                    "isChatNoForwards" to "Eb",
                    "markDialogMessageAsDeleted" to "mk",
                ),
            "org.telegram.messenger.MessagesStorage" to
                mapOf("markMessagesAsDeletedInternal" to "Wa", "updateDialogsWithDeletedMessagesInternal" to "S9"),
            "org.telegram.messenger.NotificationCenter" to mapOf("postNotificationName" to "F"),
            "org.telegram.messenger.NotificationsController" to mapOf("removeDeletedMessagesFromNotifications" to "R1"),
            "org.telegram.messenger.time.FastDateFormat" to mapOf("format" to "a"),
            "org.telegram.messenger.UserConfig" to mapOf("getCurrentUser" to "o", "isPremium" to "B", "setCurrentUser" to "N"),
            "org.telegram.SQLite.SQLiteCursor" to mapOf("intValue" to "g", "longValue" to "i", "next" to "j"),
            "org.telegram.SQLite.SQLiteDatabase" to mapOf("queryFinalized" to "h"),
            "org.telegram.ui.ActionBar.ActionBarMenuItem" to
                mapOf("addSubItem" to "f0", "lazilyAddColoredGap" to "f1", "lazilyAddSubItem" to "h1"),
            "org.telegram.ui.ActionBar.AlertDialog" to
                mapOf("setNegativeButton" to "v", "setPositiveButton" to "B", "setTitle" to "D", "setView" to "K", "show" to "N"),
            "org.telegram.ui.ActionBar.BaseFragment" to mapOf("getMessagesController" to "T0"),
            "org.telegram.ui.ActionBar.Theme" to mapOf("getActiveTheme" to "x1"),
            "org.telegram.ui.ActionBar.Theme\$ThemeInfo" to mapOf("isDark" to "J"),
            "org.telegram.ui.Adapters.DrawerLayoutAdapter" to mapOf("getId" to "S", "resetItems" to "V"),
            "org.telegram.ui.Cells.ChatMessageCell" to mapOf("getMessageObject" to "z5", "measureTime" to "m7"),
            "org.telegram.ui.ChatActivity" to
                mapOf(
                    "createMenu" to "Pr",
                    "createView" to "t0",
                    "getContext" to "F0",
                    "getResourceProvider" to "x",
                    "lambda\$createMenu$290" to "gv",
                    "scrollToMessageId" to "k",
                    "sendSecretMediaDelete" to "ND",
                    "sendSecretMessageRead" to "OD",
                    "updatePagedownButtonVisibility" to "OF",
                ),
            "org.telegram.ui.ChatActivity\$13" to mapOf("onItemClick" to "b"),
            "org.telegram.ui.ChatActivity\$ChatActivityEnterViewDelegate" to mapOf("needSendTyping" to "G"),
            "org.telegram.ui.Components.MessagePrivateSeenView" to mapOf("request" to "n"),
            "org.telegram.ui.LaunchActivity" to mapOf("lambda\$onCreate\$6" to "M5"),
            "org.telegram.ui.PeerColorActivity" to mapOf("apply" to "J3"),
            "org.telegram.ui.ProfileActivity" to mapOf("createActionBarMenu" to "dd", "createView" to "r0", "getParentActivity" to "h"),
            "org.telegram.ui.ProfileActivity\$6" to mapOf("onItemClick" to "b"),
            "org.telegram.ui.Stories.PeerStoriesView\$StoryItemHolder" to mapOf("allowScreenshots" to "c"),
            "org.telegram.ui.Stories.StoriesController" to mapOf("markStoryAsRead" to "O1"),
        )

    private val fieldMap =
        mapOf(
            "org.telegram.messenger.FileLoadOperation" to
                mapOf(
                    "downloadChunkSizeBig" to "l",
                    "maxCdnParts" to "q",
                    "maxDownloadRequests" to "n",
                    "maxDownloadRequestsBig" to "o",
                ),
            "org.telegram.messenger.MessagesStorage" to mapOf("database" to "e"),
            "org.telegram.messenger.NotificationCenter" to mapOf("messagesDeleted" to "x"),
            "org.telegram.tgnet.TLRPC.User" to mapOf("phone" to "f", "id" to "a", "username" to "d"),
            "org.telegram.ui.ActionBar.Theme" to mapOf("chat_timePaint" to "L2"),
            "org.telegram.ui.Adapters.DrawerLayoutAdapter" to mapOf("items" to "d"),
            "org.telegram.ui.Adapters.DrawerLayoutAdapter\$Item" to mapOf("icon" to "a", "id" to "c"),
        )

    override fun get(className: String): String = map[className] ?: className

    override fun getMethod(
        className: String,
        methodName: String,
    ): String = methodMap[className]?.get(methodName) ?: methodName

    override fun getField(
        className: String,
        fieldName: String,
    ): String = fieldMap[className]?.get(fieldName) ?: fieldName
}
