package com.qch.sumelauncher.topbar.viewmodel;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.qch.sumelauncher.utils.AudioUtils;
import com.qch.sumelauncher.utils.AudioUtils.RingerMode;

public class RingerModeViewModel extends AndroidViewModel {
    private static final String TAG = "RingerModeViewModel";
    // data
    private final MutableLiveData<RingerMode> mRingerMode = new MutableLiveData<>();
    // broadcast receiver
    private BroadcastReceiver broadcastReceiver = null;

    public RingerModeViewModel(@NonNull Application application) {
        super(application);
        update();
        registerBroadcastReceiver();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        unregisterBroadcastReceiver();
    }

    private void update() {
        mRingerMode.postValue(AudioUtils.getRingerMode(getApplication()));
    }

    private void registerBroadcastReceiver() {
        if (broadcastReceiver != null) {
            return;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Received intent.");
                int newRingerMode = intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1);
                switch (newRingerMode) {
                    case AudioManager.RINGER_MODE_SILENT: {
                        mRingerMode.postValue(RingerMode.Silent);
                        break;
                    }

                    case AudioManager.RINGER_MODE_VIBRATE: {
                        mRingerMode.postValue(RingerMode.Vibrate);
                        break;
                    }

                    case AudioManager.RINGER_MODE_NORMAL: {
                        mRingerMode.postValue(RingerMode.Normal);
                        break;
                    }
                }
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

    public LiveData<RingerMode> getRingerMode() {
        return mRingerMode;
    }
}
