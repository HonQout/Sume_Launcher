package com.qch.sumelauncher.utils;

import android.content.Context;
import android.os.BatteryManager;

public class BatteryUtils {
    public static int getBatteryLevel(Context context) {
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }

    public static boolean getIsCharging(Context context) {
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        return batteryManager.isCharging();
    }
}