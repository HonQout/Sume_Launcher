package com.qch.sumelauncher.ui.settings.root;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.qch.sumelauncher.application.MyApplication;
import com.qch.sumelauncher.R;
import com.qch.sumelauncher.br.MyDeviceAdminReceiver;
import com.qch.sumelauncher.persistence.PreferenceDataStoreBridge;
import com.qch.sumelauncher.utils.ApplicationUtils;
import com.qch.sumelauncher.utils.DeviceAdminUtils;
import com.qch.sumelauncher.utils.DialogUtils;
import com.qch.sumelauncher.utils.IntentUtils;

import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String TAG = "SettingsFragment";
    private PreferenceDataStoreBridge preferenceDataStoreBridge;
    private SettingsViewModel settingsViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        preferenceDataStoreBridge = new PreferenceDataStoreBridge(MyApplication.getPreferenceDataStoreImpl());
        getPreferenceManager().setPreferenceDataStore(preferenceDataStoreBridge);
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        // Set versionName
        Preference versionPref = findPreference("version");
        if (versionPref != null) {
            String versionName = ApplicationUtils.getVersionName(requireContext(),
                    requireContext().getPackageName());
            versionPref.setSummary(versionName == null ? "" : versionName);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(@NonNull Preference preference) {
        String key = preference.getKey();
        if (Objects.equals(key, "default_app")) {
            launchManageDefaultAppsSettings();
        } else if (Objects.equals(key, "device_manager")) {
            ComponentName componentName = new ComponentName(requireContext(), MyDeviceAdminReceiver.class);
            boolean isAdminActive = DeviceAdminUtils.isAdminActive(requireContext(), componentName);

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.device_manager)
                    .setNegativeButton(R.string.cancel, null);
            if (isAdminActive) {
                builder
                        .setMessage(R.string.device_manager_active)
                        .setPositiveButton(R.string.deactivate, (dialog, which) -> {
                            boolean result = DeviceAdminUtils.removeActiveAdmin(requireContext(), componentName);
                            Toast.makeText(requireContext(), result ? R.string.success : R.string.fail, Toast.LENGTH_SHORT).show();
                        });
            } else {
                builder
                        .setMessage(R.string.device_manager_inactive)
                        .setPositiveButton(R.string.confirm, null);
            }
            DialogUtils.show(builder, settingsViewModel.getAnimationValue());
        } else if (Objects.equals(key, "visit_github_page")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.visit_github_page)
                    .setMessage(R.string.visit_github_page_message)
                    .setPositiveButton(R.string.visit, (dialog, which) -> {
                        int flags = Intent.FLAG_ACTIVITY_NEW_TASK;
                        if (!settingsViewModel.getAnimationValue()) {
                            flags |= Intent.FLAG_ACTIVITY_NO_ANIMATION;
                        }
                        IntentUtils.openWebsite(
                                requireActivity(),
                                ContextCompat.getString(requireContext(), R.string.github_address),
                                flags
                        );
                    })
                    .setNegativeButton(R.string.cancel, null);
            DialogUtils.show(builder, settingsViewModel.getAnimationValue());
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void launchManageDefaultAppsSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (!settingsViewModel.getAnimationValue()) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            }
            try {
                requireActivity().startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(requireContext(), R.string.cannot_find_activity, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), R.string.device_not_support_this_function, Toast.LENGTH_SHORT).show();
        }
    }
}