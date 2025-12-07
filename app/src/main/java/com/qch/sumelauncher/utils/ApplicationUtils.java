package com.qch.sumelauncher.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qch.sumelauncher.bean.ActivityBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ApplicationUtils {
    private static final String TAG = "ApplicationUtils";

    public enum ApplicationType {
        UNKNOWN, SYSTEM, UPDATED_SYSTEM, USER
    }

    @Nullable
    public static String getPackageName(ResolveInfo resolveInfo) {
        return resolveInfo == null ? null : resolveInfo.activityInfo.packageName;
    }

    @Nullable
    public static PackageInfo getPackageInfo(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        if (packageName == null || TextUtils.isEmpty(packageName)) {
            Log.e(TAG, "Cannot get packageInfo because the given packageName is null or empty.");
            return null;
        } else {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    return pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0));
                } else {
                    return pm.getPackageInfo(packageName, 0);
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Cannot get packageInfo because package " + packageName + " doesn't exist.", e);
                return null;
            }
        }
    }

    @Nullable
    public static PackageInfo getPackageInfo(Context context, ResolveInfo resolveInfo) {
        String packageName = getPackageName(resolveInfo);
        return getPackageInfo(context, packageName);
    }

    @Nullable
    public static ApplicationInfo getApplicationInfo(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        if (packageName == null || TextUtils.isEmpty(packageName)) {
            Log.e(TAG, "Cannot get applicationInfo because the given packageName is null or empty.");
            return null;
        } else {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    return pm.getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(0));
                } else {
                    return pm.getApplicationInfo(packageName, 0);
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Cannot get packageInfo because package " + packageName + " doesn't exist.", e);
                return null;
            }
        }
    }

    @NonNull
    public static Drawable getApplicationIcon(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        if (packageName == null || TextUtils.isEmpty(packageName)) {
            Log.e(TAG, "Cannot get application icon because the given packageName is null or empty.");
            return pm.getDefaultActivityIcon();
        } else {
            try {
                return pm.getApplicationIcon(packageName);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Cannot get application icon because package " + packageName + " doesn't exist.", e);
                return pm.getDefaultActivityIcon();
            }
        }
    }

    @NonNull
    public static Drawable getApplicationIcon(Context context, ResolveInfo resolveInfo) {
        String packageName = getPackageName(resolveInfo);
        return getApplicationIcon(context, packageName);
    }

    @DrawableRes
    public static int getApplicationIconId(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);
            return applicationInfo.icon;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Cannot get applicationInfo because package " + packageName + " doesn't exist.", e);
            return 0;
        }
    }

    @DrawableRes
    public static int getApplicationIconId(Context context, ResolveInfo resolveInfo) {
        String packageName = getPackageName(resolveInfo);
        return getApplicationIconId(context, packageName);
    }

    @Nullable
    public static String getApplicationLabel(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = getPackageInfo(context, packageName);
        if (packageInfo != null) {
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            if (applicationInfo != null) {
                return applicationInfo.loadLabel(pm).toString();
            }
        }
        return null;
    }

    public static ApplicationType getApplicationType(Context context, String packageName) {
        ApplicationInfo applicationInfo = getApplicationInfo(context, packageName);
        if (applicationInfo != null) {
            int flags = applicationInfo.flags;
            if ((flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                return ApplicationType.UPDATED_SYSTEM;
            } else if ((flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                return ApplicationType.SYSTEM;
            } else {
                return ApplicationType.USER;
            }
        }
        return ApplicationType.UNKNOWN;
    }

    public static List<ShortcutInfo> getShortcuts(Context context, String packageName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            LauncherApps launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
            try {
                if (launcherApps != null && launcherApps.hasShortcutHostPermission()) {
                    LauncherApps.ShortcutQuery query = new LauncherApps.ShortcutQuery();
                    query.setPackage(packageName);
                    query.setQueryFlags(
                            LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC
                            | LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
                            | LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
                    );
                    return launcherApps.getShortcuts(query, Process.myUserHandle());
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to get shortcuts of package " + packageName + ".", e);
            }
        }
        return Collections.emptyList();
    }

    public static boolean launchAppShortcut(Context context, String packageName, String shortcutId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            LauncherApps launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
            try {
                if (launcherApps != null && launcherApps.hasShortcutHostPermission()) {
                    launcherApps.startShortcut(packageName, shortcutId, null, null, Process.myUserHandle());
                    return true;
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to launch shortcut " + shortcutId + " of package " + packageName + ".", e);
            }
        }
        return false;
    }

    @Nullable
    public static ActivityInfo getActivityInfo(Context context, String packageName, String activityName) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getActivityInfo(new ComponentName(packageName, activityName), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Cannot get activityInfo because activity " + activityName + " of package " + packageName + " doesn't exist.", e);
            return null;
        }
    }

    @NonNull
    public static Drawable getActivityIcon(Context context, ResolveInfo resolveInfo) {
        PackageManager pm = context.getPackageManager();
        if (resolveInfo == null) {
            return pm.getDefaultActivityIcon();
        } else {
            return resolveInfo.loadIcon(pm);
        }
    }

    @NonNull
    public static Drawable getActivityIcon(Context context, String packageName, String activityName) {
        PackageManager pm = context.getPackageManager();
        if (packageName != null && activityName != null) {
            try {
                return pm.getActivityIcon(new ComponentName(packageName, activityName));
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Failed to get activity icon because the specified activity doesn't exist.");
            }
        }
        return pm.getDefaultActivityIcon();
    }

    @Nullable
    public static String getActivityName(ResolveInfo resolveInfo) {
        return resolveInfo == null ? null : resolveInfo.activityInfo.name;
    }

    @Nullable
    public static String getActivityLabel(Context context, ResolveInfo resolveInfo) {
        PackageManager pm = context.getPackageManager();
        return resolveInfo == null ? null : resolveInfo.loadLabel(pm).toString();
    }

    public static List<ResolveInfo> getIntentActivityList(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        return pm.queryIntentActivities(intent, 0);
    }

    /**
     * Get a list of intent activities.
     *
     * @param packageName Specify which package should these activities belong to. Simply passing
     *                    null or empty string ("") to get all activities of all installed packages.
     */
    public static List<ResolveInfo> getIntentActivityList(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        if (packageName != null && !TextUtils.isEmpty(packageName)) {
            intent.setPackage(packageName);
        }
        return pm.queryIntentActivities(intent, 0);
    }

    @Nullable
    public static ResolveInfo getIntentActivity(Context context, String packageName, String activityName) {
        List<ResolveInfo> intentActivities = getIntentActivityList(context, packageName);
        for (ResolveInfo resolveInfo : intentActivities) {
            if (Objects.equals(getActivityName(resolveInfo), activityName)) {
                return resolveInfo;
            }
        }
        return null;
    }

    private static List<ActivityBean> getActivityBeanList(Context context, List<ResolveInfo> intentActivityList) {
        List<ActivityBean> list = new ArrayList<>();
        for (ResolveInfo resolveInfo : intentActivityList) {
            list.add(new ActivityBean(context, resolveInfo));
        }
        return list;
    }

    /**
     * Get a list of ActivityBean of all launchable activity of certain application(s).
     *
     * @param packageName Specify which package should these ActivityBeans belong to. Simply passing
     *                    null or empty string ("") to get all ActivityBeans of all installed packages.
     */
    public static List<ActivityBean> getActivityBeanList(Context context, String packageName) {
        List<ResolveInfo> list = getIntentActivityList(context, packageName);
        return getActivityBeanList(context, list);
    }
}