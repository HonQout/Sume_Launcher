package com.qch.sumelauncher.activity;

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
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.color.MaterialColors;
import com.qch.sumelauncher.R;
import com.qch.sumelauncher.application.MyApplication;
import com.qch.sumelauncher.databinding.ActivityLauncherBinding;
import com.qch.sumelauncher.utils.DialogUtils;
import com.qch.sumelauncher.utils.IntentUtils;
import com.qch.sumelauncher.utils.PermissionUtils;
import com.qch.sumelauncher.utils.UIUtils;
import com.qch.sumelauncher.viewmodel.AirplaneModeViewModel;
import com.qch.sumelauncher.viewmodel.LauncherViewModel;
import com.qch.sumelauncher.viewmodel.BatteryViewModel;
import com.qch.sumelauncher.viewmodel.BluetoothViewModel;
import com.qch.sumelauncher.viewmodel.SettingsViewModel;
import com.qch.sumelauncher.viewmodel.WifiViewModel;
import com.qch.sumelauncher.viewmodel.TimeViewModel;

public class LauncherActivity extends AppCompatActivity {
    private static final String TAG = "LauncherActivity";
    private ActivityLauncherBinding binding;
    private LauncherViewModel launcherViewModel;
    private SettingsViewModel settingsViewModel;
    private TimeViewModel timeViewModel;
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
        airplaneModeViewModel.getAirplaneModeEnabled().observe(this, isEnabled -> {
            if (isEnabled) {
                AppCompatImageView imageView = new AppCompatImageView(LauncherActivity.this);
                imageView.setTag("top_bar_airplane_mode");
                imageView.setImageResource(R.drawable.baseline_airplanemode_active_24);
                imageView.setColorFilter(
                        MaterialColors.getColor(imageView, com.google.android.material.R.attr.colorOnSurface)
                );
                imageView.setPadding(0, 0, 0, 0);
                LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(
                        getResources().getDimensionPixelSize(R.dimen.top_bar_icon_size),
                        getResources().getDimensionPixelSize(R.dimen.top_bar_icon_size)
                );
                layoutParams.setMargins(2, 0, 2, 0);
                imageView.setLayoutParams(layoutParams);
                LinearLayoutCompat linearLayoutCompat = binding.aLauncherTopBar.topBarRightPart;
                linearLayoutCompat.addView(imageView, 0);
                Log.i(TAG, "Added airplane mode icon.");
            } else {
                LinearLayoutCompat linearLayoutCompat = binding.aLauncherTopBar.topBarRightPart;
                linearLayoutCompat.removeView(linearLayoutCompat.findViewWithTag("top_bar_airplane_mode"));
                Log.i(TAG, "Removed airplane mode icon.");
            }
        });
        wifiViewModel.getWifiEnabled().observe(this, isEnabled -> {
            LinearLayoutCompat linearLayoutCompat = binding.aLauncherTopBar.topBarRightPart;
            if (isEnabled) {
                if (linearLayoutCompat.findViewWithTag("top_bar_wifi") != null) {
                    Log.i(TAG, "Wifi icon already exists.");
                    return;
                }
                AppCompatImageView imageView = new AppCompatImageView(LauncherActivity.this);
                imageView.setTag("top_bar_wifi");
                imageView.setImageResource(wifiViewModel.getWifiIconResValue());
                imageView.setColorFilter(
                        MaterialColors.getColor(imageView, com.google.android.material.R.attr.colorOnSurface)
                );
                imageView.setPadding(0, 0, 0, 0);
                LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(
                        getResources().getDimensionPixelSize(R.dimen.top_bar_icon_size),
                        getResources().getDimensionPixelSize(R.dimen.top_bar_icon_size)
                );
                layoutParams.setMargins(2, 0, 2, 0);
                imageView.setLayoutParams(layoutParams);
                linearLayoutCompat.addView(imageView, 0);
                Log.i(TAG, "Added wifi icon.");
            } else {
                View view = linearLayoutCompat.findViewWithTag("top_bar_wifi");
                if (view == null) {
                    Log.i(TAG, "Wifi icon doesn't exist.");
                    return;
                }
                linearLayoutCompat.removeView(view);
                Log.i(TAG, "Removed wifi icon.");
            }
        });
        wifiViewModel.getWifiIconRes().observe(this, integer -> {
            LinearLayoutCompat linearLayoutCompat = binding.aLauncherTopBar.topBarRightPart;
            View view = linearLayoutCompat.findViewWithTag("top_bar_wifi");
            if (!(view instanceof AppCompatImageView)) {
                Log.e(TAG, "Cannot find wifi icon.");
                return;
            }
            AppCompatImageView imageView = (AppCompatImageView) view;
            imageView.setImageResource(wifiViewModel.getWifiIconResValue());
        });
        bluetoothViewModel.getBtEnabled().observe(this, isEnabled -> {
            LinearLayoutCompat linearLayoutCompat = binding.aLauncherTopBar.topBarRightPart;
            if (isEnabled) {
                if (linearLayoutCompat.findViewWithTag("top_bar_bluetooth") != null) {
                    Log.i(TAG, "Bluetooth icon already exists.");
                    return;
                }
                AppCompatImageView imageView = new AppCompatImageView(LauncherActivity.this);
                imageView.setTag("top_bar_bluetooth");
                imageView.setImageResource(bluetoothViewModel.getBtIconResValue());
                imageView.setColorFilter(
                        MaterialColors.getColor(imageView, com.google.android.material.R.attr.colorOnSurface)
                );
                imageView.setPadding(0, 0, 0, 0);
                LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(
                        getResources().getDimensionPixelSize(R.dimen.top_bar_icon_size),
                        getResources().getDimensionPixelSize(R.dimen.top_bar_icon_size)
                );
                layoutParams.setMargins(2, 0, 2, 0);
                imageView.setLayoutParams(layoutParams);
                linearLayoutCompat.addView(imageView, 0);
                Log.i(TAG, "Added bluetooth icon.");
            } else {
                View view = linearLayoutCompat.findViewWithTag("top_bar_bluetooth");
                if (view == null) {
                    Log.i(TAG, "Bluetooth icon doesn't exist.");
                    return;
                }
                linearLayoutCompat.removeView(view);
                Log.i(TAG, "Removed bluetooth icon.");
            }
        });
        bluetoothViewModel.getBtIconRes().observe(this, icon -> {
            LinearLayoutCompat linearLayoutCompat = binding.aLauncherTopBar.topBarRightPart;
            View view = linearLayoutCompat.findViewWithTag("top_bar_bluetooth");
            if (!(view instanceof AppCompatImageView)) {
                Log.e(TAG, "Cannot find bluetooth icon.");
                return;
            }
            AppCompatImageView imageView = (AppCompatImageView) view;
            imageView.setImageResource(bluetoothViewModel.getBtIconResValue());
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
        });
        batteryViewModel.getIcon().observe(this, integer -> {
            int i = integer == null ? R.drawable.baseline_battery_unknown_24 : integer;
            LinearLayoutCompat linearLayoutCompat = binding.aLauncherTopBar.topBarRightPart;
            AppCompatImageView imageView = linearLayoutCompat.findViewById(R.id.top_bar_iv_battery);
            imageView.setImageResource(i);
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