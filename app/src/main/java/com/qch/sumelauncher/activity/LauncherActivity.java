package com.qch.sumelauncher.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.adapter.viewpager2.AppPagerAdapter;
import com.qch.sumelauncher.databinding.ActivityMainBinding;
import com.qch.sumelauncher.viewmodel.AppViewModel;
import com.qch.sumelauncher.viewmodel.BatteryViewModel;
import com.qch.sumelauncher.viewmodel.TimeViewModel;

public class LauncherActivity extends AppCompatActivity {
    private static final String TAG = "LauncherActivity";
    private ActivityMainBinding binding;
    private TimeViewModel timeViewModel;
    private BatteryViewModel batteryViewModel;
    private AppViewModel appViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Immersive system bars
        EdgeToEdge.enable(this);
        // Set view
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
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
        binding.aMainBtnPrevPage.setOnClickListener(v -> {
            launcherPageUp();
        });
        binding.aMainBtnNextPage.setOnClickListener(v -> {
            launcherPageDown();
        });
        // Initialize viewmodel
        appViewModel = new ViewModelProvider(this).get(AppViewModel.class);
        timeViewModel = new ViewModelProvider(this).get(TimeViewModel.class);
        batteryViewModel = new ViewModelProvider(this).get(BatteryViewModel.class);
        // Observe
        appViewModel.getDisplayTopBar().observe(this, displayTopBar ->
                binding.aMainLl1.setVisibility(displayTopBar ? View.VISIBLE : View.GONE));
        appViewModel.getAllowScrollPage().observe(this, allowScrollPage ->
                binding.aMainVp2.setUserInputEnabled(allowScrollPage));
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
        batteryViewModel.getLevel().observe(this, integer -> {
            int i = integer == null ? -1 : integer;
            binding.aMainTv3.setText(i + "%");
        });
        batteryViewModel.getIcon().observe(this, integer -> {
            int i = integer == null ? R.drawable.baseline_battery_unknown_24 : integer;
            binding.aMainIv1.setImageResource(i);
        });
        appViewModel.getNumPage().observe(this, integer -> {
            appPagerAdapter.setNumPages(integer);
            binding.aMainTv4.setText(
                    String.format(
                            ContextCompat.getString(
                                    LauncherActivity.this,
                                    R.string.page_text
                            ),
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(appViewModel.spListener);
        appViewModel.getStoredPreferences(sharedPreferences);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(appViewModel.spListener);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP: {
                launcherPageUp();
                return true;
            }
            case KeyEvent.KEYCODE_VOLUME_DOWN: {
                launcherPageDown();
                return true;
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