package com.qch.sumelauncher.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.util.Log;

import com.qch.sumelauncher.bean.permission.PermissionSorted;

import java.util.Arrays;

public class PackageUtils {
    private static final String TAG = "PackageUtils";

    /**
     * Get sorted permissions (defined, requested and those don't exist in the system) of the package
     * specified by the given packageName.
     */
    public static PermissionSorted getPermissionSorted(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = null;
        PermissionSorted permissionSorted = new PermissionSorted();
        try {
            packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to get permission info list. Cannot find a package with the given packageName.", e);
        }
        if (packageInfo != null) {
            if (packageInfo.permissions != null) {
                permissionSorted.permissionDefinedList.addAll(Arrays.asList(packageInfo.permissions));
            }
            if (packageInfo.requestedPermissions != null) {
                for (String permission : packageInfo.requestedPermissions) {
                    PermissionInfo permissionInfo = null;
                    try {
                        permissionInfo = pm.getPermissionInfo(permission, 0);
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.e(TAG, "Failed to get permission info. Cannot find a permission with the given name.", e);
                    }
                    if (permissionInfo == null) {
                        permissionSorted.permissionNotFoundList.add(permission);
                    } else {
                        permissionSorted.permissionRequestedList.add(permissionInfo);
                    }
                }
            }
        }
        return permissionSorted;
    }
}