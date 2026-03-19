package com.aoya.telegami.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.aoya.telegami.databinding.HookViewBinding
import dev.androidbroadcast.vbpd.CreateMethod
import dev.androidbroadcast.vbpd.viewBinding

class HookView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : LinearLayout(context, attrs, defStyleAttr) {
        val binding by viewBinding<HookViewBinding>(createMethod = CreateMethod.INFLATE)

        init {
            orientation = HORIZONTAL
            binding.toggle.setOnCheckedChangeListener { _, isChecked ->
                onToggleChanged?.invoke(isChecked)
            }
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

        fun showAsHeader() {
            binding.text.typeface = android.graphics.Typeface.DEFAULT_BOLD
            val paddingVertical = (resources.displayMetrics.density * 8f).toInt()
            binding.text.setPaddingRelative(
                0,
                paddingVertical,
                0,
                paddingVertical,
            )
            binding.toggle.isVisible = false
            binding.subText.isVisible = false
        }

        fun showAsChild(isLast: Boolean = false) {
            val indent = (resources.displayMetrics.density * 24f).toInt()
            val currentPaddingVertical = paddingTop
            setPaddingRelative(
                indent,
                currentPaddingVertical,
                paddingEnd,
                currentPaddingVertical,
            )
        }

        fun showAsToggleOnly() {
            binding.subText.isVisible = false
        }

        fun showAsStandalone() {
            // Default styling from XML
        }
    }
