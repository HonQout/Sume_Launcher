package com.qch.sumelauncher.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

public class BluetoothUtils {
    private static final String TAG = "BluetoothUtils";

    public static boolean isBluetoothSupported(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
    }

    public static boolean isBluetoothLESupported(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public static BluetoothManager getBluetoothManager(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(BluetoothManager.class);
        } else {
            return (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        }
    }

    public static BluetoothAdapter getBluetoothAdapter(Context context) {
        BluetoothManager bluetoothManager = getBluetoothManager(context);
        return bluetoothManager == null ? null : bluetoothManager.getAdapter();
    }

    public static boolean isBluetoothEnabled(Context context) {
        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter(context);
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }
}