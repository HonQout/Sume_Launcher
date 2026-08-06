package com.qch.sumelauncher.ui.launcher.fragment.controlcenter;

import android.app.Application;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.data.model.controlcenter.ActivityShortcutModel;
import com.qch.sumelauncher.data.model.controlcenter.ControlCenterItemModel;
import com.qch.sumelauncher.data.model.controlcenter.SettingsShortcutModel;
import com.qch.sumelauncher.ui.settings.root.SettingsActivity;
import com.qch.sumelauncher.utils.BluetoothUtils;
import com.qch.sumelauncher.utils.WifiUtils;

import java.util.ArrayList;
import java.util.List;

public class ControlCenterViewModel extends AndroidViewModel {
    public final SettingsShortcutModel WIFI = new SettingsShortcutModel(
            "WIFI", R.string.wifi, R.drawable.baseline_wifi_day_night_24, Settings.ACTION_WIFI_SETTINGS
    );
    public final SettingsShortcutModel BLUETOOTH = new SettingsShortcutModel(
            "BLUETOOTH", R.string.bluetooth, R.drawable.baseline_bluetooth_day_night_24, Settings.ACTION_BLUETOOTH_SETTINGS
    );
    public final SettingsShortcutModel SOUND = new SettingsShortcutModel(
            "SOUND", R.string.sound, R.drawable.baseline_volume_high_day_night_24, Settings.ACTION_SOUND_SETTINGS
    );
    public final SettingsShortcutModel DISPLAY = new SettingsShortcutModel(
            "DISPLAY", R.string.display, R.drawable.baseline_brightness_medium_day_night_24, Settings.ACTION_DISPLAY_SETTINGS
    );
    public final SettingsShortcutModel SETTINGS = new SettingsShortcutModel(
            "SETTINGS", R.string.settings, R.drawable.baseline_settings_day_night_24, Settings.ACTION_SETTINGS
    );
    public final ActivityShortcutModel LAUNCHER_SETTINGS = new ActivityShortcutModel(
            "LAUNCHER_SETTINGS", R.string.launcher_settings, R.drawable.baseline_settings_day_night_24, SettingsActivity.class
    );
    public ControlCenterItemModel LOCK_SCREEN = new ControlCenterItemModel(
            "LOCK_SCREEN", R.string.lock_screen, R.drawable.baseline_lock_day_night_24
    );

    private MutableLiveData<List<ControlCenterItemModel>> mItemList = new MutableLiveData<>();

    public ControlCenterViewModel(@NonNull Application application) {
        super(application);
        // Init
        List<ControlCenterItemModel> list = new ArrayList<>();
        if (WifiUtils.isWifiSupported(application)) {
            list.add(WIFI);
        }
        if (BluetoothUtils.isBluetoothSupported(application)) {
            list.add(BLUETOOTH);
        }
        list.add(SOUND);
        list.add(DISPLAY);
        list.add(SETTINGS);
        list.add(LAUNCHER_SETTINGS);
        list.add(LOCK_SCREEN);
        mItemList.postValue(list);
    }

    public LiveData<List<ControlCenterItemModel>> getItemList() {
        return mItemList;
    }
}