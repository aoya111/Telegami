package com.aoya.telegami.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aoya.telegami.ui.view.HookView

data class HookInfo(
    val key: String,
    val name: String,
    val desc: String,
    val enabled: Boolean = true,
    val isHeader: Boolean = false,
    val groupId: String? = null,
)

class HookAdapter(
    private val hooks: List<HookInfo>,
    private val onToggleChanged: (String, Boolean) -> Unit,
) : RecyclerView.Adapter<HookAdapter.HookViewHolder>() {

    private fun isLastInGroup(position: Int): Boolean {
        if (position >= hooks.size - 1) return true
        val current = hooks[position]
        if (current.groupId == null) return true
        return hooks[position + 1].groupId != current.groupId
    }

    inner class HookViewHolder(
        view: HookView,
    ) : RecyclerView.ViewHolder(view) {
        fun bind(
            hook: HookInfo,
            position: Int,
        ) {
            with(itemView as HookView) {
                when {
                    hook.isHeader -> {
                        showAsHeader()
                        text = hook.name
                    }

                    hook.groupId != null -> {
                        showAsChild(isLastInGroup(position))
                        text = hook.name
                        subText = hook.desc
                        toggle = hook.enabled
                        onToggleChanged = { enabled ->
                            onToggleChanged(hook.key, enabled)
                        }
                    }

                    else -> {
                        showAsStandalone()
                        text = hook.name
                        subText = hook.desc
                        toggle = hook.enabled
                        onToggleChanged = { enabled ->
                            onToggleChanged(hook.key, enabled)
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): HookViewHolder {
        val view = HookView(parent.context)
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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
