package com.aoya.televip.core.obfuscate

object Nekogram : Resolver {
    private val map =
        mapOf(
            "org.telegram.messenger.AccountInstance" to "j3", // return\ ConnectionsManager\.getInstance\(this.\w*\)\;
            "org.telegram.messenger.AndroidUtilities" to "org.telegram.messenger.a", // Https\|ton
            "org.telegram.messenger.LocaleController" to "org.telegram.messenger.g0", // unofficial_base_
            "org.telegram.messenger.MessageObject" to "org.telegram.messenger.k0", // W\|lang
            "org.telegram.messenger.MessagesController" to "org.telegram.messenger.m0", // SELECT data FROM app_config
            "org.telegram.messenger.MessagesObject" to "org.telegram.messenger.k0", // versus "
            "org.telegram.messenger.MessagesStorage" to "org.telegram.messenger.n0", // "messages_holes"
            "org.telegram.messenger.NotificationCenter" to "org.telegram.messenger.o0", // VoIPService.ID_INCOMING_CALL_PRENOTIFICATION;
            "org.telegram.messenger.NotificationsController" to "org.telegram.messenger.p0", // EnableInChatSound
            "org.telegram.messenger.UserConfig" to "org.telegram.messenger.c1", // "2pinnedDialogsLoaded"
            "org.telegram.messenger.UserObject" to "org.telegram.messenger.d1", // return \w* == 333000
            "org.telegram.tgnet.tl.TL_account\$updateStatus" to "org.telegram.tgnet.TLRPC\$u7", // -1388733202
            "org.telegram.tgnet.TLRPC.Message" to "org.telegram.tgnet.TLRPC.h2", // -2082087340:
            "org.telegram.tgnet.TLRPC.User" to "org.telegram.tgnet.TLRPC.wd1", // -2093920310
            "org.telegram.ui.ActionBar.ActionBarMenuItem" to "org.telegram.ui.ActionBar.c", // headerItem type in ChatActivity
            "org.telegram.ui.ActionBar.ActionBarMenuItem.Item" to "org.telegram.ui.ActionBar.c.r",
            "org.telegram.ui.ActionBar.ActionBarMenuSubItem" to "org.telegram.ui.ActionBar.e",
            "org.telegram.ui.ActionBar.ActionBarPopupWindow\$GapView" to "org.telegram.ui.ActionBar.ActionBarPopupWindow\$d", // static class \w* extends FrameLayout {
            "org.telegram.ui.ActionBar.AlertDialog\$OnButtonClickListener" to "org.telegram.ui.ActionBar.AlertDialog\$k",
            "org.telegram.ui.ActionBar.BaseFragment" to "org.telegram.ui.ActionBar.g",
            "org.telegram.ui.ActionBar.SimpleTextView" to "uwe", // this.gravity = 51;
            "org.telegram.ui.ActionBar.Theme" to "org.telegram.ui.ActionBar.q",
            "org.telegram.ui.Cells.ChatMessageCell" to "h73", // this\.currentMessageObject\.isMediaSpoilersRevealed = true
            "org.telegram.ui.ChatActivity" to "org.telegram.ui.o", // headerItem;
            "org.telegram.ui.ChatActivity\$13" to "org.telegram.ui.o\$h0", // canDeleteHistory
            "org.telegram.ui.ChatActivity\$ChatActivityEnterViewDelegate" to "org.telegram.ui.o\$r4", // isEditTextItemVisibilitySuppressed;
            "org.telegram.ui.Components.ItemOptions" to "org.telegram.ui.Components.z1", // > 0\.705d
            "org.telegram.ui.Components.MessagePrivateSeenView" to "org.telegram.ui.Components.j2", // loading text
            "org.telegram.ui.PeerColorActivity" to "org.telegram.ui.z0", // particles = \{-18.
            "org.telegram.ui.Stories.PeerStoriesView\$StoryItemHolder" to "org.telegram.ui.Stories.e\$s0", // https://t.me/%1
            "org.telegram.ui.Stories.StoriesController" to "org.telegram.ui.Stories.i", // DELETE FROM profile_stories WHERE
        )

