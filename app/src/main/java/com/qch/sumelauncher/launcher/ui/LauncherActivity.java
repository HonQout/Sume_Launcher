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
import com.qch.sumelauncher.settings.viewmodel.SettingsViewModel;
import com.qch.sumelauncher.topbar.viewmodel.WifiViewModel;
import com.qch.sumelauncher.topbar.viewmodel.TimeViewModel;

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
        launcherViewModel.getDisplayStatusBar().observe(this, b ->
                UIUtils.handleStatusBarVisibility(getWindow(), b == null || b));
        launcherViewModel.getDisplayTopBar().observe(this, displayTopBar ->
                binding.aLauncherTopBar.setVisibility(displayTopBar ? View.VISIBLE : View.GONE));
        timeViewModel.getCurrentTimeText().observe(this, currentTimeText -> {
            binding.aLauncherTopBar.setTimeText(currentTimeText);
        });
        timeViewModel.getCurrentDateText().observe(this, currentDateText -> {
            binding.aLauncherTopBar.setDateText(currentDateText);
        });
        ringerModeViewModel.getRingerMode().observe(this, ringerMode -> {
            if (ringerMode == null) {
                Log.e(TAG, "Failed to get ringer mode.");
                return;
            }
            boolean shouldDisplay = false;
            int iconRes = 0;
            switch (ringerMode) {
                case Silent: {
                    shouldDisplay = true;
                    iconRes = R.drawable.baseline_bell_off_24;
                    break;
                }
                case Vibrate: {
                    shouldDisplay = true;
                    iconRes = R.drawable.baseline_vibration_24;
                    break;
                }
                default: {
                    break;
                }
            }
            if (shouldDisplay) {
                binding.aLauncherTopBar.addChildView(
                        LauncherActivity.this,
                        TopBarView.ViewTag.RINGER_MODE,
                        new TopBarView.IconExtra(iconRes),
                        TopBarView.ConflictStrategy.REPLACE_EXISTING
                );
            } else {
                binding.aLauncherTopBar.removeChildView(TopBarView.ViewTag.RINGER_MODE);
            }
        });
        airplaneModeViewModel.getAirplaneModeEnabled().observe(this, isEnabled -> {
            if (isEnabled == null) {
                Log.e(TAG, "Failed to get airplane mode.");
                return;
            }
            if (isEnabled) {
                binding.aLauncherTopBar.addChildView(
                        LauncherActivity.this,
                        TopBarView.ViewTag.AIRPLANE_MODE,
                        new TopBarView.IconExtra(R.drawable.baseline_airplanemode_active_24),
                        TopBarView.ConflictStrategy.REPLACE_EXISTING
                );
            } else {
                binding.aLauncherTopBar.removeChildView(TopBarView.ViewTag.AIRPLANE_MODE);
            }
        });
        wifiViewModel.getWifiEnabled().observe(this, isEnabled -> {
            if (isEnabled) {
                binding.aLauncherTopBar.addChildView(
                        LauncherActivity.this,
                        TopBarView.ViewTag.WIFI,
                        new TopBarView.IconExtra(wifiViewModel.getWifiIconResValue()),
                        TopBarView.ConflictStrategy.REPLACE_EXISTING
                );
            } else {
                binding.aLauncherTopBar.removeChildView(TopBarView.ViewTag.WIFI);
            }
        });
        wifiViewModel.getWifiIconRes().observe(this, iconRes -> {
            if (iconRes == null) {
                Log.e(TAG, "Failed to get icon resource of Wi-Fi.");
                return;
            }
            binding.aLauncherTopBar.modifyChildView(TopBarView.ViewTag.WIFI,
                    new TopBarView.IconExtra(iconRes));
        });
        bluetoothViewModel.getBtEnabled().observe(this, isEnabled -> {
            if (isEnabled) {
                binding.aLauncherTopBar.addChildView(
                        LauncherActivity.this,
                        TopBarView.ViewTag.BLUETOOTH,
                        new TopBarView.IconExtra(bluetoothViewModel.getBtIconResValue()),
                        TopBarView.ConflictStrategy.REPLACE_EXISTING
                );
            } else {
                binding.aLauncherTopBar.removeChildView(TopBarView.ViewTag.BLUETOOTH);
            }
        });
        bluetoothViewModel.getBtIconRes().observe(this, iconRes -> {
            if (iconRes == null) {
                Log.e(TAG, "Failed to get icon resource of bluetooth.");
                return;
            }
            binding.aLauncherTopBar.modifyChildView(TopBarView.ViewTag.BLUETOOTH,
                    new TopBarView.IconExtra(iconRes));
        });
        batteryViewModel.getLevel().observe(this, integer -> {
            int level = integer == null ? 0 : integer;
            binding.aLauncherTopBar.setBatteryLevel(this, level);
        });
        batteryViewModel.getIsCharging().observe(this, aBoolean -> {
            boolean isCharging = aBoolean == null ? false : aBoolean;
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
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
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
        } else if (launcherViewModel.getAskForPermFineLocationValue()) {
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
        DialogUtils.show(builder, launcherViewModel.getAnimationValue());
    }
}