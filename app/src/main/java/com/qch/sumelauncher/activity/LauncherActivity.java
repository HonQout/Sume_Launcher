package com.qch.sumelauncher.activity;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.adapter.viewpager2.AppPagerAdapter;
import com.qch.sumelauncher.application.MyApplication;
import com.qch.sumelauncher.databinding.ActivityLauncherBinding;
import com.qch.sumelauncher.fragment.DrawerFragment;
import com.qch.sumelauncher.fragment.SettingsFragment;
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
        ViewPager2 viewPager2 = binding.aLauncherVp2;
        AppPagerAdapter appPagerAdapter = new AppPagerAdapter(this, 0);
        viewPager2.setAdapter(appPagerAdapter);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                Log.i(TAG, "ViewPager2 onPageSelected position " + (position + 1));
                launcherViewModel.setCurrentScreen(position + 1);
            }
        });
        binding.aLauncherBtnBack.setOnClickListener(v ->
                launcherViewModel.setLauncherState(LauncherViewModel.LauncherState.NORMAL));
        binding.aLauncherBtnSettings.setOnClickListener(v ->
                launcherViewModel.setLauncherState(LauncherViewModel.LauncherState.SETTINGS));
        binding.aLauncherBtnEdit.setOnClickListener(v ->
                launcherViewModel.setLauncherState(LauncherViewModel.LauncherState.EDIT));
        binding.aLauncherBtnApps.setOnClickListener(v ->
                launcherViewModel.setLauncherState(LauncherViewModel.LauncherState.APPS));
        binding.aLauncherBtnPrevPage.setOnClickListener(v -> launcherPageUp());
        binding.aLauncherBtnNextPage.setOnClickListener(v -> launcherPageDown());
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
        launcherViewModel.getScrollToSwitchPage().observe(this, scrollToSwitchPage ->
                binding.aLauncherVp2.setUserInputEnabled(scrollToSwitchPage));
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
            if (!(view instanceof AppCompatImageView imageView)) {
                Log.e(TAG, "Cannot find wifi icon.");
                return;
            }
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
            if (!(view instanceof AppCompatImageView imageView)) {
                Log.e(TAG, "Cannot find bluetooth icon.");
                return;
            }
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
        launcherViewModel.getNumScreen().observe(this, integer -> {
            if (integer != null) {
                appPagerAdapter.setNumScreen(integer);
                binding.aLauncherTvPage.setText(
                        String.format(
                                ContextCompat.getString(this, R.string.page_text),
                                launcherViewModel.getCurrentScreenValue(), integer
                        ));
            }
        });
        launcherViewModel.getCurrentScreen().observe(this, integer -> {
            if (integer != null) {
                binding.aLauncherTvPage.setText(
                        String.format(
                                ContextCompat.getString(this, R.string.page_text),
                                integer, launcherViewModel.getNumScreenValue()
                        ));
            }
        });
        launcherViewModel.getLauncherState().observe(this, launcherState -> {
            Log.i(TAG, "Prepare to examine launcher state.");
            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment settingsFragment = fragmentManager.findFragmentByTag("SettingsFragment");
            Fragment appListFragment = fragmentManager.findFragmentByTag("DrawerFragment");
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (launcherState == LauncherViewModel.LauncherState.NORMAL) {
                if (settingsFragment != null) {
                    fragmentTransaction.hide(settingsFragment);
                    fragmentTransaction.remove(settingsFragment);
                }
                if (appListFragment != null) {
                    fragmentTransaction.hide(appListFragment);
                    fragmentTransaction.remove(appListFragment);
                }
                binding.aLauncherLlTitle.setVisibility(View.GONE);
                binding.aLauncherTvTitle.setText("");
                binding.aLauncherVp2.setVisibility(View.VISIBLE);
                binding.aLauncherLlAction.setVisibility(View.VISIBLE);
                binding.aLauncherLlPage.setVisibility(View.VISIBLE);
            } else if (launcherState == LauncherViewModel.LauncherState.EDIT) {
                binding.aLauncherLlAction.setVisibility(View.GONE);
                binding.aLauncherTvTitle.setText(R.string.edit);
                binding.aLauncherLlTitle.setVisibility(View.VISIBLE);
            } else if (launcherState == LauncherViewModel.LauncherState.SETTINGS) {
                binding.aLauncherLlAction.setVisibility(View.GONE);
                binding.aLauncherLlPage.setVisibility(View.GONE);
                binding.aLauncherVp2.setVisibility(View.GONE);
                binding.aLauncherTvTitle.setText(R.string.settings);
                binding.aLauncherLlTitle.setVisibility(View.VISIBLE);
                if (settingsFragment == null) {
                    fragmentTransaction.add(R.id.a_launcher_fcv, new SettingsFragment(), "SettingsFragment");
                } else {
                    fragmentTransaction.replace(R.id.a_launcher_fcv, new SettingsFragment(), "SettingsFragment");
                }
            } else if (launcherState == LauncherViewModel.LauncherState.APPS) {
                binding.aLauncherLlAction.setVisibility(View.GONE);
                binding.aLauncherLlPage.setVisibility(View.GONE);
                binding.aLauncherVp2.setVisibility(View.GONE);
                binding.aLauncherTvTitle.setText(R.string.app_drawer);
                binding.aLauncherLlTitle.setVisibility(View.VISIBLE);
                if (appListFragment == null) {
                    fragmentTransaction.add(R.id.a_launcher_fcv, DrawerFragment.newInstance(), "DrawerFragment");
                } else {
                    fragmentTransaction.replace(R.id.a_launcher_fcv, DrawerFragment.newInstance(), "DrawerFragment");
                }
            }
            fragmentTransaction.commit();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP: {
                if (launcherViewModel.getVolumeKeySwitchPageValue()) {
                    launcherPageUp();
                    return true;
                }
                break;
            }
            case KeyEvent.KEYCODE_VOLUME_DOWN: {
                if (launcherViewModel.getVolumeKeySwitchPageValue()) {
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
        ViewPager2 viewPager2 = binding.aLauncherVp2;
        if (viewPager2.getAdapter() != null) {
            int currentItem = viewPager2.getCurrentItem();
            if (currentItem > 0) {
                currentItem -= 1;
                viewPager2.setCurrentItem(currentItem, false);
            }
        }
    }

    public void launcherPageDown() {
        ViewPager2 viewPager2 = binding.aLauncherVp2;
        if (viewPager2.getAdapter() != null) {
            int currentItem = viewPager2.getCurrentItem();
            if (currentItem < viewPager2.getAdapter().getItemCount() - 1) {
                currentItem += 1;
                viewPager2.setCurrentItem(currentItem, false);
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