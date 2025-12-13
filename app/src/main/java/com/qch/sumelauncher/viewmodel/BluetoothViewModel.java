package com.qch.sumelauncher.viewmodel;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.utils.BluetoothUtils;

public class BluetoothViewModel extends AndroidViewModel {
    private static final String TAG = "BluetoothViewModel";

    // data
    private final MutableLiveData<Boolean> mBtEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> mBtConnected = new MutableLiveData<>(false);
    @DrawableRes
    private final MutableLiveData<Integer> mBtIcon
            = new MutableLiveData<>(R.drawable.baseline_bluetooth_disabled_24);

    // broadcast receiver
    private BroadcastReceiver btBroadcastReceiver = null;

    public BluetoothViewModel(@NonNull Application application) {
        super(application);
        registerBtBR();
        init();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        unregisterBtBR();
    }

    private void registerBtBR() {
        if (btBroadcastReceiver != null) {
            return;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);

        btBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action == null) {
                    return;
                }
                switch (action) {
                    case BluetoothAdapter.ACTION_STATE_CHANGED: {
                        int btState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                        if (btState == BluetoothAdapter.STATE_OFF) {
                            mBtEnabled.postValue(false);
                        } else if (btState == BluetoothAdapter.STATE_ON) {
                            mBtEnabled.postValue(true);
                        }
                        break;
                    }
                    case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED: {
                        int btConnState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.STATE_DISCONNECTED);
                        if (btConnState == BluetoothAdapter.STATE_DISCONNECTED) {
                            mBtConnected.postValue(false);
                        } else if (btConnState == BluetoothAdapter.STATE_ON) {
                            mBtConnected.postValue(true);
                        }
                        break;
                    }
                }
            }
        };

        ContextCompat.registerReceiver(getApplication(), btBroadcastReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void unregisterBtBR() {
        if (btBroadcastReceiver != null) {
            getApplication().unregisterReceiver(btBroadcastReceiver);
            btBroadcastReceiver = null;
        }
    }

    private void init() {
        this.mBtEnabled.postValue(BluetoothUtils.isBluetoothEnabled(getApplication()));
    }

    public LiveData<Boolean> getBtEnabled() {
        return mBtEnabled;
    }

    public MutableLiveData<Boolean> getBtConnected() {
        return mBtConnected;
    }
}