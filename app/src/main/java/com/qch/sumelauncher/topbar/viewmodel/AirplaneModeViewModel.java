package com.qch.sumelauncher.topbar.viewmodel;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.qch.sumelauncher.utils.ConnectivityUtils;

public class AirplaneModeViewModel extends AndroidViewModel {
    private static final String TAG = "AirplaneModeViewModel";
    // data
    private final MutableLiveData<Boolean> mAirplaneMode = new MutableLiveData<>();
    private final MutableLiveData<AirplaneModeIconState> mIconState = new MutableLiveData<>();
    private boolean isIconVisible = true;
    // broadcast receiver
    private BroadcastReceiver broadcastReceiver = null;

    public enum AirplaneModeIconState {
        HIDDEN,
        ON
    }

    public AirplaneModeViewModel(@NonNull Application application) {
        super(application);
        init();
        registerBroadcastReceiver();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        unregisterBroadcastReceiver();
    }

    private void init() {
        boolean value = ConnectivityUtils.isAirplaneModeEnabled(getApplication());
        mAirplaneMode.postValue(value);
        setIconStateInternal(value);
    }

    private void registerBroadcastReceiver() {
        if (broadcastReceiver != null) {
            return;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean state = intent.getBooleanExtra("state", false);
                mAirplaneMode.postValue(state);
                setIconStateInternal(state);
            }
        };

        ContextCompat.registerReceiver(getApplication(), broadcastReceiver, filter,
                ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void unregisterBroadcastReceiver() {
        if (broadcastReceiver != null) {
            getApplication().unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
    }

    public LiveData<Boolean> getAirplaneMode() {
        return mAirplaneMode;
    }

    private void setIconStateInternal(boolean value) {
        if (isIconVisible) {
            if (value) {
                mIconState.postValue(AirplaneModeIconState.ON);
            } else {
                mIconState.postValue(AirplaneModeIconState.HIDDEN);
            }
        }
    }

    public void setIconState(AirplaneModeIconState state) {
        mIconState.postValue(state);
    }

    public void restoreIconState() {
        Boolean value = mAirplaneMode.getValue();
        if (value == null) {
            mIconState.postValue(AirplaneModeIconState.HIDDEN);
            return;
        }
        if (value) {
            mIconState.postValue(AirplaneModeIconState.ON);
        } else {
            mIconState.postValue(AirplaneModeIconState.HIDDEN);
        }
    }

    public LiveData<AirplaneModeIconState> getIconState() {
        return mIconState;
    }

    public void setIconVisible(boolean isIconVisible) {
        this.isIconVisible = isIconVisible;
    }
}