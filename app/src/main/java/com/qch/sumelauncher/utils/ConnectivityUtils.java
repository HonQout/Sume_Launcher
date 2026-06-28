package com.qch.sumelauncher.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ConnectivityUtils {
    private static final String TAG = "ConnectivityUtils";

    @SuppressLint("ObsoleteSdkInt")
    @Nullable
    public static ConnectivityManager getConnectivityManager(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(ConnectivityManager.class);
        } else {
            return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
    }

    public static boolean isAirplaneModeEnabled(@NonNull Context context) {
        return Settings.Global.getInt(
                context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON,
                0
        ) == 1;
    }
}