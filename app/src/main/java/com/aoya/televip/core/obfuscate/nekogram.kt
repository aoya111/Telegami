package com.aoya.televip.core.obfuscate

object Nekogram : Resolver {
    private val map =
        mapOf(
            "org.telegram.messenger.AccountInstance" to "o1",
            "org.telegram.messenger.LocaleController" to "org.telegram.messenger.E",
            "org.telegram.messenger.MessageObject" to "org.telegram.messenger.I",
            "org.telegram.messenger.MessagesController" to "org.telegram.messenger.K",
            "org.telegram.messenger.MessagesStorage" to "org.telegram.messenger.L",
            "org.telegram.messenger.NotificationCenter" to "org.telegram.messenger.M",
            "org.telegram.messenger.NotificationsController" to "org.telegram.messenger.N",
            "org.telegram.messenger.UserConfig" to "org.telegram.messenger.a0",
            "org.telegram.tgnet.tl.TL_account\$updateStatus" to "org.telegram.tgnet.TLRPC\$u7",
            "org.telegram.tgnet.TLRPC.User" to "org.telegram.tgnet.TLRPC.YE",
            "org.telegram.ui.ActionBar.ActionBarMenuSubItem" to "org.telegram.ui.ActionBar.e",
            "org.telegram.ui.ActionBar.ActionBarPopupWindow\$GapView" to "org.telegram.ui.ActionBar.ActionBarPopupWindow\$d",
            "org.telegram.ui.ActionBar.AlertDialog\$OnButtonClickListener" to "org.telegram.ui.ActionBar.AlertDialog\$k",
            "org.telegram.ui.ActionBar.BaseFragment" to "org.telegram.ui.ActionBar.g",
            "org.telegram.ui.ActionBar.SimpleTextView" to "vq3",
            "org.telegram.ui.ActionBar.Theme" to "org.telegram.ui.ActionBar.q",
            "org.telegram.ui.Cells.ChatMessageCell" to "oa0",
            "org.telegram.ui.ChatActivity" to "org.telegram.ui.o",
            "org.telegram.ui.ChatActivity\$13" to "org.telegram.ui.o\$H",
            "org.telegram.ui.ChatActivity\$ChatActivityEnterViewDelegate" to "org.telegram.ui.o\$m2",
            "org.telegram.ui.Components.ItemOptions" to "org.telegram.ui.Components.Q0",
            "org.telegram.ui.Components.MessagePrivateSeenView" to "org.telegram.ui.Components.J0",
            "org.telegram.ui.PeerColorActivity" to "org.telegram.ui.Z",
            "org.telegram.ui.ProfileActivity\$6" to "org.telegram.ui.ProfileActivity\$Z",
            "org.telegram.ui.Stories.PeerStoriesView\$StoryItemHolder" to "org.telegram.ui.Stories.c\$P",
            "org.telegram.ui.Stories.StoriesController" to "org.telegram.ui.Stories.g",
        )

