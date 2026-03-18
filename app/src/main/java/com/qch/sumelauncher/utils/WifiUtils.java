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
    public static final int MAX_SIGNAL_LEVEL = 2;
    public static final int UNKNOWN_SIGNAL_LEVEL = -2; // Wi-Fi is unsupported or disabled
    public static final int DEFAULT_SIGNAL_LEVEL = -1; // Wi-Fi is enabled but not connected

    public static boolean isWifiSupported(@NonNull Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI);
    }

    @Nullable
    public static WifiManager getWifiManager(@NonNull Context context) {
        if (!isWifiSupported(context)) {
            Log.e(TAG, "Wifi is not supported.");
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getSystemService(WifiManager.class);
        } else {
            return (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        }
    }

    public static boolean isWifiEnabled(@NonNull Context context) {
        WifiManager wifiManager = getWifiManager(context);
        return wifiManager != null && wifiManager.isWifiEnabled();
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

    public static boolean isWifiConnected(@NonNull Context context) {
        if (!isWifiSupported(context)) {
            Log.e(TAG, "Wifi is not supported.");
            return false;
        }
        NetworkCapabilities networkCapabilities = getNetworkCapabilities(context);
        return networkCapabilities != null &&
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
    }

    /**
     * Calculate the signal level of connected Wi-Fi. The max level is
     * {@link WifiUtils#MAX_SIGNAL_LEVEL}.
     *
     * @return The signal level of connected Wi-Fi, or {@link WifiUtils#UNKNOWN_SIGNAL_LEVEL} if
     * Wi-Fi is unsupported or disabled.
     */
    public static int calcSignalLevel(@NonNull Context context, WifiInfo wifiInfo) {
        if (!isWifiSupported(context)) {
            Log.e(TAG, "Wifi is not supported.");
            return UNKNOWN_SIGNAL_LEVEL;
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
            return UNKNOWN_SIGNAL_LEVEL;
        }
        try {
            int signalLevel = wifiManager.calculateSignalLevel(wifiInfo.getRssi());
            int maxSignalLevel = wifiManager.getMaxSignalLevel();
            if (maxSignalLevel == MAX_SIGNAL_LEVEL) {
                return signalLevel;
            } else if (maxSignalLevel <= 0) {
                throw new Exception("System implementation of calculating max signal level is wrong.");
            } else {
                return (int) ((float) signalLevel / maxSignalLevel * MAX_SIGNAL_LEVEL);
            }
        } catch (Exception e) {
            Log.e(TAG, "Cannot get wifi signal level.", e);
            return -1;
        }
    }

    private static int calcSignalLevelApiDef(WifiInfo wifiInfo) {
        int rssi = wifiInfo.getRssi();
        if (rssi <= -100) {
            return 0;
        } else if (rssi <= -77) {
            return 1;
        } else {
            return 2;
        }
    }

    /**
     * Get the signal level of the network specified by the given {@link NetworkCapabilities}.
     *
     * @return The signal level of the network, or {@link WifiUtils#UNKNOWN_SIGNAL_LEVEL} by default.
     */
    public static int getSignalLevel(@NonNull Context context,
                                     @Nullable NetworkCapabilities networkCapabilities) {
        if (!isWifiSupported(context)) {
            Log.e(TAG, "Wifi is not supported.");
            return UNKNOWN_SIGNAL_LEVEL;
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
            if (wifiManager == null) {
                Log.e(TAG, "WifiManager is null.");
                return UNKNOWN_SIGNAL_LEVEL;
            }
            if (wifiManager.isWifiEnabled()) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo != null) {
                    return calcSignalLevel(context, wifiInfo);
                }
            }
        }
        // There's no other way
        return UNKNOWN_SIGNAL_LEVEL;
    }
}