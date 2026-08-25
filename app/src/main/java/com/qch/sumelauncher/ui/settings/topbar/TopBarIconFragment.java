package com.qch.sumelauncher.ui.settings.topbar;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.application.MyApplication;
import com.qch.sumelauncher.persistence.PreferenceDataStoreBridge;
import com.qch.sumelauncher.utils.BluetoothUtils;
import com.qch.sumelauncher.utils.WifiUtils;

public class TopBarIconFragment extends PreferenceFragmentCompat {
    private static final String TAG = "TopBarIconFragment";
    private PreferenceDataStoreBridge preferenceDataStoreBridge;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        preferenceDataStoreBridge = new PreferenceDataStoreBridge(MyApplication.getPreferenceDataStoreImpl());
        getPreferenceManager().setPreferenceDataStore(preferenceDataStoreBridge);
        setPreferencesFromResource(R.xml.top_bar_icon_preferences, rootKey);

        Preference wifiPref = findPreference("wifi");
        if (wifiPref != null) {
            boolean isWifiSupported = WifiUtils.isWifiSupported(requireContext());
            if (!isWifiSupported) {
                wifiPref.setEnabled(false);
                wifiPref.setDefaultValue(false);
                wifiPref.setSummary(R.string.device_not_support_this_function);
            }
        }

        Preference btPref = findPreference("bluetooth");
        if (btPref != null) {
            boolean isBtSupported = BluetoothUtils.isBluetoothSupported(requireContext());
            if (!isBtSupported) {
                btPref.setEnabled(false);
                btPref.setDefaultValue(false);
                btPref.setSummary(R.string.device_not_support_this_function);
            }
        }
    }
}