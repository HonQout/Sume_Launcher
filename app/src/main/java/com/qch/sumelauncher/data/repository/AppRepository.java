package com.qch.sumelauncher.data.repository;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qch.sumelauncher.data.model.launcher.ActivityModel;
import com.qch.sumelauncher.utils.ApplicationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppRepository {
    private static final String TAG = "AppRepository";
    private final LauncherApps launcherApps;
    private final LauncherApps.Callback callback;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public AppRepository(@NonNull Context context, LauncherApps.Callback callback) {
        launcherApps = (LauncherApps) context
                .getApplicationContext()
                .getSystemService(Context.LAUNCHER_APPS_SERVICE);
        this.callback = callback;
    }

    public boolean register() {
        if (launcherApps != null && callback != null) {
            launcherApps.registerCallback(callback, mainHandler);
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

    /**
     * Get a list of activity models of all launchable activities.
     * <p>This method should be called on a background thread.
     */
    @NonNull
    public List<ActivityModel> getLaunchableActivityModels(@NonNull Context context,
                                                           @Nullable String pkgName) {
        List<ActivityModel> resultList = new ArrayList<>();
        UserHandle userHandle = Process.myUserHandle();
        List<LauncherActivityInfo> list = launcherApps.getActivityList(pkgName, userHandle);
        for (LauncherActivityInfo info : list) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityInfo activityInfo = info.getActivityInfo();
                String packageName = activityInfo.packageName;
                String activityName = activityInfo.name;
                String label = ApplicationUtils.getActivityLabel(context, activityInfo);
                int iconId = ApplicationUtils.getActivityIconId(activityInfo);
                List<ShortcutInfo> shortcutList = getShortcutList(packageName);
                resultList.add(new ActivityModel(
                        packageName,
                        activityName,
                        label,
                        iconId,
                        shortcutList
                ));
            } else {
                String packageName = info.getComponentName().getPackageName();
                String activityName = info.getComponentName().getClassName();
                ActivityInfo activityInfo = ApplicationUtils.getActivityInfo(context, packageName, activityName);
                String label = ApplicationUtils.getActivityLabel(context, activityInfo);
                int iconId = ApplicationUtils.getActivityIconId(activityInfo);
                List<ShortcutInfo> shortcutList = getShortcutList(packageName);
                resultList.add(new ActivityModel(
                        packageName,
                        activityName,
                        label,
                        iconId,
                        shortcutList
                ));
            }
        }
        return resultList;
    }

    /**
     * Get a list of shortcut of an application. This method takes effect on devices running
     * Android 7.1 (API Level 25) or above.
     */
    @NonNull
    public List<ShortcutInfo> getShortcutList(String packageName) {
        // List of shortcuts can be queried since Android 7.1
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            return Collections.emptyList();
        }
        // LauncherApps service should not be null and should have shortcut host permission
        if (launcherApps == null || !launcherApps.hasShortcutHostPermission()) {
            return Collections.emptyList();
        }
        // Arg packageName should not be null or empty
        if (TextUtils.isEmpty(packageName)) {
            return Collections.emptyList();
        }
        // Get list of shortcuts
        try {
            LauncherApps.ShortcutQuery query =
                    new LauncherApps.ShortcutQuery()
                            .setPackage(packageName)
                            .setQueryFlags(
                                    LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC
                                            | LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
                                            | LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
                            );
            List<ShortcutInfo> shortcutList = launcherApps.getShortcuts(query, Process.myUserHandle());
            return shortcutList == null ? Collections.emptyList() : shortcutList;
        } catch (Exception e) {
            Log.e(TAG, "Failed to get shortcuts of package " + packageName + ".", e);
            return Collections.emptyList();
        }
    }
}
