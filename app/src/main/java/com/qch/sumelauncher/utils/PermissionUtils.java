package com.qch.sumelauncher.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.DeprecatedSinceApi;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {
    private static final String TAG = "PermissionUtils";

    /**
     * Check if a permission is granted.
     */
    public static boolean isPermissionGranted(
            @NonNull Context context,
            @NonNull String permission
    ) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Check if a group of permissions is granted.
     */
    public static boolean arePermissionsGranted(
            @NonNull Context context,
            @NonNull String[] permissions
    ) {
        for (String permission : permissions) {
            if (!isPermissionGranted(context, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the denied permissions in a group of permissions.
     */
    @NonNull
    public static List<String> getDeniedPermissions(
            @NonNull Context context,
            @NonNull String[] permissions
    ) {
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
     * {@link Manifest.permission#READ_EXTERNAL_STORAGE} and
     * {@link Manifest.permission#WRITE_EXTERNAL_STORAGE} are granted on devices running Android 12
     * or below. On devices running Android 13 or above, these two permissions are deprecated.
     * Developers should check if the permission to access a certain kind of files (including audio,
     * image and video) is granted according to their requirements.
     */
    @DeprecatedSinceApi(api = Build.VERSION_CODES.TIRAMISU)
    public static boolean isStoragePermissionGranted(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.i(TAG, "Needless to check if permissions to access storage are granted.");
            return false;
        } else {
            return arePermissionsGranted(context,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE});
        }
    }

    /**
     * Request for storage permission. That is to say, request for
     * {@link Manifest.permission#READ_EXTERNAL_STORAGE} and
     * {@link Manifest.permission#WRITE_EXTERNAL_STORAGE} on devices running Android 12 or below.
     * On devices running Android 13 or above, these two permissions are deprecated. Developers
     * should request the permission to access a certain kind of files (including audio, image and
     * video) according to their requirements.
     */
    @DeprecatedSinceApi(api = Build.VERSION_CODES.TIRAMISU)
    public static void requestStoragePermission(@NonNull Activity activity, int REQUEST_CODE) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.i(TAG, "Needless to request permission to access storage. Request permission " +
                    "to read audio, image or video instead.");
        } else {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    /**
     * Check if permission to query all packages is granted. That is to say, check if
     * {@link Manifest.permission#QUERY_ALL_PACKAGES} is granted on devices running Android 11 or
     * above, or simply return true on devices running Android 10 or below, for the reason that
     * not until Android 11 did Google started to restrict access to package list.
     */
    public static boolean checkQueryAllPackagesPermission(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return isPermissionGranted(context, Manifest.permission.QUERY_ALL_PACKAGES);
        } else {
            Log.i(TAG, "Needless to check if permission to query all packages is granted.");
            return true;
        }
    }

    /**
     * Request for permission to query all packages. That is to say, request for
     * {@link Manifest.permission#QUERY_ALL_PACKAGES} on devices which run Android 11 or above, or
     * do nothing on devices which run Android 10 or below, for the reason that not until Android 11
     * did Google started to restrict access to package list.
     */
    public static void requestQueryAllPackagesPermission(@NonNull Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.QUERY_ALL_PACKAGES},
                    requestCode
            );
        } else {
            Log.i(TAG, "Needless to request permission to query all packages.");
        }
    }
}