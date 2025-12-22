package com.qch.sumelauncher.activity;

import android.Manifest;
import android.os.Build;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.adapter.viewpager2.AppPagerAdapter;
import com.qch.sumelauncher.databinding.ActivityLauncherBinding;
import com.qch.sumelauncher.utils.PermissionUtils;
import com.qch.sumelauncher.utils.UIUtils;
import com.qch.sumelauncher.viewmodel.AirplaneModeViewModel;
import com.qch.sumelauncher.viewmodel.AppViewModel;
import com.qch.sumelauncher.viewmodel.BatteryViewModel;
import com.qch.sumelauncher.viewmodel.BluetoothViewModel;
import com.qch.sumelauncher.viewmodel.WifiViewModel;
import com.qch.sumelauncher.viewmodel.TimeViewModel;

public class LauncherActivity extends AppCompatActivity {
    private static final String TAG = "LauncherActivity";
    private ActivityLauncherBinding binding;
    private TimeViewModel timeViewModel;
    private AirplaneModeViewModel airplaneModeViewModel;
    private WifiViewModel wifiViewModel;
    private BluetoothViewModel bluetoothViewModel;
    private BatteryViewModel batteryViewModel;
    private AppViewModel appViewModel;
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
        ViewPager2 viewPager2 = binding.aMainVp2;
        AppPagerAdapter appPagerAdapter = new AppPagerAdapter(this, 0);
        viewPager2.setAdapter(appPagerAdapter);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                appViewModel.setCurrentPage(position + 1);
            }
        });
        binding.aMainBtnSettings.setOnClickListener(v -> appViewModel.startSettingsActivity(this));
        binding.aMainBtnPrevPage.setOnClickListener(v -> launcherPageUp());
        binding.aMainBtnNextPage.setOnClickListener(v -> launcherPageDown());
        // Initialize viewmodel
        ViewModelProvider viewModelProvider = new ViewModelProvider(this);
        appViewModel = viewModelProvider.get(AppViewModel.class);
        timeViewModel = viewModelProvider.get(TimeViewModel.class);
        airplaneModeViewModel = viewModelProvider.get(AirplaneModeViewModel.class);
        wifiViewModel = viewModelProvider.get(WifiViewModel.class);
        bluetoothViewModel = viewModelProvider.get(BluetoothViewModel.class);
        batteryViewModel = viewModelProvider.get(BatteryViewModel.class);
        // Observe
        appViewModel.getDisplayStatusBar().observe(this, b ->
                UIUtils.handleStatusBarVisibility(getWindow(), b == null || b));
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
        airplaneModeViewModel.getAirplaneModeEnabled().observe(this, isEnabled -> {
                    binding.aMainIv1.setVisibility(isEnabled ? View.VISIBLE : View.GONE);
                }
        );
        wifiViewModel.getWifiEnabled().observe(this, isEnabled ->
                binding.aMainIv2.setVisibility(isEnabled ? View.VISIBLE : View.GONE));
        wifiViewModel.getWifiIconRes().observe(this, integer ->
                binding.aMainIv2.setImageResource(wifiViewModel.getWifiIconResInt()));
        bluetoothViewModel.getBtEnabled().observe(this, isEnabled ->
                binding.aMainIv3.setVisibility(isEnabled ? View.VISIBLE : View.GONE));
        bluetoothViewModel.getBtIconRes().observe(this, icon ->
                binding.aMainIv3.setImageResource(bluetoothViewModel.getBtIconResInt()));
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
            binding.aMainIv4.setImageResource(i);
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
    protected void onResume() {
        super.onResume();
        // check if permissions are granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ||
                PermissionUtils.isPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            wifiViewModel.init();
        } else if (appViewModel.getAskForPermFineLocationBoolean()) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                appViewModel.showPermFineLocationDialog(this);
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
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