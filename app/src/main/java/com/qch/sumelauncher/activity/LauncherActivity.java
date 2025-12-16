package com.qch.sumelauncher.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.qch.sumelauncher.R;
import com.qch.sumelauncher.adapter.viewpager2.AppPagerAdapter;
import com.qch.sumelauncher.databinding.ActivityMainBinding;
import com.qch.sumelauncher.utils.IntentUtils;
import com.qch.sumelauncher.utils.PermissionUtils;
import com.qch.sumelauncher.utils.UIUtils;
import com.qch.sumelauncher.viewmodel.AppViewModel;
import com.qch.sumelauncher.viewmodel.BatteryViewModel;
import com.qch.sumelauncher.viewmodel.BluetoothViewModel;
import com.qch.sumelauncher.viewmodel.WifiViewModel;
import com.qch.sumelauncher.viewmodel.TimeViewModel;

public class LauncherActivity extends AppCompatActivity {
    private static final String TAG = "LauncherActivity";
    private ActivityMainBinding binding;
    private TimeViewModel timeViewModel;
    private WifiViewModel wifiViewModel;
    private BluetoothViewModel bluetoothViewModel;
    private BatteryViewModel batteryViewModel;
    private AppViewModel appViewModel;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set view
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UIUtils.setViewFitsSystemWindows(binding.getRoot());
        // Handle back event
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.i(TAG, "Back event is intercepted.");
            }
        });
        // Initialize view
        ViewPager2 viewPager2 = binding.aMainVp2;
        AppPagerAdapter appPagerAdapter = new AppPagerAdapter(this, 0);
        viewPager2.setAdapter(appPagerAdapter);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                appViewModel.setCurrentPage(position + 1);
            }
        });
        binding.aMainBtnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(LauncherActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        binding.aMainBtnPrevPage.setOnClickListener(v -> launcherPageUp());
        binding.aMainBtnNextPage.setOnClickListener(v -> launcherPageDown());
        // Initialize viewmodel
        ViewModelProvider viewModelProvider = new ViewModelProvider(this);
        appViewModel = viewModelProvider.get(AppViewModel.class);
        timeViewModel = viewModelProvider.get(TimeViewModel.class);
        wifiViewModel = viewModelProvider.get(WifiViewModel.class);
        bluetoothViewModel = viewModelProvider.get(BluetoothViewModel.class);
        batteryViewModel = viewModelProvider.get(BatteryViewModel.class);
        // Observe
        appViewModel.getDisplayTopBar().observe(this, displayTopBar ->
                binding.aMainLl1.setVisibility(displayTopBar ? View.VISIBLE : View.GONE));
        appViewModel.getScrollToSwitchPage().observe(this, scrollToSwitchPage ->
                binding.aMainVp2.setUserInputEnabled(scrollToSwitchPage));
        timeViewModel.getCurrentTime().observe(this, currentTime ->
                binding.aMainTv1.setText(DateUtils.formatDateTime(
                                LauncherActivity.this,
                                currentTime,
                                DateUtils.FORMAT_SHOW_TIME
                        )
                ));
        timeViewModel.getCurrentTime().observe(this, currentTime ->
                binding.aMainTv2.setText(DateUtils.formatDateTime(
                                LauncherActivity.this,
                                currentTime,
                                DateUtils.FORMAT_NO_YEAR
                                        | DateUtils.FORMAT_SHOW_DATE
                                        | DateUtils.FORMAT_SHOW_WEEKDAY
                                        | DateUtils.FORMAT_ABBREV_WEEKDAY
                        )
                ));
        wifiViewModel.getWifiEnabled().observe(this, isEnabled ->
                binding.aMainIv1.setVisibility(isEnabled ? View.VISIBLE : View.GONE));
        wifiViewModel.getWifiIcon().observe(this, integer -> {
            int i = integer == null ? R.drawable.baseline_signal_wifi_statusbar_null_24 : integer;
            binding.aMainIv1.setImageResource(i);
        });
        bluetoothViewModel.getBtEnabled().observe(this, isEnabled ->
                binding.aMainIv2.setVisibility(isEnabled ? View.VISIBLE : View.GONE));
        bluetoothViewModel.getBtIcon().observe(this, icon ->
                binding.aMainIv2.setImageResource(icon));
        batteryViewModel.getLevel().observe(this, integer -> {
            int i = integer == null ? -1 : integer;
            binding.aMainTv3.setText(
                    String.format(
                            ContextCompat.getString(this, R.string.battery_percentage),
                            i
                    ));
        });
        batteryViewModel.getIcon().observe(this, integer -> {
            int i = integer == null ? R.drawable.baseline_battery_unknown_24 : integer;
            binding.aMainIv3.setImageResource(i);
        });
        appViewModel.getNumPage().observe(this, integer -> {
            appPagerAdapter.setNumPages(integer);
            binding.aMainTv4.setText(
                    String.format(
                            ContextCompat.getString(this, R.string.page_text),
                            appViewModel.getCurrentPageInt(), integer
                    ));
        });
        appViewModel.getCurrentPage().observe(this, integer ->
                binding.aMainTv4.setText(
                        String.format(
                                ContextCompat.getString(
                                        LauncherActivity.this,
                                        R.string.page_text
                                ),
                                integer, appViewModel.getNumPageInt()
                        )));
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                wifiViewModel.init();
            }
        });
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        // get shared preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        appViewModel.getStoredPreferences(sharedPreferences, false);
        boolean displayStatusBar =
                sharedPreferences.getBoolean("display_status_bar", true);
        UIUtils.handleStatusBarVisibility(getWindow(), displayStatusBar);
        sharedPreferences.registerOnSharedPreferenceChangeListener(appViewModel.spListener);
        // check if permissions are granted
        if (PermissionUtils.isPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            wifiViewModel.init();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.request_permission)
                    .setMessage(R.string.perm_fine_location_reason)
                    .setPositiveButton(R.string.app_info, (dialog, which) ->
                            IntentUtils.openAppDetailsPage(LauncherActivity.this,
                                    "com.qch.sumelauncher"))
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(appViewModel.spListener);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP: {
                if (appViewModel.getVolumeKeySwitchPageBoolean()) {
                    launcherPageUp();
                    return true;
                }
                break;
            }
            case KeyEvent.KEYCODE_VOLUME_DOWN: {
                if (appViewModel.getVolumeKeySwitchPageBoolean()) {
                    launcherPageDown();
                    return true;
                }
                break;
            }
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void launcherPageUp() {
        ViewPager2 viewPager2 = binding.aMainVp2;
        if (viewPager2.getAdapter() != null) {
            int currentItem = viewPager2.getCurrentItem();
            if (currentItem > 0) {
                currentItem -= 1;
            }
            viewPager2.setCurrentItem(currentItem, false);
            appViewModel.setCurrentPage(currentItem + 1);
        }
    }

    public void launcherPageDown() {
        ViewPager2 viewPager2 = binding.aMainVp2;
        if (viewPager2.getAdapter() != null) {
            int currentItem = viewPager2.getCurrentItem();
            if (currentItem < viewPager2.getAdapter().getItemCount() - 1) {
                currentItem += 1;
            }
            viewPager2.setCurrentItem(currentItem, false);
            appViewModel.setCurrentPage(currentItem + 1);
        }
    }
}