package com.qch.sumelauncher.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.databinding.ActivitySettingsBinding;
import com.qch.sumelauncher.utils.IntentUtils;
import com.qch.sumelauncher.utils.UIUtils;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";
    private ActivitySettingsBinding binding;
    private final SharedPreferences.OnSharedPreferenceChangeListener listener =
            (sharedPreferences, key) -> {
                if (Objects.equals(key, "display_status_bar")) {
                    boolean displayStatusBar = sharedPreferences.getBoolean(key, true);
                    UIUtils.handleStatusBarVisibility(getWindow(), displayStatusBar);
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Immersive system bars
        EdgeToEdge.enable(this);
        // Set view
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.aSettingsMt.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed());
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.a_settings_fl, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        boolean displayStatusBar =
                sharedPreferences.getBoolean("display_status_bar", true);
        UIUtils.handleStatusBarVisibility(getWindow(), displayStatusBar);
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(listener);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }

        @Override
        public boolean onPreferenceTreeClick(@NonNull Preference preference) {
            String key = preference.getKey();
            if (Objects.equals(key, "set_default_app")) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else if (Objects.equals(key, "view_github_page")) {
                IntentUtils.handleLaunchIntentResult(
                        requireContext(),
                        IntentUtils.openNetAddress(requireContext(), getString(R.string.github_address))
                );
            }
            return super.onPreferenceTreeClick(preference);
        }
    }
}