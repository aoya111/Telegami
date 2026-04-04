package com.aoya.telegami.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import com.aoya.telegami.R
import com.aoya.telegami.databinding.FragmentSettingsBinding
import com.aoya.telegami.service.PrefManager
import com.aoya.telegami.telegamiApp
import com.aoya.telegami.ui.util.navController
import com.aoya.telegami.ui.util.setEdge2EdgeFlags
import com.aoya.telegami.ui.util.setupToolbar
import com.aoya.telegami.utils.PackageHelper.findEnabledAppComponent
import dev.androidbroadcast.vbpd.viewBinding
import kotlinx.coroutines.runBlocking

class SettingsFragment :
    Fragment(R.layout.fragment_settings),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private val binding by viewBinding(FragmentSettingsBinding::bind)

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        setupToolbar(
            toolbar = binding.toolbar,
            title = getString(R.string.title_settings),
            navigationIcon = R.drawable.baseline_arrow_back_24,
            navigationOnClick = { navController.navigateUp() },
        )

        setEdge2EdgeFlags(binding.root)

        runBlocking {
            PrefManager.isLauncherIconInvisible.emit(findEnabledAppComponent(telegamiApp) == null)
        }

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
        override fun getBoolean(
            key: String,
            defValue: Boolean,
        ): Boolean =
            when (key) {
                "hideIcon" -> PrefManager.hideIcon
                else -> throw IllegalArgumentException("Invalid key: $key")
            }

        override fun putBoolean(
            key: String,
            value: Boolean,
        ) {
            when (key) {
                "hideIcon" -> PrefManager.hideIcon = value
                else -> throw IllegalArgumentException("Invalid key: $key")
            }
        }
    }

    class SettingsPreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(
            savedInstanceState: Bundle?,
            rootKey: String?,
        ) {
            preferenceManager.preferenceDataStore = SettingsPreferenceDataStore()
            setPreferencesFromResource(R.xml.settings, rootKey)
        }
    }
}
