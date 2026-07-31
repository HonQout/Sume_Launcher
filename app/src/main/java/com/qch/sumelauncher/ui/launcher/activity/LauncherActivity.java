package com.qch.sumelauncher.ui.launcher.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.qch.sumelauncher.R;
import com.qch.sumelauncher.application.MyApplication;
import com.qch.sumelauncher.data.model.launcher.ActivityModel;
import com.qch.sumelauncher.data.model.launcher.IconModel;
import com.qch.sumelauncher.databinding.ActivityLauncherBinding;
import com.qch.sumelauncher.recyclerview.adapter.FilterableListAdapter;
import com.qch.sumelauncher.recyclerview.adapter.GridDrawerRVAdapter;
import com.qch.sumelauncher.recyclerview.decoration.VerticalGridDecoration;
import com.qch.sumelauncher.ui.launcher.page.LauncherLayout;
import com.qch.sumelauncher.ui.launcher.page.LauncherPageAdapter;
import com.qch.sumelauncher.ui.settings.root.SettingsActivity;
import com.qch.sumelauncher.ui.settings.root.SettingsViewModel;
import com.qch.sumelauncher.ui.topbar.view.TopBarView;
import com.qch.sumelauncher.ui.topbar.viewmodel.RingerModeViewModel;
import com.qch.sumelauncher.utils.ApplicationUtils;
import com.qch.sumelauncher.utils.DialogUtils;
import com.qch.sumelauncher.utils.IntentUtils;
import com.qch.sumelauncher.utils.PermissionUtils;
import com.qch.sumelauncher.utils.UIUtils;
import com.qch.sumelauncher.ui.topbar.viewmodel.AirplaneModeViewModel;
import com.qch.sumelauncher.ui.topbar.viewmodel.BatteryViewModel;
import com.qch.sumelauncher.ui.topbar.viewmodel.BluetoothViewModel;
import com.qch.sumelauncher.ui.topbar.viewmodel.TimeViewModel;
import com.qch.sumelauncher.ui.topbar.viewmodel.WifiViewModel;

