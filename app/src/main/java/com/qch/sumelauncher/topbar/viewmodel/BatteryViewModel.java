package com.qch.sumelauncher.topbar.viewmodel;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.qch.sumelauncher.utils.BatteryUtils;

public class BatteryViewModel extends AndroidViewModel {
    private static final String TAG = "BatteryViewModel";
    // data
    private final MutableLiveData<Integer> mLevel = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mIsCharging = new MutableLiveData<>();
    // broadcast receiver
    private BroadcastReceiver broadcastReceiver;

    public BatteryViewModel(@NonNull Application application) {
        super(application);
        registerBroadcastReceiver();
        init();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        unregisterBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        if (broadcastReceiver != null) {
            return;
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);

        broadcastReceiver = new BroadcastReceiver() {
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
            }
        };

        ContextCompat.registerReceiver(getApplication(), broadcastReceiver, intentFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void unregisterBroadcastReceiver() {
        if (broadcastReceiver != null) {
            getApplication().unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
    }

    private void init() {
        int level = BatteryUtils.getBatteryLevel(getApplication());
        boolean isCharging = BatteryUtils.isCharging(getApplication());
        mLevel.postValue(level);
        mIsCharging.postValue(isCharging);
    }

    public LiveData<Integer> getLevel() {
        return mLevel;
    }

    public LiveData<Boolean> getIsCharging() {
        return mIsCharging;
    }
}