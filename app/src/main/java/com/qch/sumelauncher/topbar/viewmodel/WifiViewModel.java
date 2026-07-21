package com.qch.sumelauncher.topbar.viewmodel;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.qch.sumelauncher.utils.ConnectivityUtils;
import com.qch.sumelauncher.utils.WifiUtils;

public class WifiViewModel extends AndroidViewModel {
    private static final String TAG = "WifiViewModel";
    // data
    private final MutableLiveData<Boolean> mWifiEnabled = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mWifiConnected = new MutableLiveData<>();
    private final MutableLiveData<Integer> mWifiSignalLevel = new MutableLiveData<>();
    private final MutableLiveData<WifiIconState> mWifiIconState = new MutableLiveData<>();
    private boolean isIconVisible = true;
    // broadcast receiver
    private BroadcastReceiver broadcastReceiver = null;
    // callback
    private ConnectivityManager.NetworkCallback wifiCallback;

    public enum WifiIconState {
        HIDDEN,
        NOT_CONNECTED,
        CONNECTED_1,
        CONNECTED_2,
        CONNECTED_3
    }

    public WifiViewModel(@NonNull Application application) {
        super(application);
        registerBroadcastReceiver();
        registerWifiCallback();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        unregisterBroadcastReceiver();
        unregisterWifiCallback();
    }

    public void update() {
        Context context = getApplication();
        boolean isWifiEnabled = WifiUtils.isWifiEnabled(context);
        Log.i(TAG, "Executed update(). Set mWifiEnabled to " + isWifiEnabled);
        this.mWifiEnabled.postValue(isWifiEnabled);
        int signalLevel = WifiUtils.UNKNOWN_SIGNAL_LEVEL;
        if (isWifiEnabled) {
            signalLevel = WifiUtils.getSignalLevel(context, WifiUtils.getNetworkCapabilities(context));
        }
        this.mWifiSignalLevel.postValue(signalLevel);
        if (isIconVisible) {
            this.mWifiIconState.postValue(getWifiIconStateInternal(signalLevel));
        }
    }

    private void registerBroadcastReceiver() {
        if (broadcastReceiver != null) {
            return;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN);
                if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                    Log.i(TAG, "Wifi is disabled.");
                    mWifiEnabled.postValue(false);
                } else if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                    Log.i(TAG, "Wifi is enabled.");
                    mWifiEnabled.postValue(true);
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

    private boolean registerWifiCallback() {
        Context context = getApplication();
        ConnectivityManager connectivityManager = ConnectivityUtils.getConnectivityManager(context);
        if (connectivityManager == null) {
            Log.i(TAG, "Connectivity manager is null.");
            return false;
        }
        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();
        wifiCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                Log.i(TAG, "Wifi is connected.");
                super.onAvailable(network);
                mWifiConnected.postValue(true);
            }

            @Override
            public void onLost(@NonNull Network network) {
                Log.i(TAG, "Wifi is lost.");
                super.onLost(network);
                mWifiConnected.postValue(false);
                if (isIconVisible) {
                    if (WifiUtils.isWifiEnabled(context)) {
                        mWifiIconState.postValue(WifiIconState.NOT_CONNECTED);
                    } else {
                        mWifiIconState.postValue(WifiIconState.HIDDEN);
                    }
                }
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network,
                                              @NonNull NetworkCapabilities networkCapabilities) {
                Log.i(TAG, "Wifi network capabilities have changed.");
                super.onCapabilitiesChanged(network, networkCapabilities);
                int signalLevel = WifiUtils.getSignalLevel(context, networkCapabilities);
                mWifiSignalLevel.postValue(signalLevel);
                if (isIconVisible) {
                    mWifiIconState.postValue(getWifiIconStateInternal(signalLevel));
                }
            }

            @Override
            public void onLinkPropertiesChanged(@NonNull Network network,
                                                @NonNull LinkProperties linkProperties) {
                Log.i(TAG, "Wifi link properties have changed.");
                super.onLinkPropertiesChanged(network, linkProperties);
            }
        };
        connectivityManager.registerNetworkCallback(request, wifiCallback);
        return true;
    }

    private boolean unregisterWifiCallback() {
        Context context = getApplication();
        if (wifiCallback == null) {
            Log.i(TAG, "WifiCallback is null.");
            return false;
        }
        ConnectivityManager connectivityManager = ConnectivityUtils.getConnectivityManager(context);
        if (connectivityManager == null) {
            Log.i(TAG, "Connectivity manager is null.");
            return false;
        }
        connectivityManager.unregisterNetworkCallback(wifiCallback);
        return true;
    }

    public LiveData<Boolean> getWifiEnabled() {
        return mWifiEnabled;
    }

    public LiveData<Integer> getWifiSignalLevel() {
        return mWifiSignalLevel;
    }

    public void setWifiIconState(WifiIconState state) {
        mWifiIconState.postValue(state);
    }

    public void restoreWifiIconState() {
        // If Wi-Fi is not enabled, hide the icon.
        if (mWifiEnabled.getValue() == null || !mWifiEnabled.getValue()) {
            mWifiIconState.postValue(WifiIconState.HIDDEN);
            return;
        }
        // Now, mWifiEnabled can only be Boolean(true). Therefore, Wi-Fi is enabled.
        // If Wi-Fi is connected, decide the signal level.
        if (mWifiConnected.getValue() != null && mWifiConnected.getValue()) {
            if (mWifiSignalLevel.getValue() != null) {
                int signalLevel = mWifiSignalLevel.getValue();
                mWifiIconState.postValue(getWifiIconStateInternal(signalLevel));
                return;
            }
        }
        // Although signal level runs into a problem, we have confirmed that Wi-Fi is enabled.
        mWifiIconState.postValue(WifiIconState.NOT_CONNECTED);
    }

    public LiveData<WifiIconState> getWifiIconState() {
        return mWifiIconState;
    }

    public void setIconVisible(boolean isIconVisible) {
        this.isIconVisible = isIconVisible;
    }


    private WifiIconState getWifiIconStateInternal(int signalLevel) {
        switch (signalLevel) {
            case 0:
                return WifiIconState.CONNECTED_1;
            case 1:
                return WifiIconState.CONNECTED_2;
            case 2:
                return WifiIconState.CONNECTED_3;
            default:
                return WifiIconState.NOT_CONNECTED;
        }
    }
}