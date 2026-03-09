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
import com.qch.sumelauncher.databinding.ActivityLauncherBinding;
import com.qch.sumelauncher.fragment.DrawerFragment;
import com.qch.sumelauncher.fragment.SettingsFragment;
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
                launcherViewModel.setCurrentPage(position + 1);
            }
        });
        binding.aLauncherBtnBack.setOnClickListener(v ->
                launcherViewModel.setLauncherState(LauncherViewModel.LauncherState.NORMAL));
        binding.aLauncherBtnSettings.setOnClickListener(v ->
                launcherViewModel.setLauncherState(LauncherViewModel.LauncherState.SETTINGS));
        binding.aLauncherBtnSearch.setOnClickListener(v ->
                launcherViewModel.setLauncherState(LauncherViewModel.LauncherState.SEARCH));
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
                binding.aLauncherLlTop.setVisibility(displayTopBar ? View.VISIBLE : View.GONE));
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
                layoutParams.setMargins(0, 0, 0, 0);
                imageView.setLayoutParams(layoutParams);
                LinearLayoutCompat linearLayoutCompat = binding.aLauncherTopBar.topBarRightPart;
                linearLayoutCompat.addView(imageView);
            } else {
                LinearLayoutCompat linearLayoutCompat = binding.aLauncherTopBar.topBarRightPart;
                linearLayoutCompat.removeView(linearLayoutCompat.findViewWithTag("top_bar_airplane_mode"));
            }
        });
        wifiViewModel.getWifiEnabled().observe(this, isEnabled ->
                binding.aLauncherIvWifi.setVisibility(isEnabled ? View.VISIBLE : View.GONE));
        wifiViewModel.getWifiIconRes().observe(this, integer ->
                binding.aLauncherIvWifi.setImageResource(wifiViewModel.getWifiIconResValue()));
        bluetoothViewModel.getBtEnabled().observe(this, isEnabled ->
                binding.aLauncherIvBluetooth.setVisibility(isEnabled ? View.VISIBLE : View.GONE));
        bluetoothViewModel.getBtIconRes().observe(this, icon ->
                binding.aLauncherIvBluetooth.setImageResource(bluetoothViewModel.getBtIconResValue()));
        batteryViewModel.getLevel().observe(this, integer -> {
            int i = integer == null ? -1 : integer;
            binding.aLauncherTvBattery.setText(
                    String.format(
                            ContextCompat.getString(this, R.string.battery_percentage),
                            i
                    ));
        });
        batteryViewModel.getIcon().observe(this, integer -> {
            int i = integer == null ? R.drawable.baseline_battery_unknown_24 : integer;
            binding.aLauncherIvBattery.setImageResource(i);
        });
        launcherViewModel.getNumPage().observe(this, integer -> {
            if (integer != null) {
                appPagerAdapter.setNumPages(integer);
                binding.aLauncherTvPage.setText(
                        String.format(
                                ContextCompat.getString(this, R.string.page_text),
                                launcherViewModel.getCurrentPageValue(), integer
                        ));
            }
        });
        launcherViewModel.getCurrentPage().observe(this, integer ->
                binding.aLauncherTvPage.setText(
                        String.format(
                                ContextCompat.getString(
                                        LauncherActivity.this,
                                        R.string.page_text
                                ),
                                integer, launcherViewModel.getNumPageValue()
                        )));
        launcherViewModel.getLauncherState().observe(this, launcherState -> {
            Log.i(TAG, "Prepare to examine launcher state");
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
            } else if (launcherState == LauncherViewModel.LauncherState.SEARCH) {
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
            wifiViewModel.update();
        } else if (launcherViewModel.getAskForPermFineLocationValue()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                launcherViewModel.showPermFineLocationDialog(this);
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
            }
            viewPager2.setCurrentItem(currentItem, false);
            launcherViewModel.setCurrentPage(currentItem + 1);
        }
    }

    public void launcherPageDown() {
        ViewPager2 viewPager2 = binding.aLauncherVp2;
        if (viewPager2.getAdapter() != null) {
            int currentItem = viewPager2.getCurrentItem();
            if (currentItem < viewPager2.getAdapter().getItemCount() - 1) {
                currentItem += 1;
            }
            viewPager2.setCurrentItem(currentItem, false);
            launcherViewModel.setCurrentPage(currentItem + 1);
        }
    }
}