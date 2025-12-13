package com.qch.sumelauncher.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.TransportInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

public class WifiUtils {
    private static final String TAG = "WifiUtils";

    public static boolean isWifiSupported(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI);
    }

    @Nullable
    public static WifiManager getWifiManager(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(WifiManager.class);
        } else {
            return (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        }
    }

    public static boolean isWifiEnabled(Context context) {
        ConnectivityManager connectivityManager = ConnectivityUtils.getConnectivityManager(context);
        if (connectivityManager != null) {
            Network network = connectivityManager.getActiveNetwork();
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        }
        return false;
    }

    public static int calcSignalLevel(Context context, WifiInfo wifiInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WifiManager wifiManager = getWifiManager(context);
            if (wifiManager != null) {
                try {
                    int signalLevel = wifiManager.calculateSignalLevel(wifiInfo.getRssi());
                    int maxSignalLevel = wifiManager.getMaxSignalLevel();
                    if (maxSignalLevel == 3) {
                        return signalLevel;
                    } else if (maxSignalLevel <= 0) {
                        throw new Exception("System implementation of calculating max signal level went wrong.");
                    } else {
                        return (int) ((float) signalLevel / maxSignalLevel * 3);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to get wifi signal level.", e);
                }
            } else {
                Log.e(TAG, "Failed to get wifi signal level. \nWifi manager is null.");
            }
        } else {
            int rssi = wifiInfo.getRssi();
            if (rssi <= -100) {
                return 0;
            } else if (rssi <= -77) {
                return 1;
            } else if (rssi <= -55) {
                return 2;
            } else {
                return 3;
            }
        }
        return -1;
    }

    public static int getSignalLevel(Context context, @Nullable NetworkCapabilities networkCapabilities) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (networkCapabilities != null) {
                TransportInfo transportInfo = networkCapabilities.getTransportInfo();
                if (transportInfo instanceof WifiInfo) {
                    WifiInfo wifiInfo = (WifiInfo) transportInfo;
                    return WifiUtils.calcSignalLevel(context, wifiInfo);
                }
            }
        } else {
            WifiManager wifiManager = getWifiManager(context);
            if (wifiManager != null && wifiManager.isWifiEnabled()) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo != null) {
                    return WifiUtils.calcSignalLevel(context, wifiInfo);
                }
            }
        }
        return -1;
    }
}