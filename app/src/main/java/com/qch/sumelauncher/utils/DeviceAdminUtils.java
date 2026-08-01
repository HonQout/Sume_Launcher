package com.qch.sumelauncher.utils;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

public class DeviceAdminUtils {
    private static final String TAG = "DeviceAdminUtils";

    public static DevicePolicyManager getDevicePolicyManager(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        return appContext.getSystemService(DevicePolicyManager.class);
    }

    public static boolean isAdminActive(@NonNull Context context, ComponentName componentName) {
        DevicePolicyManager manager = getDevicePolicyManager(context);
        return manager.isAdminActive(componentName);
    }

    public static boolean removeActiveAdmin(@NonNull Context context, ComponentName componentName) {
        DevicePolicyManager manager = getDevicePolicyManager(context);
        if (manager.isAdminActive(componentName)) {
            try {
                manager.removeActiveAdmin(componentName);
                return true;
            } catch (SecurityException e) {
                Log.e(TAG, "Failed to remove active admin.", e);
                return false;
            }
        }
        return false;
    }
}