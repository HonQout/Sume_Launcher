package com.qch.sumelauncher.topbar.viewmodel;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

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
    private final MutableLiveData<RingerModeIconState> mRingerModeIconState = new MutableLiveData<>();
    private boolean isIconVisible = true;
    // broadcast receiver
    private BroadcastReceiver broadcastReceiver = null;

    public enum RingerModeIconState {
        HIDDEN,
        SILENT,
        VIBRATE
    }

    public RingerModeViewModel(@NonNull Application application) {
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
        RingerMode mode = AudioUtils.getRingerMode(getApplication());
        mRingerMode.postValue(mode);
        setRingerModeIconStateInner(mode);
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
                int newRingerMode = intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, 2);
                RingerMode mode = RingerMode.Normal;
                switch (newRingerMode) {
                    case AudioManager.RINGER_MODE_SILENT: {
                        mode = RingerMode.Silent;
                        break;
                    }

                    case AudioManager.RINGER_MODE_VIBRATE: {
                        mode = RingerMode.Vibrate;
                        break;
                    }
                }
                mRingerMode.postValue(mode);
                setRingerModeIconStateInner(mode);
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

    private void setRingerModeIconStateInner(RingerMode mode) {
        if (isIconVisible) {
            switch (mode) {
                case Silent: {
                    mRingerModeIconState.postValue(RingerModeIconState.SILENT);
                    break;
                }
                case Vibrate: {
                    mRingerModeIconState.postValue(RingerModeIconState.VIBRATE);
                    break;
                }
                case Normal: {
                    mRingerModeIconState.postValue(RingerModeIconState.HIDDEN);
                    break;
                }
            }
        }
    }

    public void setRingerModeIconState(RingerModeIconState state) {
        mRingerModeIconState.postValue(state);
    }

    public void restoreRingerModeIconState() {
        RingerMode value = mRingerMode.getValue();
        if (value == null) {
            mRingerModeIconState.postValue(RingerModeIconState.HIDDEN);
            return;
        }
        switch (value) {
            case Silent: {
                mRingerModeIconState.postValue(RingerModeIconState.SILENT);
                break;
            }
            case Vibrate: {
                mRingerModeIconState.postValue(RingerModeIconState.VIBRATE);
                break;
            }
            case Normal: {
                mRingerModeIconState.postValue(RingerModeIconState.HIDDEN);
                break;
            }
        }
    }

    public LiveData<RingerModeIconState> getRingerModeIconState() {
        return mRingerModeIconState;
    }

    public void setIconVisible(boolean isIconVisible) {
        this.isIconVisible = isIconVisible;
    }
}
