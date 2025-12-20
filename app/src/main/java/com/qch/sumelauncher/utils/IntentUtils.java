package com.qch.sumelauncher.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.qch.sumelauncher.R;

public class IntentUtils {
    private static final String TAG = "IntentUtils";

    public enum LaunchActivityResult {
        SUCCESS, NOT_EXPORTED, REQUIRE_PERMISSION, NOT_FOUND
    }

    public enum LaunchIntentResult {
        SUCCESS, URI_IS_EMPTY, NO_MATCHING_ACTIVITY
    }

    public static LaunchActivityResult launchActivity(@NonNull Context context,
                                                      @NonNull ActivityInfo activityInfo,
                                                      boolean newTask) {
        if (!activityInfo.exported) {
            Log.e(TAG, "Cannot launch activity. Requested activity is not exported.");
            return LaunchActivityResult.NOT_EXPORTED;
        } else if (!TextUtils.isEmpty(activityInfo.permission)) {
            Log.e(TAG, "Cannot launch activity. Requested activity requires extra permission" +
                    " to start.\nPermission: " + activityInfo.permission);
            return LaunchActivityResult.REQUIRE_PERMISSION;
        } else {
            String packageName = activityInfo.packageName;
            String activityName = activityInfo.name;
            Intent intent = new Intent();
            intent.setClassName(packageName, activityName);
            if (newTask) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            try {
                context.startActivity(intent);
                return LaunchActivityResult.SUCCESS;
            } catch (ActivityNotFoundException e) {
                Log.e(TAG, "Cannot find requested activity.", e);
                return LaunchActivityResult.NOT_FOUND;
            }
        }
    }

    public static LaunchActivityResult launchActivity(@NonNull Context context,
                                                      @NonNull String packageName,
                                                      @NonNull String activityName,
                                                      boolean newTask) {
        ActivityInfo activityInfo = ApplicationUtils.getActivityInfo(context, packageName, activityName);
        return activityInfo == null ? LaunchActivityResult.NOT_FOUND : launchActivity(context, activityInfo, newTask);
    }

    public static void handleLaunchIntentResult(Context context, LaunchIntentResult result) {
        if (result == IntentUtils.LaunchIntentResult.URI_IS_EMPTY) {
            Toast.makeText(context, R.string.uri_is_empty,
                    Toast.LENGTH_SHORT).show();
        } else if (result == IntentUtils.LaunchIntentResult.NO_MATCHING_ACTIVITY) {
            Toast.makeText(context, R.string.no_matching_activity,
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static LaunchIntentResult openAppDetailsPage(Context context, String packageName) {
        if (packageName == null || TextUtils.isEmpty(packageName)) {
            Log.e(TAG, "Failed to launch activity because the given packageName is null or empty.");
            return LaunchIntentResult.URI_IS_EMPTY;
        }
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", packageName, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            return LaunchIntentResult.SUCCESS;
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Failed to launch Settings. Cannot find requested package.", e);
            return LaunchIntentResult.NO_MATCHING_ACTIVITY;
        }
    }

    public static LaunchIntentResult requireUninstallApp(@NonNull Context context, String packageName) {
        if (packageName == null || TextUtils.isEmpty(packageName)) {
            Log.e(TAG, "Failed to uninstall app because the given packageName is empty.");
            return LaunchIntentResult.URI_IS_EMPTY;
        }
        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
        intent.setData(Uri.fromParts("package", packageName, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            return LaunchIntentResult.SUCCESS;
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Failed to uninstall app because no activity can open this uri", e);
            return LaunchIntentResult.NO_MATCHING_ACTIVITY;
        }
    }

    public static LaunchIntentResult openAppInMarket(Context context, String packageName) {
        if (packageName == null || TextUtils.isEmpty(packageName)) {
            Log.e(TAG, "Failed to open detail page of this app in app market because the given packageName is null or empty.");
            return LaunchIntentResult.URI_IS_EMPTY;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + packageName));
        try {
            context.startActivity(intent);
            return LaunchIntentResult.SUCCESS;
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Failed to open detail page of this app in app market because no activity can open this uri.", e);
            return LaunchIntentResult.NO_MATCHING_ACTIVITY;
        }
    }

    public static LaunchIntentResult openNetAddress(Context context, String address, int flags) {
        if (address == null || TextUtils.isEmpty(address)) {
            Log.e(TAG, "Failed to open net address because the given address is null or empty.");
            return LaunchIntentResult.URI_IS_EMPTY;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(address));
        intent.setFlags(flags);
        try {
            context.startActivity(intent);
            return LaunchIntentResult.SUCCESS;
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Failed to open net address because no activity can open this uri.", e);
            return LaunchIntentResult.NO_MATCHING_ACTIVITY;
        }
    }
}