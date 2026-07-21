package com.qch.sumelauncher.topbar.viewmodel;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.qch.sumelauncher.utils.BluetoothUtils;

public class BluetoothViewModel extends AndroidViewModel {
    private static final String TAG = "BluetoothViewModel";
    // data
    private final MutableLiveData<Boolean> mBtState = new MutableLiveData<>();
    private final MutableLiveData<BtStateIconState> mBtStateIconState = new MutableLiveData<>();
    private boolean isIconVisible = true;
    // broadcast receiver
    private BroadcastReceiver broadcastReceiver = null;

    public enum BtStateIconState {
        HIDDEN,
        ENABLED,
        CONNECTED
    }

    public BluetoothViewModel(@NonNull Application application) {
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
        Context context = getApplication();
        boolean isEnabled = BluetoothUtils.isBluetoothEnabled(context);
        mBtState.postValue(isEnabled);
        setBtStateIconStateInner(isEnabled);
    }

    private void registerBroadcastReceiver() {
        if (broadcastReceiver != null) {
            return;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action == null) {
                    return;
                }
                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    int btState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.STATE_OFF);
                    boolean value = false;
                    if (btState == BluetoothAdapter.STATE_ON) {
                        value = true;
                    }
                    mBtState.postValue(value);
                    setBtStateIconStateInner(value);
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

    public LiveData<Boolean> getBtState() {
        return mBtState;
    }

    private void setBtStateIconStateInner(boolean value) {
        if (isIconVisible) {
            if (value) {
                mBtStateIconState.postValue(BtStateIconState.ENABLED);
            } else {
                mBtStateIconState.postValue(BtStateIconState.HIDDEN);
            }
        }
    }

    public void setBtStateIconState(BtStateIconState state) {
        mBtStateIconState.postValue(state);
    }

    public void restoreBtModeIconState() {
        Boolean value = mBtState.getValue();
        if (value == null) {
            mBtStateIconState.postValue(BtStateIconState.HIDDEN);
            return;
        }
        if (value) {
            mBtStateIconState.postValue(BtStateIconState.ENABLED);
        } else {
            mBtStateIconState.postValue(BtStateIconState.HIDDEN);
        }
    }

    public LiveData<BtStateIconState> getBtStateIconState() {
        return mBtStateIconState;
    }

    public void setIconVisible(boolean isIconVisible) {
        this.isIconVisible = isIconVisible;
    }
}