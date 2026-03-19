package com.aoya.telegami.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.aoya.telegami.ui.view.HookView

data class HookInfo(
    val key: String,
    val name: String,
    val desc: String,
    var enabled: Boolean = true,
    val isHeader: Boolean = false,
    val groupId: String? = null,
    val dependsOn: String? = null,
)

class HookAdapter(
    hooks: List<HookInfo>,
    private val onToggleChanged: (String, Boolean) -> Unit,
) : RecyclerView.Adapter<HookAdapter.HookViewHolder>() {
    private val hooks = hooks.toMutableList()

    private fun isLastInGroup(position: Int): Boolean {
        if (position >= hooks.size - 1) return true
        val current = hooks[position]
        if (current.groupId == null) return true
        return hooks[position + 1].groupId != current.groupId
    }

    private fun isDependencyMet(hook: HookInfo): Boolean {
        val depKey = hook.dependsOn ?: return true
        return hooks.any { it.key == depKey && it.enabled }
    }

    private fun onToggle(
        hookKey: String,
        enabled: Boolean,
    ) {
        val index = hooks.indexOfFirst { it.key == hookKey }
        if (index == -1) return

        hooks[index] = hooks[index].copy(enabled = enabled)
        onToggleChanged(hookKey, enabled)

        hooks.filter { it.dependsOn == hookKey }.forEach { dependent ->
            val depIndex = hooks.indexOf(dependent)
            if (depIndex != -1) {
                if (!enabled && dependent.enabled) {
                    hooks[depIndex] = dependent.copy(enabled = false)
                    onToggleChanged(dependent.key, false)
                }
                notifyItemChanged(depIndex)
            }
        }
    }

    inner class HookViewHolder(
        view: HookView,
    ) : RecyclerView.ViewHolder(view) {
        fun bind(
            hook: HookInfo,
            position: Int,
        ) {
            val dependencyMet = isDependencyMet(hook)
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
                        toggle = hook.enabled && dependencyMet
                        toggleEnabled = dependencyMet
                        onToggleChanged =
                            if (dependencyMet) {
                                { enabled ->
                                    onToggle(hook.key, enabled)
                                }
                            } else {
                                null
                            }
                    }

                    else -> {
                        showAsStandalone()
                        text = hook.name
                        subText = hook.desc
                        toggle = hook.enabled && dependencyMet
                        toggleEnabled = dependencyMet
                        onToggleChanged =
                            if (dependencyMet) {
                                { enabled ->
                                    onToggle(hook.key, enabled)
                                }
                            } else {
                                null
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
        view.layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
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
