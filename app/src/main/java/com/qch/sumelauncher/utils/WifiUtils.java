package com.qch.sumelauncher.utils;

import android.Manifest;
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
import androidx.annotation.RequiresApi;

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

    @Nullable
    public static NetworkCapabilities getNetworkCapabilities(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ConnectivityManager connectivityManager = ConnectivityUtils.getConnectivityManager(context);
            if (connectivityManager != null) {
                Network network = connectivityManager.getActiveNetwork();
                return connectivityManager.getNetworkCapabilities(network);
            }
        }
        Log.i(TAG, "Failed to get network capabilities");
        return null;
    }

    public static boolean isWifiEnabled(Context context) {
        NetworkCapabilities networkCapabilities = getNetworkCapabilities(context);
        return networkCapabilities != null &&
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
    }

    public static int calcSignalLevel(Context context, WifiInfo wifiInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return calcSignalLevelApi30(context, wifiInfo);
        } else {
            return calcSignalLevelApiDef(wifiInfo);
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private static int calcSignalLevelApi30(Context context, WifiInfo wifiInfo) {
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
            Log.e(TAG, "Wifi manager is null.");
        }
        return -1;
    }

    private static int calcSignalLevelApiDef(WifiInfo wifiInfo) {
        int rssi = wifiInfo.getRssi();
        if (rssi <= -95) {
            return 0;
        } else if (rssi <= -75) {
            return 1;
        } else if (rssi <= -55) {
            return 2;
        } else {
            return 3;
        }
    }

    public static int getSignalLevel(Context context, @Nullable NetworkCapabilities networkCapabilities) {
        // Try using modern way on Android 11+ first
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && networkCapabilities != null) {
            TransportInfo transportInfo = networkCapabilities.getTransportInfo();
            if (transportInfo instanceof WifiInfo) {
                WifiInfo wifiInfo = (WifiInfo) transportInfo;
                return WifiUtils.calcSignalLevel(context, wifiInfo);
            }
        }
        // If requirements aren't met, try using traditional way then
        if (PermissionUtils.isPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
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