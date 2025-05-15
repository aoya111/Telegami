package com.aoya.televip.core.i18n

object I18NzhCN : Translation {
    private val map =
        mapOf(
            "app_name" to "TeleVip",
            "input_msg_id" to "输入消息 ID",
            "done" to "完成",
            "cancel" to "取消",
            "new_name" to "新名字",
            "change_name" to "更改名称",
            "delete_name" to "删除名称",
            "change" to "更改",
            "change_to" to "Change to",
            "name_deleted" to "名称已删除",
            "ghost_mode" to "幽灵模式 👻",
            "hide_seen_status" to "隐藏消息的 '已读' 状态",
            "hide_story_view_status" to "隐藏 '故事观看' 状态",
            "hide_typing" to "隐藏正在输入...",
            "enable_premium" to "启用 Telegram 本地会员",
            "unlock_channel_features" to "解锁频道的所有受限和加密功能",
            "show_delete_msg_button" to "显示 '删除消息' 按钮",
            "allow_save_videos" to "允许将视频保存到图库",
            "ghost_mode_title" to "幽灵模式",
            "save" to "保存",
            "developer_channel" to "开发者频道",
            "hide_online_status" to "隐藏 '在线' 状态",
            "prevent_secret_media_deletion" to "防止删除秘密媒体",
            "hide_phone_number" to "隐藏 '电话' 号码",
            "show_deleted_messages" to "显示已删除的消息",
            "DeletedMessage" to "已删除",
            "copied_to_clipboard" to "已复制 '{item}' 到剪贴板",
            "offline_status" to "您当前处于离线状态",
        )

    override fun get(key: String): String = map[key] ?: key
}
