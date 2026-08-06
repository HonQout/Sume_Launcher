package com.qch.sumelauncher.utils;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class IntentUtils {
    private static final String TAG = "IntentUtils";

    public enum LaunchIntentResult {
        SUCCESS,
        URI_IS_EMPTY,
        NO_MATCHING_ACTIVITY,
        REQUIRE_EXTRA_PERMISSION
    }

    public enum LaunchActivityResult {
        SUCCESS,
        NOT_FOUND,
        NOT_EXPORTED,
        REQUIRE_EXTRA_PERMISSION
    }

    public interface OnRequestUninstallApp {
        void uninstallThisApp();

        void uninstallSystemApp(String packageName);

        void uninstallUpdatedSystemApp(String packageName);

        void uninstallUserApp(String packageName);

        void onError(String packageName);
    }

    public static LaunchActivityResult launchActivity(@NonNull Context context,
                                                      @NonNull ActivityInfo activityInfo,
                                                      boolean newTask) {
        if (!activityInfo.exported) {
            Log.e(TAG, "Cannot launch activity. Requested activity is not exported.");
            return LaunchActivityResult.NOT_EXPORTED;
        } else if (!TextUtils.isEmpty(activityInfo.permission)) {
            Log.e(TAG, "Cannot launch activity. Requested activity requires extra permission"
                    + activityInfo.permission + " to start.");
            return LaunchActivityResult.REQUIRE_EXTRA_PERMISSION;
        } else {
            String packageName = activityInfo.packageName;
            String activityName = activityInfo.name;
            Intent intent = new Intent();
            intent.setClassName(packageName, activityName);
            if (newTask) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            try {
                context.startActivity(intent, null);
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
        return activityInfo == null ? LaunchActivityResult.NOT_FOUND :
                launchActivity(context, activityInfo, newTask);
    }

    public static LaunchIntentResult openAppDetailsPage(Context context, String packageName) {
        if (packageName == null || TextUtils.isEmpty(packageName)) {
            Log.e(TAG, "Cannot launch activity because the given packageName is null or empty.");
            return LaunchIntentResult.URI_IS_EMPTY;
        }
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", packageName, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
            return LaunchIntentResult.SUCCESS;
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Cannot launch Settings because requested package cannot be found.", e);
            return LaunchIntentResult.NO_MATCHING_ACTIVITY;
        }
    }

    public static LaunchIntentResult uninstallApp(@NonNull Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            Log.e(TAG, "Cannot uninstall app. The given packageName is empty.");
            return LaunchIntentResult.URI_IS_EMPTY;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                && PermissionUtils.isPermissionGranted(context, Manifest.permission.REQUEST_DELETE_PACKAGES)) {
            Log.e(TAG, "Failed to uninstall app. This operation requires permission "
                    + Manifest.permission.REQUEST_DELETE_PACKAGES + " since Android 9.0.");
            return LaunchIntentResult.REQUIRE_EXTRA_PERMISSION;
        }
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.fromParts("package", packageName, null));
        try {
            context.startActivity(intent);
            return LaunchIntentResult.SUCCESS;
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Failed to uninstall app. Requested activity cannot be found.", e);
            return LaunchIntentResult.NO_MATCHING_ACTIVITY;
        }
    }

    public static void requestUninstallApp(@NonNull Context context, @Nullable String packageName,
                                           OnRequestUninstallApp callback) {
        if (TextUtils.isEmpty(packageName)) {
            return;
        }

        ApplicationUtils.ApplicationType type = ApplicationUtils.getApplicationType(context, packageName);
        if (Objects.equals(packageName, context.getPackageName())) {
            callback.uninstallThisApp();
        } else {
            switch (type) {
                case SYSTEM: {
                    callback.uninstallSystemApp(packageName);
                    break;
                }
                case UPDATED_SYSTEM: {
                    callback.uninstallUpdatedSystemApp(packageName);
                    break;
                }
                case USER: {
                    callback.uninstallUserApp(packageName);
                    break;
                }
                default: {
                    callback.onError(packageName);
                    break;
                }
            }
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