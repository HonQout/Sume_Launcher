package com.qch.sumelauncher.settings.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.qch.sumelauncher.application.MyApplication;
import com.qch.sumelauncher.R;
import com.qch.sumelauncher.persistence.PreferenceDataStoreBridge;
import com.qch.sumelauncher.settings.viewmodel.SettingsViewModel;
import com.qch.sumelauncher.utils.ConfigUtils;
import com.qch.sumelauncher.utils.IntentUtils;

import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String TAG = "SettingsFragment";
    private PreferenceDataStoreBridge preferenceDataStoreBridge;
    private SettingsViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        preferenceDataStoreBridge = new PreferenceDataStoreBridge(MyApplication.getPreferenceDataStore());
        getPreferenceManager().setPreferenceDataStore(preferenceDataStoreBridge);
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        // Set versionName
        Preference versionPref = findPreference("version");
        if (versionPref != null) {
            String versionName = ConfigUtils.getVersionName(requireContext());
            versionPref.setSummary(versionName);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(@NonNull Preference preference) {
        String key = preference.getKey();
        if (Objects.equals(key, "default_app")) {
            startManageDefaultAppsActivity();
        } else if (Objects.equals(key, "view_github_page")) {
            openGithubPage();
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void startManageDefaultAppsActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (!viewModel.getAnimationValue()) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            }
            requireActivity().startActivity(intent);
        } else {
            Toast.makeText(requireContext(), R.string.cannot_find_activity, Toast.LENGTH_SHORT).show();
        }
    }

    private void openGithubPage() {
        int flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        if (viewModel.getAnimationValue()) {
            flags |= Intent.FLAG_ACTIVITY_NO_ANIMATION;
        }
        IntentUtils.handleLaunchIntentResult(
                requireActivity(),
                IntentUtils.openNetAddress(
                        requireActivity(),
                        ContextCompat.getString(requireContext(), R.string.github_address),
                        flags)
        );
    }
}