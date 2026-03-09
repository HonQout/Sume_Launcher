package com.qch.sumelauncher.viewmodel;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.qch.sumelauncher.utils.ConnectivityUtils;

public class AirplaneModeViewModel extends AndroidViewModel {
    private static final String TAG = "AirplaneModeViewModel";
    // data
    private final MutableLiveData<Boolean> mAirplaneModeEnabled = new MutableLiveData<>();
    // broadcast receiver
    private BroadcastReceiver amBroadcastReceiver = null;

    public AirplaneModeViewModel(@NonNull Application application) {
        super(application);
        update();
        registerAMBR();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        unregisterAMBR();
    }

    public void update() {
        mAirplaneModeEnabled.postValue(ConnectivityUtils.getAirplaneModeState(getApplication()));
    }

    private void registerAMBR() {
        if (amBroadcastReceiver != null) {
            return;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);

        amBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i(TAG, "Received intent.");
                boolean state = intent.getBooleanExtra("state", false);
                mAirplaneModeEnabled.postValue(state);
            }
        };

        ContextCompat.registerReceiver(getApplication(), amBroadcastReceiver, filter,
                ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void unregisterAMBR() {
        if (amBroadcastReceiver != null) {
            getApplication().unregisterReceiver(amBroadcastReceiver);
            amBroadcastReceiver = null;
        }
    }

    public LiveData<Boolean> getAirplaneModeEnabled() {
        return mAirplaneModeEnabled;
    }
}