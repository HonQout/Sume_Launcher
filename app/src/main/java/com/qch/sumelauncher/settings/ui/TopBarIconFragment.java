package com.qch.sumelauncher.settings.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceFragmentCompat;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.application.MyApplication;
import com.qch.sumelauncher.persistence.PreferenceDataStoreBridge;

public class TopBarIconFragment extends PreferenceFragmentCompat {
    private static final String TAG = "TopBarIconFragment";
    private PreferenceDataStoreBridge preferenceDataStoreBridge;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        preferenceDataStoreBridge = new PreferenceDataStoreBridge(MyApplication.getPreferenceDataStore());
        getPreferenceManager().setPreferenceDataStore(preferenceDataStoreBridge);
        setPreferencesFromResource(R.xml.top_bar_icon_preferences, rootKey);
    }
}