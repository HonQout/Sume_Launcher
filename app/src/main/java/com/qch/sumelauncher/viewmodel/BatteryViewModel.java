package com.qch.sumelauncher.viewmodel;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.utils.BatteryUtils;

public class BatteryViewModel extends AndroidViewModel {
    private static final String TAG = "BatteryViewModel";

    // data
    private final MutableLiveData<Integer> mLevel = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsCharging = new MutableLiveData<>();
    @DrawableRes
    private final MutableLiveData<Integer> mIcon = new MutableLiveData<>();

    // broadcast receiver
    private BroadcastReceiver batteryBroadcastReceiver;

    public BatteryViewModel(@NonNull Application application) {
        super(application);
        registerBatteryBR();
        init();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        unregisterBatteryBR();
    }

    private void registerBatteryBR() {
        if (batteryBroadcastReceiver != null) {
            return;
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

        batteryBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                float batteryPct = level * 100 / (float) scale;
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;
                mLevel.postValue((int) batteryPct);
                mIsCharging.postValue(isCharging);
                mIcon.postValue(getBatteryIcon(level, isCharging));
            }
        };

        ContextCompat.registerReceiver(getApplication(), batteryBroadcastReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void unregisterBatteryBR() {
        if (batteryBroadcastReceiver != null) {
            getApplication().unregisterReceiver(batteryBroadcastReceiver);
            batteryBroadcastReceiver = null;
        }
    }

    private void init() {
        int level = BatteryUtils.getBatteryLevel(getApplication());
        boolean isCharging = BatteryUtils.getIsCharging(getApplication());
        mLevel.postValue(level);
        mIsCharging.postValue(isCharging);
        mIcon.postValue(getBatteryIcon(level, isCharging));
    }

    public LiveData<Integer> getLevel() {
        return mLevel;
    }

    public LiveData<Boolean> getIsCharging() {
        return mIsCharging;
    }

    public LiveData<Integer> getIcon() {
        return mIcon;
    }

    @DrawableRes
    public int getBatteryIcon(int level, boolean isCharging) {
        if (isCharging) {
            return R.drawable.baseline_battery_charging_full_24;
        } else {
            if (level < 0) {
                return R.drawable.baseline_battery_unknown_24;
            } else if (level == 0) {
                return R.drawable.baseline_battery_alert_24;
            } else if (level < 100 / 7) {
                return R.drawable.baseline_battery_0_bar_24;
            } else if (level < 100 / 7 * 2) {
                return R.drawable.baseline_battery_1_bar_24;
            } else if (level < 100 / 7 * 3) {
                return R.drawable.baseline_battery_2_bar_24;
            } else if (level < 100 / 7 * 4) {
                return R.drawable.baseline_battery_3_bar_24;
            } else if (level < 100 / 7 * 5) {
                return R.drawable.baseline_battery_4_bar_24;
            } else if (level < 100 / 7 * 6) {
                return R.drawable.baseline_battery_5_bar_24;
            } else if (level < 100) {
                return R.drawable.baseline_battery_6_bar_24;
            } else if (level == 100) {
                return R.drawable.baseline_battery_full_24;
            } else {
                return R.drawable.baseline_battery_unknown_24;
            }
        }
    }
}