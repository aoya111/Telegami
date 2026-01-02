package com.aoya.televip.core.i18n

object I18NenUS : Translation {
    private val map =
        mapOf(
            "app_name" to "TeleVip",
            "chat_scroll_to_top" to "Scroll to top",
            "input_msg_id" to "Input Message Id",
            "done" to "Done",
            "cancel" to "Cancel",
            "new_name" to "New Name",
            "change_name" to "Change Name",
            "delete_name" to "Delete Name",
            "change" to "Change",
            "change_to" to "Change to",
            "name_deleted" to "Name deleted",
            "ghost_mode" to "Ghost Mode 👻",
            "hide_seen_status" to "Hide 'Seen' status for messages",
            "hide_story_view_status" to "Hide 'Story View' status",
            "hide_typing" to "Hide Typing...",
            "fake_premium" to "Enable Telegram Premium",
            "unlock_channel_features" to "Unlock all restricted and encrypted features for channels",
            "show_delete_msg_button" to "Show 'Delete Messages' button",
            "allow_save_videos" to "Allow saving videos to the gallery",
            "ghost_mode_title" to "Ghost Mode",
            "save" to "Save",
            "developer_channel" to "Developer Channel",
            "hide_online_status" to "Hide 'Online' status",
            "prevent_secret_media_deletion" to "Prevent Deletion of Secret Media.",
            "hide_phone" to "Hide 'Phone' number",
            "show_deleted_messages" to "Show 'Deleted Messages'",
            "DeletedMessage" to "deleted",
            "copied_to_clipboard" to "Copied '{item}' to the clipboard",
            "offline_status" to "You are currently offline",
            "ProfileCopyUserId" to "Copy ID",
            "ProfileCopyChatId" to "Copy ID",
        )

    override fun get(key: String): String = map[key] ?: key
}
