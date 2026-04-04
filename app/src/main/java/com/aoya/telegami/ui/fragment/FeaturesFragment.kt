package com.aoya.telegami.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.aoya.telegami.R
import com.aoya.telegami.databinding.FragmentFeaturesBinding
import com.aoya.telegami.service.PrefManager
import com.aoya.telegami.ui.adapter.HookAdapter
import com.aoya.telegami.ui.adapter.HookInfo
import com.aoya.telegami.ui.util.navController
import com.aoya.telegami.ui.util.setEdge2EdgeFlags
import com.aoya.telegami.ui.util.setupToolbar
import com.aoya.telegami.ui.view.HookViewType
import dev.androidbroadcast.vbpd.viewBinding

class FeaturesFragment : Fragment(R.layout.fragment_features) {
    private val binding by viewBinding(FragmentFeaturesBinding::bind)

    private var hookAdapter: HookAdapter? = null

    private val featureDependencies =
        mapOf(
            "MarkMessagesDeleted" to "ShowDeletedMessages",
        )

    private val dropdownFeatures =
        mapOf(
            "BoostDownload" to listOf("BoostDownloadOff", "BoostDownloadOn", "BoostDownloadExtreme"),
        )

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        with(binding.toolbar) {
            setupToolbar(
                toolbar = binding.toolbar,
                title = getString(R.string.TitleFeatures),
                navigationIcon = R.drawable.baseline_arrow_back_24,
                navigationOnClick = { navController.navigateUp() },
            )
        }

        setEdge2EdgeFlags(binding.root)
    }

    override fun onStart() {
        super.onStart()
        with(binding.list) {
            layoutManager = LinearLayoutManager(context)
            hookAdapter =
                HookAdapter(
                    loadHooks(),
                    onToggleChanged = { hookKey, enabled ->
                        PrefManager.setFeatureEnabled(hookKey, enabled)
                    },
                    onSelectionChanged = { hookKey, index ->
                        PrefManager.setFeatureValue(hookKey, index)
                    },
                )
            adapter = hookAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        hookAdapter?.notifyDataSetChanged()
    }

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

                val enabled = PrefManager.isFeatureEnabled(hookKey)

                hooks.add(
                    HookInfo(
                        key = hookKey,
                        name = name,
                        desc = description,
                        enabled = enabled,
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

                val optionLabels =
                    options.map { getString(resources.getIdentifier(it, "string", requireContext().packageName)) }
                val selectedIndex = PrefManager.getFeatureValue(hookKey, 0)

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

                val enabled = PrefManager.isFeatureEnabled(hookKey)

                hooks.add(
                    HookInfo(
                        key = hookKey,
                        name = name,
                        desc = description,
                        enabled = enabled,
                        dependsOn = featureDependencies[hookKey],
                    ),
                )
            }
        }

        return hooks
    }
}
