package com.aoya.telegami.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.PopupMenu
import androidx.core.view.isVisible
import com.aoya.telegami.databinding.ViewHookBinding
import dev.androidbroadcast.vbpd.CreateMethod
import dev.androidbroadcast.vbpd.viewBinding

enum class HookViewType {
    TOGGLE,
    DROPDOWN,
}

class HookView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : LinearLayout(context, attrs, defStyleAttr) {
        val binding: ViewHookBinding by viewBinding(createMethod = CreateMethod.INFLATE)

        private var viewType = HookViewType.TOGGLE
        private var options: List<String> = emptyList()
        private var _selectedIndex: Int = 0

        init {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            binding.toggle.setOnCheckedChangeListener { _, isChecked ->
                onToggleChanged?.invoke(isChecked)
            }
            binding.dropdown.setOnClickListener { showDropdownMenu() }
        }

        var text: CharSequence?
            get() = binding.text.text
            set(value) {
                binding.text.text = value
            }

        var subText: CharSequence?
            get() = binding.subText.text
            set(value) {
                binding.subText.isVisible = value != null && value.isNotEmpty()
                binding.subText.text = value
            }

        var toggle: Boolean
            get() = binding.toggle.isChecked
            set(value) {
                binding.toggle.isChecked = value
            }

        var toggleEnabled: Boolean
            get() = binding.toggle.isEnabled
            set(value) {
                binding.toggle.isEnabled = value
            }

        var onToggleChanged: ((Boolean) -> Unit)? = null

        var type: HookViewType
            get() = viewType
            set(value) {
                viewType = value
                binding.toggle.isVisible = value == HookViewType.TOGGLE
                binding.dropdown.isVisible = value == HookViewType.DROPDOWN
            }

        var dropdownOptions: List<String>
            get() = options
            set(value) {
                options = value
                updateDropdownText()
            }

        var selectedIndex: Int
            get() = _selectedIndex
            set(value) {
                _selectedIndex = value
                updateDropdownText()
            }

        var onSelectionChanged: ((Int) -> Unit)? = null

        private fun updateDropdownText() {
            if (options.isNotEmpty() && _selectedIndex in options.indices) {
                binding.dropdown.text = options[_selectedIndex]
            }
        }

        private fun showDropdownMenu() {
            if (options.isEmpty()) return

            val popup = PopupMenu(context, binding.dropdown)
            options.forEachIndexed { index, option ->
                popup.menu.add(0, index, index, option)
            }

            popup.setOnMenuItemClickListener { item ->
                selectedIndex = item.itemId
                onSelectionChanged?.invoke(item.itemId)
                true
            }

            popup.show()
        }

        fun showAsHeader() {
            binding.text.typeface = android.graphics.Typeface.DEFAULT_BOLD
            val paddingTop = (resources.displayMetrics.density * 8f).toInt()
            val paddingBottom = (resources.displayMetrics.density * 4f).toInt()
            binding.text.setPaddingRelative(
                0,
                paddingTop,
                0,
                paddingBottom,
            )
            binding.toggle.isVisible = false
            binding.dropdown.isVisible = false
            binding.subText.isVisible = false
        }

        fun showAsChild(isLast: Boolean = false) {
            val indent = (resources.displayMetrics.density * 24f).toInt()
            val paddingVertical = (resources.displayMetrics.density * 2f).toInt()
            val currentPaddingEnd = paddingEnd
            setPaddingRelative(
                indent,
                paddingVertical,
                currentPaddingEnd,
                paddingVertical,
            )
        }

        fun showAsToggleOnly() {
            binding.subText.isVisible = false
        }

        fun showAsStandalone() {
            // Default styling from XML
        }

        fun showAsDropdown() {
            type = HookViewType.DROPDOWN
        }
    }
