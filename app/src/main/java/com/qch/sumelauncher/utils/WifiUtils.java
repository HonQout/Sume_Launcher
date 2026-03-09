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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class WifiUtils {
    private static final String TAG = "WifiUtils";

    public static boolean isWifiSupported(@NonNull Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI);
    }

    @Nullable
    public static WifiManager getWifiManager(@NonNull Context context) {
        if (!isWifiSupported(context)) {
            Log.e(TAG, "Wifi is not supported.");
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(WifiManager.class);
        } else {
            return (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        }
    }

    @Nullable
    public static NetworkCapabilities getNetworkCapabilities(@NonNull Context context) {
        if (!isWifiSupported(context)) {
            Log.e(TAG, "Wifi is not supported.");
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ConnectivityManager connectivityManager = ConnectivityUtils.getConnectivityManager(context);
            if (connectivityManager != null) {
                Network network = connectivityManager.getActiveNetwork();
                return connectivityManager.getNetworkCapabilities(network);
            }
        }
        Log.i(TAG, "Cannot get network capabilities");
        return null;
    }

    public static boolean isWifiEnabled(@NonNull Context context) {
        if (!isWifiSupported(context)) {
            Log.e(TAG, "Wifi is not supported.");
            return false;
        }
        NetworkCapabilities networkCapabilities = getNetworkCapabilities(context);
        return networkCapabilities != null &&
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
    }

    /**
     * Calculate the signal level of connected Wi-Fi. The max level is 3.
     *
     * @return The signal level of connected Wi-Fi, or -1 by default.
     */
    public static int calcSignalLevel(@NonNull Context context, WifiInfo wifiInfo) {
        if (!isWifiSupported(context)) {
            Log.e(TAG, "Wifi is not supported.");
            return -1;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return calcSignalLevelApi30(context, wifiInfo);
        } else {
            return calcSignalLevelApiDef(wifiInfo);
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private static int calcSignalLevelApi30(@NonNull Context context, WifiInfo wifiInfo) {
        WifiManager wifiManager = getWifiManager(context);
        if (wifiManager == null) {
            Log.e(TAG, "Wifi manager is null.");
            return -1;
        }
        try {
            int signalLevel = wifiManager.calculateSignalLevel(wifiInfo.getRssi());
            int maxSignalLevel = wifiManager.getMaxSignalLevel();
            if (maxSignalLevel == 3) {
                return signalLevel;
            } else if (maxSignalLevel <= 0) {
                throw new Exception("System implementation of calculating max signal level is wrong.");
            } else {
                return (int) ((float) signalLevel / maxSignalLevel * 3);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get wifi signal level.", e);
            return -1;
        }
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

    /**
     * Get the signal level of the network specified by the given {@link NetworkCapabilities}.
     *
     * @return The signal level of the network, or -1 by default.
     */
    public static int getSignalLevel(@NonNull Context context,
                                     @Nullable NetworkCapabilities networkCapabilities) {
        if (!isWifiSupported(context)) {
            Log.e(TAG, "Wifi is not supported.");
            return -1;
        }
        // Try using modern way on Android 11+ first
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && networkCapabilities != null) {
            TransportInfo transportInfo = networkCapabilities.getTransportInfo();
            if (transportInfo instanceof WifiInfo wifiInfo) {
                return calcSignalLevel(context, wifiInfo);
            }
        }
        // If requirements aren't met, try using traditional way then
        if (PermissionUtils.isPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            WifiManager wifiManager = getWifiManager(context);
            if (wifiManager != null && wifiManager.isWifiEnabled()) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo != null) {
                    return calcSignalLevel(context, wifiInfo);
                }
            }
        }
        return -1;
    }
}