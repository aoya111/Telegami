package com.aoya.televip.core.i18n

object I18NarSA : Translation {
    private val map =
        mapOf(
            "app_name" to "TeleVip",
            "input_msg_id" to "ادخل معرف الرسالة",
            "done" to "حسناً",
            "cancel" to "الغاء",
            "new_name" to "الاسم الجديد",
            "change_name" to "تغير الاسم",
            "delete_name" to "حذف الاسم",
            "change" to "تغير",
            "change_to" to "Change to",
            "name_deleted" to "تم حذف الاسم",
            "ghost_mode" to "وضع الشبح 👻",
            "hide_seen_status" to "اخفاء علامة الاستلام",
            "hide_story_view_status" to "اخفاء مشاهدة قصة",
            "hide_typing" to "اخفاء مؤشر الكتاب",
            "enable_premium" to "فتح تيليجرام المميز",
            "unlock_channel_features" to "فتح جميع الخصائص المشفره والمغلقه",
            "show_delete_msg_button" to "عرض زر حذف الرسائل",
            "allow_save_videos" to "سماح حفظ الفيديو في معرض",
            "ghost_mode_title" to "مميزات وضع شبح",
            "save" to "حفظ",
            "developer_channel" to "قناة المطور",
            "hide_online_status" to "إخفاء حالة الاتصال بالإنترنت",
            "prevent_secret_media_deletion" to "تعطيل حذف الوسائط السرية",
            "hide_phone" to "اخفاء رقم هاتف",
            "show_deleted_messages" to "اضهار الرسائل المحذوفة",
            "DeletedMessage" to "محذوفه",
            "copied_to_clipboard" to "تم نسخ '{item}' إلى الحافظة",
            "offline_status" to "لست متصلاً بالإنترنت",
        )

    override fun get(key: String): String = map[key] ?: key
}
