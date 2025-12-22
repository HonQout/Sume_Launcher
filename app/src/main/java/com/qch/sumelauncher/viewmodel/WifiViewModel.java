package com.qch.sumelauncher.viewmodel;

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

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.utils.ConnectivityUtils;
import com.qch.sumelauncher.utils.WifiUtils;

public class WifiViewModel extends AndroidViewModel {
    private static final String TAG = "WifiViewModel";
    // data
    private final MutableLiveData<Boolean> mWifiEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> mWifiConnected = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> mWifiSignalLevel = new MutableLiveData<>(-1);
    @DrawableRes
    private final MutableLiveData<Integer> mWifiIconRes
            = new MutableLiveData<>(R.drawable.baseline_signal_wifi_statusbar_null_24);
    // broadcast receiver
    private BroadcastReceiver wifiBroadcastReceiver = null;
    // callback
    private ConnectivityManager.NetworkCallback wifiCallback;

    public WifiViewModel(@NonNull Application application) {
        super(application);
        init();
        registerWifiBR();
        registerWifiCallback();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        unregisterWifiBR();
        unregisterWifiCallback();
    }

    private void registerWifiBR() {
        if (wifiBroadcastReceiver != null) {
            return;
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

        wifiBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                    mWifiEnabled.postValue(false);
                } else if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                    mWifiEnabled.postValue(true);
                }
            }
        };

        ContextCompat.registerReceiver(getApplication(), wifiBroadcastReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void unregisterWifiBR() {
        if (wifiBroadcastReceiver != null) {
            getApplication().unregisterReceiver(wifiBroadcastReceiver);
            wifiBroadcastReceiver = null;
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
                mWifiIconRes.postValue(getWifiIconResource(-1));
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network,
                                              @NonNull NetworkCapabilities networkCapabilities) {
                Log.i(TAG, "Wifi network capabilities have changed.");
                super.onCapabilitiesChanged(network, networkCapabilities);
                int signalLevel = WifiUtils.getSignalLevel(context, networkCapabilities);
                mWifiSignalLevel.postValue(signalLevel);
                mWifiIconRes.postValue(getWifiIconResource(signalLevel));
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

    public void init() {
        Context context = getApplication();
        boolean isWifiEnabled = WifiUtils.isWifiEnabled(context);
        this.mWifiEnabled.postValue(isWifiEnabled);
        if (isWifiEnabled) {
            int signalLevel = WifiUtils.getSignalLevel(context, WifiUtils.getNetworkCapabilities(context));
            this.mWifiSignalLevel.postValue(signalLevel);
            this.mWifiIconRes.postValue(getWifiIconResource(signalLevel));
        }
    }

    public LiveData<Boolean> getWifiEnabled() {
        return mWifiEnabled;
    }

    public LiveData<Boolean> getWifiConnected() {
        return mWifiConnected;
    }

    public LiveData<Integer> getWifiSignalLevel() {
        return mWifiSignalLevel;
    }

    public LiveData<Integer> getWifiIconRes() {
        return mWifiIconRes;
    }

    @DrawableRes
    public int getWifiIconResInt() {
        return mWifiIconRes.getValue() == null ? R.drawable.baseline_signal_wifi_statusbar_null_24 : mWifiIconRes.getValue();
    }

    @DrawableRes
    private int getWifiIconResource(int signalLevel) {
        switch (signalLevel) {
            case 1: {
                return R.drawable.baseline_wifi_1_bar_24;
            }
            case 2: {
                return R.drawable.baseline_wifi_2_bar_24;
            }
            case 3: {
                return R.drawable.baseline_wifi_24;
            }
            default: {
                return R.drawable.baseline_signal_wifi_statusbar_null_24;
            }
        }
    }
}