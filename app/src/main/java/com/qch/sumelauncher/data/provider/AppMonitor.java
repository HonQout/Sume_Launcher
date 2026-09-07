package com.qch.sumelauncher.data.provider;

import android.content.Context;
import android.content.pm.LauncherApps;

import androidx.annotation.NonNull;

public class AppMonitor {
    private static final String TAG = "AppMonitor";
    private final LauncherApps launcherApps;
    private LauncherApps.Callback callback;

    public AppMonitor(@NonNull Context context) {
        launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
    }

    public AppMonitor setCallback(LauncherApps.Callback callback) {
        this.callback = callback;
        return this;
    }

    public boolean register() {
        if (launcherApps != null && callback != null) {
            launcherApps.registerCallback(callback);
            return true;
        }
        return false;
    }

    public boolean unregister() {
        if (launcherApps != null && callback != null) {
            launcherApps.unregisterCallback(callback);
            return true;
        }
        return false;
    }
}
