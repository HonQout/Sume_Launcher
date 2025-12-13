package com.qch.sumelauncher.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;

import androidx.annotation.Nullable;

public class ConnectivityUtils {
    private static final String TAG = "ConnectivityUtils";

    @Nullable
    public static ConnectivityManager getConnectivityManager(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(ConnectivityManager.class);
        } else {
            return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
    }
}