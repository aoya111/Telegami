package com.aoya.telegami.ui.view

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.core.view.isVisible
import com.aoya.telegami.R
import com.aoya.telegami.databinding.HookViewBinding
import com.google.android.material.card.MaterialCardView
import dev.androidbroadcast.vbpd.CreateMethod
import dev.androidbroadcast.vbpd.viewBinding

class HookView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : MaterialCardView(context, attrs, defStyleAttr) {
        val binding by viewBinding<HookViewBinding>(createMethod = CreateMethod.INFLATE)

        init {
            // Set default state
            binding.toggle.isChecked = true
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
                binding.subText.visibility = if (value != null && value.isNotEmpty()) VISIBLE else GONE
                binding.subText.text = value
            }

        var toggle: Boolean
            get() = binding.toggle.isChecked
            set(value) {
                binding.toggle.isChecked = value
            }

        var onToggleChanged: ((Boolean) -> Unit)? = null

        fun showAsHeader() {
            with(binding.text) {
                typeface = Typeface.DEFAULT_BOLD
                val padding = (resources.displayMetrics.density * 8f).toInt()
                setPaddingRelative(paddingStart, padding, paddingEnd, padding)
            }
        }
    }
