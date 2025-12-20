package com.qch.sumelauncher.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.qch.sumelauncher.MyApplication;
import com.qch.sumelauncher.R;
import com.qch.sumelauncher.persistence.PreferenceDataStoreBridge;
import com.qch.sumelauncher.viewmodel.SettingsViewModel;

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
    }

    @Override
    public boolean onPreferenceTreeClick(@NonNull Preference preference) {
        String key = preference.getKey();
        if (Objects.equals(key, "set_default_app")) {
            viewModel.startManageDefaultAppsSettings(requireActivity());
        } else if (Objects.equals(key, "requested_permissions")) {
            viewModel.startPermissionActivity(requireActivity());
        } else if (Objects.equals(key, "view_github_page")) {
            viewModel.openGithubPage(requireActivity());
        }
        return super.onPreferenceTreeClick(preference);
    }
}