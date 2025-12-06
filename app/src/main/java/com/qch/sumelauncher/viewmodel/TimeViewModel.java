package com.qch.sumelauncher.viewmodel;

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

public class TimeViewModel extends AndroidViewModel {
    // data
    private final MutableLiveData<Long> mCurrentTime = new MutableLiveData<>(System.currentTimeMillis());

    // broadcast receiver
    private BroadcastReceiver timeBroadcastReceiver;

    public TimeViewModel(@NonNull Application application) {
        super(application);
        registerTimeBR();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        unRegisterTimeBR();
    }

    private void registerTimeBR() {
        if (timeBroadcastReceiver != null) {
            return;
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        intentFilter.addAction(Intent.ACTION_DATE_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);

        timeBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCurrentTime.postValue(System.currentTimeMillis());
            }
        };

        ContextCompat.registerReceiver(getApplication(), timeBroadcastReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void unRegisterTimeBR() {
        if (timeBroadcastReceiver != null) {
            getApplication().unregisterReceiver(timeBroadcastReceiver);
            timeBroadcastReceiver = null;
        }
    }

    public LiveData<Long> getCurrentTime() {
        return mCurrentTime;
    }
}
