package com.qch.sumelauncher.viewmodel;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.format.DateUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class TimeViewModel extends AndroidViewModel {
    private static final String TAG = "TimeViewModel";
    // data
    private final MutableLiveData<Long> mCurrentTime = new MutableLiveData<>();
    private final MutableLiveData<String> mCurrentTimeText = new MutableLiveData<>();
    private final MutableLiveData<String> mCurrentDateText = new MutableLiveData<>();
    // broadcast receiver
    private BroadcastReceiver broadcastReceiver;

    public TimeViewModel(@NonNull Application application) {
        super(application);
        update();
        registerBroadcastReceiver();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        unRegisterBroadcastReceiver();
    }

    private void update() {
        long currentTime = System.currentTimeMillis();
        mCurrentTime.postValue(currentTime);
        mCurrentTimeText.postValue(DateUtils.formatDateTime(
                getApplication(),
                currentTime,
                DateUtils.FORMAT_SHOW_TIME
        ));
        mCurrentDateText.postValue(DateUtils.formatDateTime(
                getApplication(),
                currentTime,
                DateUtils.FORMAT_NO_YEAR
                        | DateUtils.FORMAT_SHOW_DATE
                        | DateUtils.FORMAT_SHOW_WEEKDAY
                        | DateUtils.FORMAT_ABBREV_WEEKDAY
        ));
    }

    private void registerBroadcastReceiver() {
        if (broadcastReceiver != null) {
            return;
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        intentFilter.addAction(Intent.ACTION_DATE_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                update();
            }
        };

        ContextCompat.registerReceiver(getApplication(), broadcastReceiver, intentFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void unRegisterBroadcastReceiver() {
        if (broadcastReceiver != null) {
            getApplication().unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
    }

    public LiveData<Long> getCurrentTime() {
        return mCurrentTime;
    }

    public LiveData<String> getCurrentTimeText() {
        return mCurrentTimeText;
    }

    public LiveData<String> getCurrentDateText() {
        return mCurrentDateText;
    }
}