package com.qch.sumelauncher.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {
    public static final String TAG = "PermissionUtils";

    /**
     * Check if a permission is granted.
     */
    public static boolean isPermissionGranted(@NonNull Context context, @NonNull String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check if a group of permissions are granted.
     */
    public static boolean arePermissionsGranted(@NonNull Context context, @NonNull String[] permissions) {
        for (String permission : permissions) {
            if (!isPermissionGranted(context, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the denied permissions from a group of permissions.
     */
    @NonNull
    public static List<String> getDeniedPermissions(@NonNull Context context, @NonNull String[] permissions) {
        List<String> permissionDenied = new ArrayList<>();
        for (String permission : permissions) {
            if (!isPermissionGranted(context, permission)) {
                permissionDenied.add(permission);
            }
        }
        return permissionDenied;
    }

    /**
     * Check if storage permission is granted. That is to say, check if
     * {@code Manifest.permission.READ_MEDIA_AUDIO} is granted on devices which run Android 13 or
     * above, or check if {@code Manifest.permission.READ_EXTERNAL_STORAGE} and
     * {@code MANIFEST.permission.WRITE_EXTERNAL_STORAGE} are granted on devices which run
     * Android 12 or below.
     */
    public static boolean isStoragePermissionGranted(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return isPermissionGranted(context, Manifest.permission.READ_MEDIA_AUDIO);
        } else {
            return arePermissionsGranted(context,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE});
        }
    }

    /**
     * Request for storage permission. That is to say, request for
     * {@code Manifest.permission.READ_MEDIA_AUDIO} on devices which run Android 13 or above, or
     * request for {@code Manifest.permission.READ_EXTERNAL_STORAGE} and
     * {@code MANIFEST.permission.WRITE_EXTERNAL_STORAGE} on devices which run Android 12 or below.
     */
    public static void requestStoragePermission(@NonNull Activity activity, int REQUEST_CODE) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_MEDIA_AUDIO}, REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    /**
     * Check if permission to query all packages is granted. That is to say, check if
     * {@code Manifest.permission.QUERY_ALL_PACKAGES} is granted on devices which run Android 11 or
     * above, or simply return true on devices which run Android 10 or below, for the reason that
     * not until Android 11 did Google started to restrict access to package list.
     */
    public static boolean checkQueryAllPackagesPermission(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return isPermissionGranted(context, Manifest.permission.QUERY_ALL_PACKAGES);
        } else {
            return true;
        }
    }

    /**
     * Request for permission to query all packages. That is to say, request for
     * {@code Manifest.permission.QUERY_ALL_PACKAGES} on devices which run Android 11 or above, or
     * do nothing on devices which run Android 10 or below, for the reason that not until Android 11
     * did Google started to restrict access to package list.
     */
    public static void requestQueryAllPackagesPermission(@NonNull Activity activity, int REQUEST_CODE) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.QUERY_ALL_PACKAGES}, REQUEST_CODE);
        }
    }
}