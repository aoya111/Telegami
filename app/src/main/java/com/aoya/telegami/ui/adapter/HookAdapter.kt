package com.aoya.telegami.ui.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aoya.telegami.R
import com.aoya.telegami.ui.view.HookView

data class HookInfo(
    val key: String, // "HideSeenStatus" - for saving to prefs
    val name: String, // "Hide seen status" - for display
    val desc: String,
    val enabled: Boolean = true,
)

class HookAdapter(
    private val hooks: List<HookInfo>,
    private val onToggleChanged: (String, Boolean) -> Unit,
) : RecyclerView.Adapter<HookAdapter.HookViewHolder>() {
    inner class HookViewHolder(
        view: HookView,
    ) : RecyclerView.ViewHolder(view) {
        fun bind(
            hook: HookInfo,
            position: Int,
        ) {
            with(itemView as HookView) {
                text = hook.name
                subText = hook.desc
                toggle = hook.enabled
                onToggleChanged = { enabled ->
                    onToggleChanged(hook.key, enabled)
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): HookViewHolder {
        val view =
            HookView(parent.context).apply {
                layoutParams =
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
            }
        return HookViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: HookViewHolder,
        position: Int,
    ) {
        holder.bind(hooks[position], position)
    }

    override fun getItemCount(): Int = hooks.size
}