    private val methodMap =
        mapOf(
            "org.telegram.messenger.LocaleController" to mapOf("getFormatterDay" to "d1", "getInstance" to "s1"),
            "org.telegram.messenger.MessageObject" to mapOf("getDialogId" to "S0", "getId" to "x1"),
            "org.telegram.messenger.MessagesController" to
                mapOf(
                    "completeReadTask" to "V8",
                    "deleteMessages" to "h9",
                    "getChat" to "Y9",
                    "getUser" to "Eb",
                    "isChatNoForwards" to "Yb",
                    "markDialogMessageAsDeleted" to "Tk",
                ),
            "org.telegram.messenger.MessagesStorage" to
                mapOf("markMessagesAsDeletedInternal" to "pb", "updateDialogsWithDeletedMessagesInternal" to "ja"),
            "org.telegram.messenger.NotificationCenter" to mapOf("postNotificationName" to "F"),
            "org.telegram.messenger.NotificationsController" to mapOf("removeDeletedMessagesFromNotifications" to "S1"),
            "org.telegram.messenger.time.FastDateFormat" to mapOf("format" to "a"),
            "org.telegram.messenger.UserConfig" to
                mapOf("getClientUserId" to "n", "getCurrentUser" to "o", "isPremium" to "C", "setCurrentUser" to "O"),
            "org.telegram.SQLite.SQLiteCursor" to mapOf("intValue" to "g", "longValue" to "i", "next" to "j"),
            "org.telegram.SQLite.SQLiteDatabase" to mapOf("queryFinalized" to "h"),
            "org.telegram.ui.ActionBar.ActionBarMenuItem" to
                mapOf("addSubItem" to "f0", "lazilyAddColoredGap" to "f1", "lazilyAddSubItem" to "h1"),
            "org.telegram.ui.ActionBar.AlertDialog" to
                mapOf("setNegativeButton" to "v", "setPositiveButton" to "B", "setTitle" to "D", "setView" to "K", "show" to "N"),
            "org.telegram.ui.ActionBar.BaseFragment" to mapOf("getMessagesController" to "T0", "getUserConfig" to "i1"),
            "org.telegram.ui.ActionBar.SimpleTextView" to mapOf("setText" to "m0"),
            "org.telegram.ui.ActionBar.Theme" to mapOf("getActiveTheme" to "y1"),
            "org.telegram.ui.ActionBar.Theme\$ThemeInfo" to mapOf("isDark" to "J"),
            "org.telegram.ui.Adapters.DrawerLayoutAdapter" to mapOf("getId" to "S", "resetItems" to "V"),
            "org.telegram.ui.Cells.ChatMessageCell" to mapOf("getMessageObject" to "D5", "measureTime" to "E7"),
            "org.telegram.ui.ChatActivity" to
                mapOf(
                    "createMenu" to "bt",
                    "createView" to "t0",
                    "getContext" to "F0",
                    "getResourceProvider" to "x",
                    "lambda\$createMenu$290" to "Gw",
                    "scrollToMessageId" to "k",
                    "sendSecretMediaDelete" to "KF",
                    "sendSecretMessageRead" to "LF",
                    "updatePagedownButtonVisibility" to "OF",
                ),
            "org.telegram.ui.ChatActivity\$13" to mapOf("onItemClick" to "b"),
            "org.telegram.ui.ChatActivity\$ChatActivityEnterViewDelegate" to mapOf("needSendTyping" to "H"),
            "org.telegram.ui.Components.MessagePrivateSeenView" to mapOf("request" to "n"),
            "org.telegram.ui.LaunchActivity" to mapOf("lambda\$onCreate\$6" to "g6"),
            "org.telegram.ui.PeerColorActivity" to mapOf("apply" to "J3"),
            "org.telegram.ui.ProfileActivity" to
                mapOf(
                    "createActionBarMenu" to "dd",
                    "createView" to "r0",
                    "editRow" to "qe",
                    "getParentActivity" to "h",
                    "processOnClickOrPress" to "Zh",
                    "updateProfileData" to "Pi",
                ),
            "org.telegram.ui.ProfileActivity\$6" to mapOf("onItemClick" to "b"),
            "org.telegram.ui.SecretMediaViewer" to mapOf("closePhoto" to "n0", "openMedia" to "M0"),
            "org.telegram.ui.Stories.PeerStoriesView\$StoryItemHolder" to mapOf("allowScreenshots" to "c"),
            "org.telegram.ui.Stories.StoriesController" to mapOf("markStoryAsRead" to "c2"),
        )

    private val fieldMap =
        mapOf(
            "org.telegram.messenger.MessagesStorage" to mapOf("database" to "l"),
            "org.telegram.messenger.NotificationCenter" to mapOf("messagesDeleted" to "x"),
            "org.telegram.tgnet.TLRPC.Message" to mapOf("ttl" to "g0"),
            "org.telegram.tgnet.TLRPC.User" to mapOf("phone" to "f", "id" to "a", "username" to "d"),
            "org.telegram.ui.ActionBar.Theme" to mapOf("chat_timePaint" to "L2"),
            "org.telegram.ui.Adapters.DrawerLayoutAdapter" to mapOf("items" to "h"),
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
