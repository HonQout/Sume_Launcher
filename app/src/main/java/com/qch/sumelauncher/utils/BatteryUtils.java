package com.qch.sumelauncher.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class BatteryUtils {
    private static final String TAG = "BatteryUtils";

    public enum PluggedType {
        UNKNOWN, AC, DOCK, USB, WIRELESS
    }

    public static int getBatteryLevel(Context context) {
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }

    public static Intent getBatteryChangedIntent(Context context) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        return context.registerReceiver(null, filter);
    }

    public static int getBatteryStatusExtra(Context context) {
        Intent batteryStatus = getBatteryChangedIntent(context);
        return batteryStatus == null ?
                -1 : batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
    }

    public static int getBatteryPluggedExtra(Context context) {
        Intent batteryStatus = getBatteryChangedIntent(context);
        return batteryStatus == null ?
                -1 : batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
    }

    public static boolean isCharging(Context context) {
        int batteryStatusExtra = getBatteryStatusExtra(context);
        return batteryStatusExtra == BatteryManager.BATTERY_STATUS_CHARGING
                || batteryStatusExtra == BatteryManager.BATTERY_STATUS_FULL;
    }

    public static PluggedType getChargingType(Context context) {
        int batteryPluggedExtra = getBatteryPluggedExtra(context);
        switch (batteryPluggedExtra) {
            case BatteryManager.BATTERY_PLUGGED_AC: {
                return PluggedType.AC;
            }
            case BatteryManager.BATTERY_PLUGGED_DOCK: {
                return PluggedType.DOCK;
            }
            case BatteryManager.BATTERY_PLUGGED_USB: {
                return PluggedType.USB;
            }
            case BatteryManager.BATTERY_PLUGGED_WIRELESS: {
                return PluggedType.WIRELESS;
            }
            default: {
                return PluggedType.UNKNOWN;
            }
        }
    }
}