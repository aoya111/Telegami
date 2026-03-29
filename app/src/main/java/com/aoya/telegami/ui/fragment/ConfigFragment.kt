package com.aoya.telegami.ui.fragment

import android.content.SharedPreferences
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.aoya.telegami.R
import com.aoya.telegami.core.Config
import com.aoya.telegami.databinding.FragmentConfigBinding
import com.aoya.telegami.ui.adapter.HookAdapter
import com.aoya.telegami.ui.adapter.HookInfo
import com.aoya.telegami.ui.view.HookViewType
import dev.androidbroadcast.vbpd.viewBinding

class ConfigFragment : Fragment(R.layout.fragment_config) {
    private val binding by viewBinding(FragmentConfigBinding::bind)

    private lateinit var prefs: SharedPreferences

    private val featureDependencies =
        mapOf(
            "MarkMessagesDeleted" to "ShowDeletedMessages",
        )

    private val dropdownFeatures =
        mapOf(
            "BoostDownload" to listOf("BoostDownloadOff", "BoostDownloadOn", "BoostDownloadExtreme"),
        )

    private fun loadHooks(): List<HookInfo> {
        val featureKeys = resources.getStringArray(R.array.features).toList()
        val hooks = mutableListOf<HookInfo>()

        val headers = featureKeys.filter { it.startsWith("Header") }.map { it.removePrefix("Header") }.toSet()

        for (hookKey in featureKeys) {
            if (hookKey.startsWith("Header")) {
                val name = hookKey.removePrefix("Header")
                val nameResId = resources.getIdentifier("Feat$name", "string", requireContext().packageName)
                hooks.add(
                    HookInfo(
                        key = name,
                        name = if (nameResId != 0) getString(nameResId) else name,
                        desc = "",
                        enabled = true,
                        isHeader = true,
                    ),
                )
                continue
            }

            val groupId = headers.find { hookKey.startsWith(it) }
            val options = dropdownFeatures[hookKey]

            if (groupId != null) {
                val nameResId = resources.getIdentifier("Feat$hookKey", "string", requireContext().packageName)
                val name = if (nameResId != 0) getString(nameResId) else hookKey

                val descResId = resources.getIdentifier("Feat${hookKey}Desc", "string", requireContext().packageName)
                val description = if (descResId != 0) getString(descResId) else ""

                hooks.add(
                    HookInfo(
                        key = hookKey,
                        name = name,
                        desc = description,
                        enabled = Config.isFeatureEnabledInActivity(requireContext(), hookKey),
                        isHeader = false,
                        groupId = groupId,
                        dependsOn = featureDependencies[hookKey],
                    ),
                )
            } else if (options != null) {
                val nameResId = resources.getIdentifier("Feat$hookKey", "string", requireContext().packageName)
                val name = if (nameResId != 0) getString(nameResId) else hookKey

                val descResId = resources.getIdentifier("Feat${hookKey}Desc", "string", requireContext().packageName)
                val description = if (descResId != 0) getString(descResId) else ""

                val optionLabels = options.map { getString(resources.getIdentifier(it, "string", requireContext().packageName)) }
                val selectedIndex = Config.getFeatureValueInActivity(requireContext(), hookKey, 0)

                hooks.add(
                    HookInfo(
                        key = hookKey,
                        name = name,
                        desc = description,
                        enabled = true,
                        type = HookViewType.DROPDOWN,
                        options = optionLabels,
                        selectedIndex = selectedIndex,
                    ),
                )
            } else {
                val nameResId = resources.getIdentifier("Feat$hookKey", "string", requireContext().packageName)
                val name = if (nameResId != 0) getString(nameResId) else hookKey

                val descResId = resources.getIdentifier("Feat${hookKey}Desc", "string", requireContext().packageName)
                val description = if (descResId != 0) getString(descResId) else ""

                hooks.add(
                    HookInfo(
                        key = hookKey,
                        name = name,
                        desc = description,
                        enabled = Config.isFeatureEnabledInActivity(requireContext(), hookKey),
                        dependsOn = featureDependencies[hookKey],
                    ),
                )
            }
        }

        return hooks
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: android.os.Bundle?,
    ) {
        binding.list.layoutManager = LinearLayoutManager(context)
        binding.list.adapter =
            HookAdapter(
                loadHooks(),
                onToggleChanged = { hookKey, enabled ->
                    Log.d("ConfigFragment", "Toggle $hookKey to $enabled")
                    Config.setFeatureEnabled(requireContext(), hookKey, enabled)
                },
                onSelectionChanged = { hookKey, index ->
                    Log.d("ConfigFragment", "Selection $hookKey to $index")
                    Config.setFeatureValue(requireContext(), hookKey, index)
                },
            )
    }
}
