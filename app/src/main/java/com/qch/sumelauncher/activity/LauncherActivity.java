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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.adapter.viewpager2.AppPagerAdapter;
import com.qch.sumelauncher.databinding.ActivityLauncherBinding;
import com.qch.sumelauncher.fragment.ManageFragment;
import com.qch.sumelauncher.utils.PermissionUtils;
import com.qch.sumelauncher.utils.UIUtils;
import com.qch.sumelauncher.viewmodel.AirplaneModeViewModel;
import com.qch.sumelauncher.viewmodel.LauncherViewModel;
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
    private LauncherViewModel launcherViewModel;
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
        binding.aLauncherBtnSettings.setOnClickListener(v -> launcherViewModel.startSettingsActivity(this));
        binding.aLauncherBtnManage.setOnClickListener(view -> {
            LauncherViewModel.LauncherState launcherState = launcherViewModel.getLauncherStateValue();
            switch (launcherState) {
                case NORMAL: {
                    launcherViewModel.setLauncherState(LauncherViewModel.LauncherState.EDIT);
                    break;
                }
                case EDIT: {
                    launcherViewModel.setLauncherState(LauncherViewModel.LauncherState.NORMAL);
                    break;
                }
                default: {
                    break;
                }
            }
        });
        binding.aLauncherBtnPrevPage.setOnClickListener(v -> launcherPageUp());
        binding.aLauncherBtnNextPage.setOnClickListener(v -> launcherPageDown());
        // Initialize viewmodel
        ViewModelProvider viewModelProvider = new ViewModelProvider(this);
        launcherViewModel = viewModelProvider.get(LauncherViewModel.class);
        timeViewModel = viewModelProvider.get(TimeViewModel.class);
        airplaneModeViewModel = viewModelProvider.get(AirplaneModeViewModel.class);
        wifiViewModel = viewModelProvider.get(WifiViewModel.class);
        bluetoothViewModel = viewModelProvider.get(BluetoothViewModel.class);
        batteryViewModel = viewModelProvider.get(BatteryViewModel.class);
        // Observe
        launcherViewModel.getDisplayStatusBar().observe(this, b ->
                UIUtils.handleStatusBarVisibility(getWindow(), b == null || b));
        launcherViewModel.getDisplayTopBar().observe(this, displayTopBar ->
                binding.aLauncherLl1.setVisibility(displayTopBar ? View.VISIBLE : View.GONE));
        launcherViewModel.getScrollToSwitchPage().observe(this, scrollToSwitchPage ->
                binding.aLauncherVp2.setUserInputEnabled(scrollToSwitchPage));
        timeViewModel.getCurrentTime().observe(this, currentTime ->
                binding.aLauncherTv1.setText(DateUtils.formatDateTime(
                                LauncherActivity.this,
                                currentTime,
                                DateUtils.FORMAT_SHOW_TIME
                        )
                ));
        timeViewModel.getCurrentTime().observe(this, currentTime ->
                binding.aLauncherTv2.setText(DateUtils.formatDateTime(
                                LauncherActivity.this,
                                currentTime,
                                DateUtils.FORMAT_NO_YEAR
                                        | DateUtils.FORMAT_SHOW_DATE
                                        | DateUtils.FORMAT_SHOW_WEEKDAY
                                        | DateUtils.FORMAT_ABBREV_WEEKDAY
                        )
                ));
        airplaneModeViewModel.getAirplaneModeEnabled().observe(this, isEnabled ->
                binding.aLauncherIv1.setVisibility(isEnabled ? View.VISIBLE : View.GONE)
        );
        wifiViewModel.getWifiEnabled().observe(this, isEnabled ->
                binding.aLauncherIv2.setVisibility(isEnabled ? View.VISIBLE : View.GONE));
        wifiViewModel.getWifiIconRes().observe(this, integer ->
                binding.aLauncherIv2.setImageResource(wifiViewModel.getWifiIconResValue()));
        bluetoothViewModel.getBtEnabled().observe(this, isEnabled ->
                binding.aLauncherIv3.setVisibility(isEnabled ? View.VISIBLE : View.GONE));
        bluetoothViewModel.getBtIconRes().observe(this, icon ->
                binding.aLauncherIv3.setImageResource(bluetoothViewModel.getBtIconResValue()));
        batteryViewModel.getLevel().observe(this, integer -> {
            int i = integer == null ? -1 : integer;
            binding.aLauncherTv3.setText(
                    String.format(
                            ContextCompat.getString(this, R.string.battery_percentage),
                            i
                    ));
        });
        batteryViewModel.getIcon().observe(this, integer -> {
            int i = integer == null ? R.drawable.baseline_battery_unknown_24 : integer;
            binding.aLauncherIv4.setImageResource(i);
        });
        launcherViewModel.getNumPage().observe(this, integer -> {
            appPagerAdapter.setNumPages(integer);
            binding.aLauncherTv4.setText(
                    String.format(
                            ContextCompat.getString(this, R.string.page_text),
                            launcherViewModel.getCurrentPageValue(), integer
                    ));
        });
        launcherViewModel.getCurrentPage().observe(this, integer ->
                binding.aLauncherTv4.setText(
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
            Fragment manageFragment = fragmentManager.findFragmentByTag("ManageFragment");
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (launcherState == LauncherViewModel.LauncherState.NORMAL) {
                if (manageFragment != null) {
                    Log.i(TAG, "Fragment is not null. Hide fragment.");
                    fragmentTransaction.hide(manageFragment);
                }
                binding.aLauncherLl3.setVisibility(View.VISIBLE);
            } else if (launcherState == LauncherViewModel.LauncherState.EDIT) {
                binding.aLauncherLl3.setVisibility(View.GONE);
                if (manageFragment == null) {
                    Log.i(TAG, "Fragment is null. Create new instance of fragment.");
                    manageFragment = ManageFragment.newInstance();
                    fragmentTransaction.add(R.id.a_launcher_fl2, manageFragment, "ManageFragment");
                } else {
                    Log.i(TAG, "Fragment is not null. Show fragment.");
                    fragmentTransaction.show(manageFragment);
                }
            }
            fragmentTransaction.commit();
        });
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