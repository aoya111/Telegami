package com.aoya.telegami.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import com.aoya.telegami.R
import com.aoya.telegami.databinding.FragmentSettingsBinding
import com.aoya.telegami.service.PrefManager
import com.aoya.telegami.ui.util.navController
import com.aoya.telegami.ui.util.setEdge2EdgeFlags
import com.aoya.telegami.ui.util.setupToolbar
import com.aoya.telegami.utils.toPascalCase
import dev.androidbroadcast.vbpd.viewBinding

class FeaturesFragment :
    Fragment(R.layout.fragment_settings),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private val binding by viewBinding(FragmentSettingsBinding::bind)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        setupToolbar(
            toolbar = binding.toolbar,
            title = getString(R.string.title_features),
            navigationIcon = R.drawable.baseline_arrow_back_24,
            navigationOnClick = { navController.navigateUp() },
        )

        setEdge2EdgeFlags(binding.root)

        if (childFragmentManager.findFragmentById(R.id.settings_container) == null) {
            childFragmentManager
                .beginTransaction()
                .replace(R.id.settings_container, SettingsPreferenceFragment())
                .commit()
        }
    }

    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: androidx.preference.Preference,
    ): Boolean {
        val fragment =
            childFragmentManager.fragmentFactory.instantiate(
                requireContext().classLoader,
                pref.fragment!!,
            )
        fragment.arguments = pref.extras
        childFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, fragment)
            .addToBackStack(null)
            .commit()
        return true
    }

    class SettingsPreferenceDataStore : PreferenceDataStore() {
        private val booleanKeys =
            setOf(
                "hideOnlineStatus",
                "hideTyping",
                "hideStoryViewStatus",
                "showDeletedMessages",
                "preventSecretMediaDeletion",
                "allowSaveVideos",
                "unlockChannelFeatures",
                "fakePremium",
                "disableAds",
            )

        private val stringKeys = setOf("boostDownload")

        private val multiSelectKeys = setOf("hideSeen", "markMessages")

        override fun getBoolean(
            key: String,
            defValue: Boolean,
        ): Boolean =
            if (key in booleanKeys) {
                PrefManager.isFeatureEnabled(key.toPascalCase())
            } else {
                throw IllegalArgumentException("Invalid key: $key")
            }

        override fun getString(
            key: String,
            defValue: String?,
        ): String =
            if (key in stringKeys) {
                PrefManager.getFeatureValue(key.toPascalCase(), 0).toString()
            } else {
                throw IllegalArgumentException("Invalid key: $key")
            }

        override fun getStringSet(
            key: String,
            defValues: MutableSet<String>?,
        ): MutableSet<String> =
            if (key in multiSelectKeys) {
                val pascalKey = key.toPascalCase()
                val enabledOptions = mutableSetOf<String>()
                when (key) {
                    "hideSeen" -> {
                        if (PrefManager.isFeatureEnabled("HideSeenPrivateChat")) enabledOptions.add("private_chat")
                        if (PrefManager.isFeatureEnabled("HideSeenChannel")) enabledOptions.add("channel")
                    }
                    "markMessages" -> {
                        if (PrefManager.isFeatureEnabled("MarkMessagesDeleted")) enabledOptions.add("deleted")
                        if (PrefManager.isFeatureEnabled("MarkMessagesEdited")) enabledOptions.add("edited")
                    }
                }
                enabledOptions
            } else {
                throw IllegalArgumentException("Invalid key: $key")
            }

        override fun putBoolean(
            key: String,
            value: Boolean,
        ) {
            if (key in booleanKeys) {
                PrefManager.setFeatureEnabled(key.toPascalCase(), value)
            } else {
                throw IllegalArgumentException("Invalid key: $key")
            }
        }

        override fun putString(
            key: String,
            value: String?,
        ) {
            if (key in stringKeys) {
                PrefManager.setFeatureValue(key.toPascalCase(), value!!.toInt())
            } else {
                throw IllegalArgumentException("Invalid key: $key")
            }
        }

        override fun putStringSet(
            key: String,
            values: MutableSet<String>?,
        ) {
            if (key in multiSelectKeys) {
                when (key) {
                    "hideSeen" -> {
                        PrefManager.setFeatureEnabled("HideSeenPrivateChat", values?.contains("private_chat") == true)
                        PrefManager.setFeatureEnabled("HideSeenChannel", values?.contains("channel") == true)
                    }
                    "markMessages" -> {
                        PrefManager.setFeatureEnabled("MarkMessagesDeleted", values?.contains("deleted") == true)
                        PrefManager.setFeatureEnabled("MarkMessagesEdited", values?.contains("edited") == true)
                    }
                }
            } else {
                throw IllegalArgumentException("Invalid key: $key")
            }
        }
    }

    class SettingsPreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(
            savedInstanceState: Bundle?,
            rootKey: String?,
        ) {
            preferenceManager.preferenceDataStore = SettingsPreferenceDataStore()
            setPreferencesFromResource(R.xml.features, rootKey)
        }
    }
}
