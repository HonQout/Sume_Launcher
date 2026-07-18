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
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.application.MyApplication;
import com.qch.sumelauncher.databinding.ActivityLauncherBinding;
import com.qch.sumelauncher.topbar.ui.TopBarManager;
import com.qch.sumelauncher.topbar.viewmodel.RingerModeViewModel;
import com.qch.sumelauncher.utils.DialogUtils;
import com.qch.sumelauncher.utils.IntentUtils;
import com.qch.sumelauncher.utils.PermissionUtils;
import com.qch.sumelauncher.utils.UIUtils;
import com.qch.sumelauncher.topbar.view.BatteryView;
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
                binding.aLauncherTopBar.getRoot().setVisibility(displayTopBar ? View.VISIBLE : View.GONE));
        timeViewModel.getCurrentTimeText().observe(this, currentTimeText -> {
            AppCompatTextView textView =
                    binding.aLauncherTopBar.topBarLeftPart.findViewById(R.id.top_bar_tv_time);
            textView.setText(currentTimeText);
        });
        timeViewModel.getCurrentDateText().observe(this, currentDateText -> {
            AppCompatTextView textView =
                    binding.aLauncherTopBar.topBarLeftPart.findViewById(R.id.top_bar_tv_date);
            textView.setText(currentDateText);
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
                TopBarManager.replaceIcon(LauncherActivity.this,
                        binding.aLauncherTopBar.topBarRightPart,
                        TopBarManager.TopBarIcons.RINGER_MODE,
                        iconRes,
                        LauncherActivity.TAG);
            } else {
                TopBarManager.removeIcon(binding.aLauncherTopBar.topBarRightPart,
                        TopBarManager.TopBarIcons.RINGER_MODE, LauncherActivity.TAG);
            }
        });
        airplaneModeViewModel.getAirplaneModeEnabled().observe(this, isEnabled -> {
            if (isEnabled == null) {
                Log.e(TAG, "Failed to get airplane mode.");
                return;
            }
            if (isEnabled) {
                TopBarManager.addIcon(LauncherActivity.this,
                        binding.aLauncherTopBar.topBarRightPart,
                        TopBarManager.TopBarIcons.AIRPLANE_MODE,
                        R.drawable.baseline_airplanemode_active_24,
                        LauncherActivity.TAG);
            } else {
                TopBarManager.removeIcon(binding.aLauncherTopBar.topBarRightPart,
                        TopBarManager.TopBarIcons.AIRPLANE_MODE, LauncherActivity.TAG);
            }
        });
        wifiViewModel.getWifiEnabled().observe(this, isEnabled -> {
            if (isEnabled) {
                TopBarManager.addIcon(LauncherActivity.this,
                        binding.aLauncherTopBar.topBarRightPart,
                        TopBarManager.TopBarIcons.WIFI,
                        wifiViewModel.getWifiIconResValue(),
                        LauncherActivity.TAG);
            } else {
                TopBarManager.removeIcon(binding.aLauncherTopBar.topBarRightPart,
                        TopBarManager.TopBarIcons.WIFI, LauncherActivity.TAG);
            }
        });
        wifiViewModel.getWifiIconRes().observe(this, iconRes -> {
            if (iconRes == null) {
                Log.e(TAG, "Failed to get icon resource of Wi-Fi.");
                return;
            }
            TopBarManager.modifyIcon(binding.aLauncherTopBar.topBarRightPart,
                    TopBarManager.TopBarIcons.WIFI, iconRes, LauncherActivity.TAG);
        });
        bluetoothViewModel.getBtEnabled().observe(this, isEnabled -> {
            if (isEnabled) {
                TopBarManager.addIcon(LauncherActivity.this,
                        binding.aLauncherTopBar.topBarRightPart,
                        TopBarManager.TopBarIcons.BLUETOOTH,
                        bluetoothViewModel.getBtIconResValue(),
                        LauncherActivity.TAG);
            } else {
                TopBarManager.removeIcon(binding.aLauncherTopBar.topBarRightPart,
                        TopBarManager.TopBarIcons.BLUETOOTH, LauncherActivity.TAG);
            }
        });
        bluetoothViewModel.getBtIconRes().observe(this, iconRes -> {
            if (iconRes == null) {
                Log.e(TAG, "Failed to get icon resource of bluetooth.");
                return;
            }
            TopBarManager.modifyIcon(binding.aLauncherTopBar.topBarRightPart,
                    TopBarManager.TopBarIcons.BLUETOOTH, iconRes, LauncherActivity.TAG);
        });
        batteryViewModel.getLevel().observe(this, integer -> {
            int i = integer == null ? 0 : integer;
            LinearLayoutCompat linearLayoutCompat = binding.aLauncherTopBar.topBarRightPart;
            AppCompatTextView textView = linearLayoutCompat.findViewById(R.id.top_bar_tv_battery);
            textView.setText(
                    String.format(
                            ContextCompat.getString(this, R.string.battery_percentage),
                            i
                    ));
            BatteryView batteryView = binding.aLauncherTopBar.topBarBv;
            batteryView.setLevel(i);
        });
        batteryViewModel.getIsCharging().observe(this, aBoolean -> {
            boolean b = aBoolean == null ? false : aBoolean;
            BatteryView batteryView = binding.aLauncherTopBar.topBarBv;
            batteryView.setCharging(b);
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