    private val methodMap =
        mapOf(
            "org.telegram.messenger.AndroidUtilities" to
                mapOf(
                    "addToClipboard" to "E", // public static boolean \w*(CharSequence
                ),
            "org.telegram.messenger.LocaleController" to
                mapOf(
                    "getFormatterDay" to "g1", // "formatterDay24H"
                    "getInstance" to "v1", // catch (Throwable
                ),
            "org.telegram.messenger.MessageObject" to
                mapOf(
                    "getDialogId" to "M0", // return \w*(this.messageOwner); (long)
                    "getId" to "B1", // return this.messageOwner.\w*; (int)
                ),
            "org.telegram.messenger.MessagesController" to
                mapOf(
                    "completeReadTask" to "b9",
                    "deleteMessages" to "m9", // (arraylist, arrayList2,
                    "getChat" to "fa", // return (TLRPC\.\w*) this\.\w*\.get(\w*);
                    "getUser" to "Mb", // return (TLRPC\.\w*) this\.\w*\.get(\w*); (with if)
                    "isChatNoForwards" to "ic", // if (\w*\.\w* || \w* == null)
                    "markDialogMessageAsDeleted" to "gl", // public void \w*(long \w*, ArrayList arrayList)
                ),
            "org.telegram.messenger.MessagesStorage" to
                mapOf("markMessagesAsDeletedInternal" to "rb", "updateDialogsWithDeletedMessagesInternal" to "ed"),
            "org.telegram.messenger.NotificationCenter" to
                mapOf(
                    "postNotificationName" to "F", // SystemClock.elapsedRealtime();
                ),
            "org.telegram.messenger.NotificationsController" to mapOf("removeDeletedMessagesFromNotifications" to "R1"),
            "org.telegram.messenger.time.FastDateFormat" to mapOf("format" to "a"),
            "org.telegram.messenger.UserConfig" to
                mapOf("getClientUserId" to "n", "getCurrentUser" to "o", "isPremium" to "C", "setCurrentUser" to "O"),
            "org.telegram.messenger.UserObject" to
                mapOf(
                    "getPublicUsername" to "q", // String \w*(<TLRPC.User> \w*)
                ),
            "org.telegram.SQLite.SQLiteCursor" to mapOf("intValue" to "g", "longValue" to "i", "next" to "j"),
            "org.telegram.SQLite.SQLiteDatabase" to mapOf("queryFinalized" to "h"),
            "org.telegram.ui.ActionBar.ActionBarMenuItem" to
                mapOf(
                    "lazilyAddColoredGap" to "e1",
                    "lazilyAddSubItem" to "h1", // (int \w*, Drawable \w*, CharSequence \w*)
                ),
            "org.telegram.ui.ActionBar.AlertDialog" to
                mapOf("setNegativeButton" to "v", "setPositiveButton" to "B", "setTitle" to "D", "setView" to "K", "show" to "N"),
            "org.telegram.ui.ActionBar.BaseFragment" to
                mapOf(
                    "getMessagesController" to "X0", // returns MessagesController
                    "getUserConfig" to "n1", // returns UserConfig
                ),
            "org.telegram.ui.ActionBar.SimpleTextView" to
                mapOf(
                    "setText" to "l0", // boolean \w*(CharSequence \w*)
                ),
            "org.telegram.ui.ActionBar.Theme" to mapOf("getActiveTheme" to "D1"),
            "org.telegram.ui.ActionBar.Theme\$ThemeInfo" to
                mapOf(
                    "isDark" to "J", // !"Dark Blue"\.equals(
                ),
            "org.telegram.ui.Adapters.DrawerLayoutAdapter" to
                mapOf(
                    "getId" to "T", // public int \w*(int \w*)
                    "resetItems" to "W", // public final void \w*()
                ),
            "org.telegram.ui.Cells.ChatMessageCell" to
                mapOf(
                    "getMessageObject" to "H5", // returns MessageObject
                    "measureTime" to "H8", // final void \w*(<MessageObject>
                ),
            "org.telegram.ui.ChatActivity" to
                mapOf(
                    "createMenu" to "ht", // "open menu
                    "createView" to "w0", // "ChatActivity.createView"
                    "getContext" to "I0", // \ \w*().getResources().getDrawable
                    "getResourceProvider" to "y", // AlertDialog.Builder(<see getContext>, \w*())
                    "lambda\$createMenu$290" to "Tw", // scrimPopupWindow\.showAtLocation(this\.chatListView, 51,
                    "scrollToMessageId" to "l", // public void \w*(int \w*, int \w*, boolean \w*, int \w*, boolean \w*, int \w*)
                    "sendSecretMediaDelete" to "XF", // Runnable \w*(final <MessageObject>
                    "sendSecretMessageRead" to "YF", // Runnable \w*(final <MessageObject>
                    "updatePagedownButtonVisibility" to "iI", // function below canShowPagedownButton = false;
                ),
            "org.telegram.ui.ChatActivity\$13" to mapOf("onItemClick" to "b"),
            "org.telegram.ui.ChatActivity\$ChatActivityEnterViewDelegate" to mapOf("needSendTyping" to "J"),
            "org.telegram.ui.Components.ItemOptions" to
                mapOf(
                    "add" to "P",
                    "setGravity" to "l1", // swipeBackGravityRight = true
                    "show" to "y1",
                ),
            "org.telegram.ui.Components.MessagePrivateSeenView" to mapOf("request" to "n"),
            "org.telegram.ui.LaunchActivity" to
                mapOf(
                    "lambda\$onCreate\$6" to "u6", // void \w*(View \w*, int \w*, float \w*, float \w*)
                ),
            "org.telegram.ui.PeerColorActivity" to
                mapOf(
                    "apply" to "e4", // public static int \w*(int \w*)
                ),
            "org.telegram.ui.ProfileActivity" to
                mapOf(
                    "createView" to "r1", // \ setWillNotDraw(false)
                    "editRow" to "He", // view)\.dialogCell
                    "getParentActivity" to "h", // \w*()\.getWindowManager()
                    "processOnClickOrPress" to "zi", // if (\w* != this\.usernameRow && \w* != this\.setUsernameRow) {
                    "updateProfileData" to "qj", // "avatar",
                ),
            "org.telegram.ui.SecretMediaViewer" to
                mapOf(
                    "closePhoto" to "n0", // isPhotoVisible |
                    "openMedia" to "M0", // animateFromRadius =
                ),
            "org.telegram.ui.Stories.PeerStoriesView\$StoryItemHolder" to mapOf("allowScreenshots" to "d"),
            "org.telegram.ui.Stories.StoriesController" to mapOf("markStoryAsRead" to "j2"),
        )

    private val fieldMap =
        mapOf(
            "org.telegram.messenger.MessagesStorage" to mapOf("database" to "b"), // UPDATE chat_settings_v2
            "org.telegram.messenger.NotificationCenter" to mapOf("messagesDeleted" to "x"),
            "org.telegram.tgnet.TLRPC.Message" to
                mapOf(
                    "ttl" to "h0", // if (\w*.messageOwner.\w* == Integer.MAX_VALUE)
                ),
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
