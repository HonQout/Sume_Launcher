package com.qch.sumelauncher.utils;

import android.Manifest;
import android.annotation.SuppressLint;
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

    public enum SignalLevel {
        /**
         * Wi-Fi is unsupported or disabled.
         */
        UNKNOWN(-2),
        /**
         * Wi-Fi is enabled but not connected.
         */
        DEFAULT(-1),
        LEVEL_0(0),
        LEVEL_1(1),
        LEVEL_2(2);

        private final int level;

        SignalLevel(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

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

    @SuppressLint("ObsoleteSdkInt")
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
        Log.e(TAG, "Cannot get network capabilities.");
        return null;
    }

    public static boolean isWifiConnected(@NonNull Context context) {
        NetworkCapabilities networkCapabilities = getNetworkCapabilities(context);
        return networkCapabilities != null &&
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
    }

    /**
     * Calculate the signal level of connected Wi-Fi. The max level is
     * {@link WifiUtils#MAX_SIGNAL_LEVEL}.
     *
     * @return The signal level of connected Wi-Fi, or {@link SignalLevel#UNKNOWN} if
     * Wi-Fi is unsupported or disabled.
     */
    public static SignalLevel calcSignalLevel(@NonNull Context context, WifiInfo wifiInfo) {
        if (!isWifiConnected(context)) {
            Log.e(TAG, "Wifi is not connected.");
            return SignalLevel.UNKNOWN;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return calcSignalLevelApi30(context, wifiInfo);
        } else {
            return calcSignalLevelApi1(wifiInfo);
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private static SignalLevel calcSignalLevelApi30(@NonNull Context context, WifiInfo wifiInfo) {
        WifiManager wifiManager = getWifiManager(context);
        if (wifiManager == null) {
            Log.e(TAG, "Wifi manager is null.");
            return SignalLevel.UNKNOWN;
        }
        try {
            int signalLevel = wifiManager.calculateSignalLevel(wifiInfo.getRssi());
            int maxSignalLevel = wifiManager.getMaxSignalLevel();
            if (maxSignalLevel == MAX_SIGNAL_LEVEL) {
                return toSignalLevel(signalLevel);
            } else if (maxSignalLevel <= 0) {
                throw new Exception("System implementation of calculating max signal level is wrong.");
            } else {
                return toSignalLevel((int) ((float) signalLevel / maxSignalLevel * MAX_SIGNAL_LEVEL));
            }
        } catch (Exception e) {
            Log.e(TAG, "Cannot get wifi signal level.", e);
            return SignalLevel.DEFAULT;
        }
    }

    private static SignalLevel toSignalLevel(int signalLevel) {
        if (signalLevel == SignalLevel.LEVEL_0.level) {
            return SignalLevel.LEVEL_0;
        } else if (signalLevel == SignalLevel.LEVEL_1.level) {
            return SignalLevel.LEVEL_1;
        } else if (signalLevel == SignalLevel.LEVEL_2.level) {
            return SignalLevel.LEVEL_2;
        } else {
            return SignalLevel.DEFAULT;
        }
    }

    private static SignalLevel calcSignalLevelApi1(WifiInfo wifiInfo) {
        if (wifiInfo == null) {
            return SignalLevel.DEFAULT;
        }
        int rssi = wifiInfo.getRssi();
        if (rssi <= -90) {
            return SignalLevel.LEVEL_0;
        } else if (rssi <= -73) {
            return SignalLevel.LEVEL_1;
        } else {
            return SignalLevel.LEVEL_2;
        }
    }

    /**
     * Get the signal level of the network specified by the given {@link NetworkCapabilities}.
     *
     * @return The signal level of the network, or {@link SignalLevel#UNKNOWN} by default.
     */
    public static SignalLevel getSignalLevel(@NonNull Context context,
                                             @Nullable NetworkCapabilities networkCapabilities) {
        if (!isWifiSupported(context)) {
            Log.e(TAG, "Wifi is not supported.");
            return SignalLevel.UNKNOWN;
        }
        // Try using modern way on Android 11+ first
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && networkCapabilities != null) {
            TransportInfo transportInfo = networkCapabilities.getTransportInfo();
            if (transportInfo instanceof WifiInfo) {
                WifiInfo wifiInfo = (WifiInfo) transportInfo;
                return calcSignalLevel(context, wifiInfo);
            }
        }
        // If requirements aren't met, try using traditional way then
        if (PermissionUtils.isPermissionGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            WifiManager wifiManager = getWifiManager(context);
            if (wifiManager == null) {
                Log.e(TAG, "WifiManager is null.");
                return SignalLevel.UNKNOWN;
            }
            if (wifiManager.isWifiEnabled()) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                if (wifiInfo != null) {
                    return calcSignalLevel(context, wifiInfo);
                }
            }
        } else {
            Log.e(TAG, "Failed to get signal level. Permission "
                    + Manifest.permission.ACCESS_FINE_LOCATION + " is not granted.");
        }
        // There's no other way
        return SignalLevel.UNKNOWN;
    }
}