import java.util.ArrayList;
import java.util.List;

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
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private OnBackPressedCallback onBackPressedCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLauncherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UIUtils.setViewFitsSystemWindows(binding.getRoot());
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
                UIUtils.forceHandleStatusBarVisibility(getWindow(), b == null || b));
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
        // Launcher
        // Initialize ViewPager2
        LauncherPageAdapter launcherPageAdapter = new LauncherPageAdapter();
        ViewPager2 viewPager2 = binding.aLauncherRoot.launcherVp2;
        viewPager2.setAdapter(launcherPageAdapter);
        // Set page change callback
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                Log.i(TAG, "ViewPager2 onPageSelected position #" + (position + 1));
                launcherViewModel.setCurrentScreenIndex(position);
                binding.aLauncherRoot.launcherTvPage.setText(
                        String.format(
                                ContextCompat.getString(LauncherActivity.this, R.string.page_text),
                                position + 1,
                                launcherViewModel.getNumScreenValue()
                        ));
            }
        });
        // Observe
        bottomSheetBehavior = BottomSheetBehavior.from(binding.aLauncherFl);
        bottomSheetBehavior.setSkipCollapsed(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.i(TAG, "State of BottomSheetBehavior is " + bottomSheetBehavior.getState());
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    launcherViewModel.setLauncherState(LauncherViewModel.LauncherState.LAUNCHER);
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
        launcherViewModel.getLauncherState().observe(this, launcherState -> {
            if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
                if (launcherState == LauncherViewModel.LauncherState.LAUNCHER) {
                    Log.i(TAG, "Launcher state is LAUNCHER. ");
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                } else if (launcherState == LauncherViewModel.LauncherState.APPS) {
                    Log.i(TAG, "Launcher state is APPS. ");
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });
        launcherViewModel.getGridSize().observe(this, gridSize -> {
            if (gridSize == null) {
                Log.e(TAG, "Grid size is null.");
                return;
            }
            launcherPageAdapter.setGridSize(gridSize);
        });
        launcherViewModel.getIconModelMap().observe(this, map -> {
            if (map == null) {
                Log.e(TAG, "Map of paged icons is null.");
                return;
            }
            launcherPageAdapter.setMap(map);
            launcherPageAdapter.setOnIconClickListener(new LauncherLayout.OnIconClickListener() {
                @Override
                public void onClick(@Nullable View view, IconModel item) {
                    if (view == null) {
                        Log.e(TAG, "Clicked IconModel is null.");
                        return;
                    }
                    IntentUtils.launchActivity(LauncherActivity.this, item.getPackageName(),
                            item.getActivityName(), true);
                }

                @Override
                public boolean onLongClick(@Nullable View view, IconModel item) {
                    if (view == null) {
                        Log.e(TAG, "Long clicked IconModel is null.");
                        return false;
                    }
                    showLauncherIconMenu(view, item);
                    return true;
                }
            });
            launcherPageAdapter.setOnBlankAreaClickListener(new LauncherLayout.OnBlankAreaClickListener() {
                @Override
                public void onClick(int x, int y) {
                    Log.i(TAG, "Clicked blank cell " + x + "," + y);
                }

                @Override
                public boolean onLongClick(int x, int y) {
                    Log.i(TAG, "Long clicked blank cell " + x + "," + y);
                    return true;
                }
            });
        });
        // Set num screen
        launcherViewModel.getNumScreen().observe(this, integer -> {
            if (integer != null) {
                binding.aLauncherRoot.launcherTvPage.setText(
                        String.format(
                                ContextCompat.getString(this, R.string.page_text),
                                launcherViewModel.getCurrentScreenIndexValue() + 1,
                                integer
                        )
                );
            }
        });
        // Pattern of switching between pages
        settingsViewModel.getScrollToSwitchPage().observe(this, scrollToSwitchPage ->
                binding.aLauncherRoot.launcherVp2.setUserInputEnabled(scrollToSwitchPage));
        // Set button callback
        // Settings button
        binding.aLauncherRoot.launcherBtnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(LauncherActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
        // Edit button
        binding.aLauncherRoot.launcherBtnEdit.setOnClickListener(v -> {
        });
        // Apps button
        binding.aLauncherRoot.launcherBtnApps.setOnClickListener(v ->
                launcherViewModel.setLauncherState(LauncherViewModel.LauncherState.APPS));
        // Prev page button
        binding.aLauncherRoot.launcherBtnPrevPage.setOnClickListener(v -> launcherPageUp());
        // Next page button
        binding.aLauncherRoot.launcherBtnNextPage.setOnClickListener(v -> launcherPageDown());
        // Handle permission request
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        wifiViewModel.update();
                    }
                });

        // Drawer
        int gridColumnCount = getResources().getInteger(R.integer.grid_column_count);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, gridColumnCount);
        GridDrawerRVAdapter gridDrawerRVAdapter = new GridDrawerRVAdapter(new ArrayList<>());
        gridDrawerRVAdapter.setOnItemClickListener(new FilterableListAdapter.OnItemClickListener<>() {
            @Override
            public void onItemClick(ActivityModel item, View view) {
                IntentUtils.handleLaunchActivityResult(LauncherActivity.this,
                        IntentUtils.launchActivity(LauncherActivity.this,
                                item.getPackageName(), item.getActivityName(), true));
            }

            @Override
            public boolean onItemLongClick(ActivityModel item, View view) {
                showGridIconMenu(view, item);
                return true;
            }
        });
        VerticalGridDecoration verticalGridDecoration = new VerticalGridDecoration(
                getResources().getInteger(R.integer.grid_column_count), 0, 0);
        binding.aLauncherDrawer.drawerRv.setLayoutManager(gridLayoutManager);
        binding.aLauncherDrawer.drawerRv.setAdapter(gridDrawerRVAdapter);
        binding.aLauncherDrawer.drawerRv.addItemDecoration(verticalGridDecoration);
        binding.aLauncherDrawer.drawerSv.setOnQueryTextFocusChangeListener((v, hasFocus) ->
                binding.aLauncherDrawer.drawerBtnQuit.setVisibility(View.VISIBLE));
        binding.aLauncherDrawer.drawerSv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                GridDrawerRVAdapter adapter = (GridDrawerRVAdapter) binding.aLauncherDrawer.drawerRv.getAdapter();
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                }
                return true;
            }
        });
        binding.aLauncherDrawer.drawerBtnQuit.setOnClickListener(v -> {
            binding.aLauncherDrawer.drawerSv.setQuery("", false);
            binding.aLauncherDrawer.drawerSv.clearFocus();
            binding.aLauncherDrawer.drawerBtnQuit.setVisibility(View.GONE);
        });
        launcherViewModel.getActivityModelList().observe(LauncherActivity.this, gridDrawerRVAdapter::setList);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP: {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (settingsViewModel.getVolumeKeySwitchPageValue()) {
                        LauncherViewModel.LauncherState launcherState = launcherViewModel.getLauncherStateValue();
                        if (launcherState == LauncherViewModel.LauncherState.LAUNCHER) {
                            launcherPageUp();
                            return true;
                        }
                    }
                }
                break;
            }
            case KeyEvent.KEYCODE_VOLUME_DOWN: {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (settingsViewModel.getVolumeKeySwitchPageValue()) {
                        LauncherViewModel.LauncherState launcherState = launcherViewModel.getLauncherStateValue();
                        if (launcherState == LauncherViewModel.LauncherState.LAUNCHER) {
                            launcherPageDown();
                            return true;
                        }
                    }
                }
                break;
            }
            default:
                break;
        }
        return false;
    }

    private void showPermFineLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.request_permission)
                .setMessage(R.string.perm_fine_location_reason)
                .setPositiveButton(R.string.app_info, (dialog, which) ->
                        IntentUtils.openAppDetailsPage(this, this.getPackageName()))
                .setNeutralButton(R.string.deny, (dialog, which) ->
                        MyApplication.getPreferenceDataStore().putBoolean("ask_for_perm_fine_location", false))
                .setNegativeButton(R.string.cancel, null);
        DialogUtils.show(builder, settingsViewModel.getAnimationValue());
    }

    private void launcherPageUp() {
        ViewPager2 viewPager2 = binding.aLauncherRoot.launcherVp2;
        if (viewPager2.getAdapter() != null) {
            int currentItem = viewPager2.getCurrentItem();
            if (currentItem > 0) {
                currentItem -= 1;
                viewPager2.setCurrentItem(currentItem, false);
            }
        }
    }

    private void launcherPageDown() {
        ViewPager2 viewPager2 = binding.aLauncherRoot.launcherVp2;
        if (viewPager2.getAdapter() != null) {
            int currentItem = viewPager2.getCurrentItem();
            if (currentItem < launcherViewModel.getNumScreenValue() - 1) {
                currentItem += 1;
                viewPager2.setCurrentItem(currentItem, false);
            }
        }
    }

    private void showLauncherIconMenu(@NonNull View view, @NonNull IconModel iconModel) {
        Context context = view.getContext();

        ActivityInfo activityInfo = ApplicationUtils.getActivityInfo(
                context,
                iconModel.getPackageName(),
                iconModel.getActivityName()
        );
        if (activityInfo == null) {
            Log.e(TAG, "Cannot find corresponding ActivityInfo.");
            return;
        }
        ActivityModel activityModel = new ActivityModel(context, activityInfo);

        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.launcher_icon_op_menu, popupMenu.getMenu());
        int baseIndex = popupMenu.getMenu().size();
        List<ShortcutInfo> shortcutInfoList;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutInfoList = activityModel.getShortcutInfoList();
            for (int i = 0; i < shortcutInfoList.size(); i++) {
                ShortcutInfo shortcutInfo = shortcutInfoList.get(i);
                popupMenu.getMenu().add(
                        0,
                        baseIndex + i,
                        baseIndex + i,
                        shortcutInfo.getShortLabel()
                );
            }
        } else {
            shortcutInfoList = new ArrayList<>();
        }
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int menuId = menuItem.getItemId();
            if (menuId == R.id.remove_icon) {
                launcherViewModel.removeIcon(iconModel.toIconEntity());
                return true;
            } else if (menuId == R.id.uninstall) {
                ApplicationUtils.ApplicationType type = ApplicationUtils.getApplicationType(
                        LauncherActivity.this, activityModel.getPackageName());
                if (getPackageName().equals(activityModel.getPackageName())) {
                    showUninstallThisAppDialog();
                } else if (type == ApplicationUtils.ApplicationType.UPDATED_SYSTEM
                        || type == ApplicationUtils.ApplicationType.USER) {
                    IntentUtils.handleLaunchIntentResult(
                            LauncherActivity.this,
                            IntentUtils.requireUninstallApp(LauncherActivity.this, activityModel.getPackageName())
                    );
                } else if (type == ApplicationUtils.ApplicationType.SYSTEM) {
                    showUninstallSystemAppDialog(activityModel.getPackageName());
                } else {
                    Toast.makeText(this, R.string.cannot_uninstall_app, Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (menuId == R.id.app_info) {
                IntentUtils.handleLaunchIntentResult(
                        this,
                        IntentUtils.openAppDetailsPage(LauncherActivity.this, activityModel.getPackageName())
                );
                return true;
            } else if (menuId == R.id.app_market) {
                IntentUtils.handleLaunchIntentResult(
                        this,
                        IntentUtils.openAppInMarket(LauncherActivity.this, activityModel.getPackageName())
                );
                return true;
            } else if (menuId >= baseIndex) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    ShortcutInfo shortcutInfo = shortcutInfoList.get(menuId - baseIndex);
                    ApplicationUtils.launchAppShortcut(context, activityModel.getPackageName(),
                            shortcutInfo.getId());
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        });
        popupMenu.show();
    }

    private void showGridIconMenu(@NonNull View view, @NonNull ActivityModel item) {
        PopupMenu popupMenu = new PopupMenu(LauncherActivity.this, view);
        popupMenu.getMenuInflater().inflate(R.menu.app_op_menu, popupMenu.getMenu());
        int baseIndex = popupMenu.getMenu().size();
        List<ShortcutInfo> shortcutInfoList;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutInfoList = item.getShortcutInfoList();
            for (int i = 0; i < shortcutInfoList.size(); i++) {
                ShortcutInfo shortcutInfo = shortcutInfoList.get(i);
                popupMenu.getMenu()
                        .add(0, baseIndex + i, baseIndex + i, shortcutInfo.getShortLabel());
            }
        } else {
            shortcutInfoList = new ArrayList<>();
        }
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int menuId = menuItem.getItemId();
            if (menuId == R.id.app_info) {
                IntentUtils.handleLaunchIntentResult(
                        LauncherActivity.this,
                        IntentUtils.openAppDetailsPage(LauncherActivity.this, item.getPackageName())
                );
                return true;
            } else if (menuId == R.id.uninstall) {
                ApplicationUtils.ApplicationType type = ApplicationUtils.getApplicationType(
                        LauncherActivity.this, item.getPackageName());
                if (getPackageName().equals(item.getPackageName())) {
                    showUninstallThisAppDialog();
                } else if (type == ApplicationUtils.ApplicationType.UPDATED_SYSTEM
                        || type == ApplicationUtils.ApplicationType.USER) {
                    IntentUtils.handleLaunchIntentResult(
                            LauncherActivity.this,
                            IntentUtils.requireUninstallApp(LauncherActivity.this, item.getPackageName())
                    );
                } else if (type == ApplicationUtils.ApplicationType.SYSTEM) {
                    showUninstallSystemAppDialog(item.getPackageName());
                } else {
                    Toast.makeText(LauncherActivity.this, R.string.cannot_uninstall_app, Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (menuId == R.id.app_market) {
                IntentUtils.handleLaunchIntentResult(
                        LauncherActivity.this,
                        IntentUtils.openAppInMarket(LauncherActivity.this, item.getPackageName())
                );
                return true;
            } else if (menuId >= baseIndex) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    ShortcutInfo shortcutInfo = shortcutInfoList.get(menuId - baseIndex);
                    ApplicationUtils.launchAppShortcut(LauncherActivity.this, item.getPackageName(),
                            shortcutInfo.getId());
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        });
        popupMenu.show();
    }

    private void showUninstallSystemAppDialog(String packageName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.hint)
                .setMessage(R.string.insist_uninstall_system_app)
                .setPositiveButton(R.string.uninstall, (dialog, which) ->
                        IntentUtils.handleLaunchIntentResult(
                                this,
                                IntentUtils.requireUninstallApp(this, packageName)
                        ))
                .setNegativeButton(R.string.cancel, null);
        DialogUtils.show(builder, settingsViewModel.getAnimationValue());
    }

    private void showUninstallThisAppDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.hint)
                .setMessage(R.string.insist_uninstall_this_app)
                .setPositiveButton(R.string.uninstall, (dialog, which) ->
                        IntentUtils.handleLaunchIntentResult(
                                this,
                                IntentUtils.requireUninstallApp(this, getPackageName())
                        ))
                .setNegativeButton(R.string.cancel, null);
        DialogUtils.show(builder, settingsViewModel.getAnimationValue());
    }
}