package com.qch.sumelauncher.launcher.ui;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.application.MyApplication;
import com.qch.sumelauncher.databinding.ActivityLauncherBinding;
import com.qch.sumelauncher.settings.viewmodel.SettingsViewModel;
import com.qch.sumelauncher.topbar.view.TopBarView;
import com.qch.sumelauncher.topbar.viewmodel.RingerModeViewModel;
import com.qch.sumelauncher.utils.DialogUtils;
import com.qch.sumelauncher.utils.IntentUtils;
import com.qch.sumelauncher.utils.PermissionUtils;
import com.qch.sumelauncher.utils.UIUtils;
import com.qch.sumelauncher.topbar.viewmodel.AirplaneModeViewModel;
import com.qch.sumelauncher.launcher.viewmodel.LauncherViewModel;
import com.qch.sumelauncher.topbar.viewmodel.BatteryViewModel;
import com.qch.sumelauncher.topbar.viewmodel.BluetoothViewModel;
import com.qch.sumelauncher.topbar.viewmodel.TimeViewModel;
import com.qch.sumelauncher.topbar.viewmodel.WifiViewModel;

public class LauncherActivity extends AppCompatActivity {
    private static final String TAG = "LauncherActivity";
    private ActivityLauncherBinding binding;
    private LauncherViewModel launcherViewModel;
    private SettingsViewModel settingsViewModel;
    private TimeViewModel timeViewModel;
    private RingerModeViewModel ringerModeViewModel;
    private AirplaneModeViewModel airplaneModeViewModel;
    private WifiViewModel wifiViewModel;
    private BluetoothViewModel bluetoothViewModel;
    private BatteryViewModel batteryViewModel;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLauncherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UIUtils.setViewFitsSystemWindows(binding.getRoot());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.i(TAG, "Back event is intercepted.");
            }
        });
        // Initialize viewmodel
        ViewModelProvider viewModelProvider = new ViewModelProvider(this);
        launcherViewModel = viewModelProvider.get(LauncherViewModel.class);
        settingsViewModel = viewModelProvider.get(SettingsViewModel.class);
        timeViewModel = viewModelProvider.get(TimeViewModel.class);
        ringerModeViewModel = viewModelProvider.get(RingerModeViewModel.class);
        airplaneModeViewModel = viewModelProvider.get(AirplaneModeViewModel.class);
        wifiViewModel = viewModelProvider.get(WifiViewModel.class);
        bluetoothViewModel = viewModelProvider.get(BluetoothViewModel.class);
        batteryViewModel = viewModelProvider.get(BatteryViewModel.class);
        // Observe
        settingsViewModel.getDisplayStatusBar().observe(this, b ->
                UIUtils.handleStatusBarVisibility(getWindow(), b == null || b));
        settingsViewModel.getDisplayTopBar().observe(this, displayTopBar ->
                binding.aLauncherTopBar.setVisibility(displayTopBar ? View.VISIBLE : View.GONE));
        settingsViewModel.getDisplayRingerMode().observe(this, shouldDisplay -> {
            if (shouldDisplay) {
                ringerModeViewModel.restoreIconState();
                ringerModeViewModel.setIconVisible(true);
            } else {
                ringerModeViewModel.setIconVisible(false);
                ringerModeViewModel.setIconState(RingerModeViewModel.RingerModeIconState.HIDDEN);
            }
        });
        settingsViewModel.getDisplayAirplaneMode().observe(this, shouldDisplay -> {
            if (shouldDisplay) {
                airplaneModeViewModel.restoreIconState();
                airplaneModeViewModel.setIconVisible(true);
            } else {
                airplaneModeViewModel.setIconVisible(false);
                airplaneModeViewModel.setIconState(AirplaneModeViewModel.AirplaneModeIconState.HIDDEN);
            }
        });
        settingsViewModel.getDisplayWlan().observe(this, shouldDisplay -> {
            if (shouldDisplay) {
                wifiViewModel.restoreIconState();
                wifiViewModel.setIconVisible(true);
            } else {
                wifiViewModel.setIconVisible(false);
                wifiViewModel.setWifiIconState(WifiViewModel.WifiIconState.HIDDEN);
            }
        });
        settingsViewModel.getDisplayBluetooth().observe(this, shouldDisplay -> {
            if (shouldDisplay) {
                bluetoothViewModel.restoreIconState();
                bluetoothViewModel.setIconVisible(true);
            } else {
                bluetoothViewModel.setIconVisible(false);
                bluetoothViewModel.setIconState(BluetoothViewModel.BluetoothIconState.HIDDEN);
            }
        });
        settingsViewModel.getDisplayBatteryPct().observe(this, shouldDisplay -> {
            if (shouldDisplay) {
                binding.aLauncherTopBar.addChildView(this,
                        TopBarView.ViewTag.BATTERY_PCT,
                        new TopBarView.BatteryPctExtra(batteryViewModel.getLevelValue()),
                        TopBarView.ConflictStrategy.REPLACE_EXISTING);
            } else {
                binding.aLauncherTopBar.removeChildView(TopBarView.ViewTag.BATTERY_PCT);
            }
        });
        timeViewModel.getCurrentTimeText().observe(this, currentTimeText -> {
            binding.aLauncherTopBar.setTimeText(currentTimeText);
        });
        timeViewModel.getCurrentDateText().observe(this, currentDateText -> {
            binding.aLauncherTopBar.setDateText(currentDateText);
        });
        ringerModeViewModel.getIconState().observe(this, state -> {
            if (state == null) {
                Log.e(TAG, "Failed to get state of ringer mode icon.");
                return;
            }
            switch (state) {
                case SILENT: {
                    binding.aLauncherTopBar.modifyOrAddChildView(this,
                            TopBarView.ViewTag.RINGER_MODE, new TopBarView.IconExtra(R.drawable.baseline_bell_off_24));
                    break;
                }
                case VIBRATE: {
                    binding.aLauncherTopBar.modifyOrAddChildView(this,
                            TopBarView.ViewTag.RINGER_MODE, new TopBarView.IconExtra(R.drawable.baseline_vibration_24));
                    break;
                }
                case HIDDEN: {
                    binding.aLauncherTopBar.removeChildView(TopBarView.ViewTag.RINGER_MODE);
                    break;
                }
            }
        });
        airplaneModeViewModel.getIconState().observe(this, state -> {
            if (state == null) {
                Log.e(TAG, "Failed to get state of airplane mode icon.");
                return;
            }
            switch (state) {
                case ON: {
                    binding.aLauncherTopBar.addChildView(
                            LauncherActivity.this,
                            TopBarView.ViewTag.AIRPLANE_MODE,
                            new TopBarView.IconExtra(R.drawable.baseline_airplanemode_24),
                            TopBarView.ConflictStrategy.REPLACE_EXISTING
                    );
                    break;
                }
                case HIDDEN: {
                    binding.aLauncherTopBar.removeChildView(TopBarView.ViewTag.AIRPLANE_MODE);
                    break;
                }
            }
        });
        wifiViewModel.getIconState().observe(this, state -> {
            if (state == null) {
                Log.e(TAG, "Failed to get state of Wi-Fi icon.");
                return;
            }
            switch (state) {
                case NOT_CONNECTED: {
                    binding.aLauncherTopBar.modifyOrAddChildView(this, TopBarView.ViewTag.WIFI,
                            new TopBarView.IconExtra(R.drawable.baseline_wifi_null_24));
                    break;
                }
                case CONNECTED_0: {
                    binding.aLauncherTopBar.modifyOrAddChildView(this, TopBarView.ViewTag.WIFI,
                            new TopBarView.IconExtra(R.drawable.baseline_wifi_1_bar_24));
                    break;
                }
                case CONNECTED_1: {
                    binding.aLauncherTopBar.modifyOrAddChildView(this, TopBarView.ViewTag.WIFI,
                            new TopBarView.IconExtra(R.drawable.baseline_wifi_2_bar_24));
                    break;
                }
                case CONNECTED_2: {
                    binding.aLauncherTopBar.modifyOrAddChildView(this, TopBarView.ViewTag.WIFI,
                            new TopBarView.IconExtra(R.drawable.baseline_wifi_3_bar_24));
                    break;
                }
                case HIDDEN: {
                    binding.aLauncherTopBar.removeChildView(TopBarView.ViewTag.WIFI);
                    break;
                }
            }
        });
        bluetoothViewModel.getIconState().observe(this, state -> {
            if (state == null) {
                Log.e(TAG, "Failed to get state of bluetooth state icon.");
                return;
            }
            switch (state) {
                case ENABLED: {
                    binding.aLauncherTopBar.addChildView(
                            LauncherActivity.this,
                            TopBarView.ViewTag.BLUETOOTH,
                            new TopBarView.IconExtra(R.drawable.baseline_bluetooth_24),
                            TopBarView.ConflictStrategy.REPLACE_EXISTING
                    );
                    break;
                }
                case HIDDEN: {
                    binding.aLauncherTopBar.removeChildView(TopBarView.ViewTag.BLUETOOTH);
                }
            }
        });
        batteryViewModel.getLevel().observe(this, integer -> {
            int level = integer == null ? 0 : integer;
            binding.aLauncherTopBar.setBatteryLevel(this, level);
        });
        batteryViewModel.getIsCharging().observe(this, aBoolean -> {
            boolean isCharging = aBoolean != null && aBoolean;
            binding.aLauncherTopBar.setBatteryCharging(isCharging);
        });

        launcherViewModel.getLauncherState().observe(this, launcherState -> {
            Log.i(TAG, "Prepare to examine launcher state.");
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                binding.aLauncherFcv.post(() -> {
                    NavController navController =
                            Navigation.findNavController(LauncherActivity.this, R.id.a_launcher_fcv);
                    if (launcherState == LauncherViewModel.LauncherState.NORMAL) {
                        Log.i(TAG, "Launcher state is NORMAL. ");
                    } else if (launcherState == LauncherViewModel.LauncherState.EDIT) {
                        Log.i(TAG, "Launcher state is EDIT. ");
                        // TODO: Realize edit mode
                    } else if (launcherState == LauncherViewModel.LauncherState.APPS) {
                        Log.i(TAG, "Launcher state is APPS. ");
                        navController.navigate(R.id.action_Launcher_to_Drawer);
                    }
                });
            }
        });
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        wifiViewModel.update();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if permissions are granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ||
                PermissionUtils.isPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.i(TAG, "No need to show the dialog to ask for permission.");
        } else if (settingsViewModel.getAskForPermFineLocationValue()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                showPermFineLocationDialog();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
    }

    private void showPermFineLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.request_permission)
                .setMessage(R.string.perm_fine_location_reason)
                .setPositiveButton(R.string.app_info, (dialog, which) ->
                        IntentUtils.openAppDetailsPage(this, this.getPackageName()))
                .setNeutralButton(R.string.deny, (dialog, which) ->
                        MyApplication.getPreferenceDataStore().setBoolean("ask_for_perm_fine_location", false))
                .setNegativeButton(R.string.cancel, null);
        DialogUtils.show(builder, settingsViewModel.getAnimationValue());
    }
}