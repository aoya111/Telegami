package com.aoya.telegami.core.i18n

object I18NenUS : Translation {
    private val map =
        mapOf(
            "AppName" to "Telegami",
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
            "hide_seen_status" to "Hide 'Seen' status for messages",
            "hide_story_view_status" to "Hide 'Story View' status",
            "hide_typing" to "Hide Typing...",
            "unlock_channel_features" to "Unlock all restricted and encrypted features for channels",
            "show_delete_msg_button" to "Show 'Delete Messages' button",
            "allow_save_videos" to "Allow saving videos to the gallery",
            "developer_channel" to "Developer Channel",
            "hide_online_status" to "Hide online status",
            "prevent_secret_media_deletion" to "Prevent Deletion of Secret Media.",
            "hide_phone" to "Hide 'Phone' number",
            "show_deleted_messages" to "Show 'Deleted Messages'",
            "DeletedMessage" to "deleted",
            "copied_to_clipboard" to "Copied '{item}' to the clipboard",
            "ProfileStatusOffline" to "offline",
            "ProfileCopyUserId" to "Copy ID",
            "ProfileCopyChatId" to "Copy ID",
        )

    override fun get(key: String): String = map[key] ?: key
}
