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
    public static PackageInfo getPackageInfo(@NonNull Context context, String packageName) {
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
    public static ApplicationInfo getApplicationInfo(@NonNull Context context, String packageName) {
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
    public static Drawable getApplicationIcon(@NonNull Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        if (packageName == null || TextUtils.isEmpty(packageName)) {
            Log.e(TAG, "Cannot get application icon because the given packageName is null or empty.");
            return pm.getDefaultActivityIcon();
        } else {
            try {
                return pm.getApplicationIcon(packageName);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Cannot get application icon because package " + packageName
                        + " doesn't exist.", e);
                return pm.getDefaultActivityIcon();
            }
        }
    }

    @DrawableRes
    public static int getApplicationIconId(@NonNull Context context, String packageName) {
        ApplicationInfo applicationInfo = getApplicationInfo(context, packageName);
        if (applicationInfo != null) {
            return applicationInfo.icon;
        }
        return 0;
    }

    @Nullable
    public static String getApplicationLabel(@NonNull Context context, String packageName) {
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

    public static ApplicationType getApplicationType(@NonNull Context context, String packageName) {
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

    public static List<ShortcutInfo> getShortcuts(@NonNull Context context, String packageName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            LauncherApps launcherApps =
                    (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
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
                Log.e(TAG, "Cannot get shortcuts of package " + packageName + ".", e);
            }
        }
        return Collections.emptyList();
    }

    public static boolean launchAppShortcut(@NonNull Context context, String packageName,
                                            String shortcutId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            LauncherApps launcherApps =
                    (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
            try {
                if (launcherApps != null && launcherApps.hasShortcutHostPermission()) {
                    launcherApps.startShortcut(packageName, shortcutId, null,
                            null, Process.myUserHandle());
                    return true;
                }
            } catch (Exception e) {
                Log.e(TAG, "Cannot launch shortcut " + shortcutId + " of package "
                        + packageName + ".", e);
            }
        }
        return false;
    }

    @Nullable
    public static ActivityInfo getActivityInfo(@NonNull Context context, String packageName,
                                               String activityName) {
        PackageManager pm = context.getPackageManager();
        if (packageName != null && activityName != null) {
            try {
                return pm.getActivityInfo(new ComponentName(packageName, activityName), 0);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Cannot get activityInfo because activity " + activityName
                        + " of package " + packageName + " doesn't exist.", e);
            }
        }
        return null;
    }

    @NonNull
    public static Drawable getActivityIcon(@NonNull Context context, String packageName,
                                           String activityName) {
        PackageManager pm = context.getPackageManager();
        if (packageName != null && activityName != null) {
            try {
                return pm.getActivityIcon(new ComponentName(packageName, activityName));
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Cannot get activity icon because activity " + activityName
                        + " of package " + packageName + " doesn't exist.", e);
            }
        }
        return pm.getDefaultActivityIcon();
    }

    @NonNull
    public static Drawable getActivityIcon(@NonNull Context context, ActivityInfo activityInfo) {
        if (activityInfo != null) {
            String packageName = activityInfo.packageName;
            String activityName = activityInfo.name;
            if (packageName != null && activityName != null) {
                return getActivityIcon(context, packageName, activityName);
            }
        }
        return context.getPackageManager().getDefaultActivityIcon();
    }

    @DrawableRes
    public static int getActivityIconId(@NonNull Context context, String packageName,
                                        String activityName) {
        if (packageName != null && activityName != null) {
            ActivityInfo activityInfo = getActivityInfo(context, packageName, activityName);
            return getActivityIconId(activityInfo);
        }
        return 0;
    }

    @DrawableRes
    public static int getActivityIconId(ActivityInfo activityInfo) {
        if (activityInfo != null) {
            return activityInfo.icon;
        }
        return 0;
    }

    @Nullable
    public static String getActivityName(ResolveInfo resolveInfo) {
        return resolveInfo == null ? null : resolveInfo.activityInfo.name;
    }

    @Nullable
    public static String getActivityLabel(@NonNull Context context,
                                          @Nullable ActivityInfo activityInfo) {
        PackageManager pm = context.getPackageManager();
        return activityInfo == null ? null : activityInfo.loadLabel(pm).toString();
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
     * @param packageName Specify which package should these activities belong to. Passing null or
     *                    empty string ("") to get all activities of all installed packages.
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
    public static ResolveInfo getIntentActivity(Context context,
                                                String packageName,
                                                String activityName) {
        List<ResolveInfo> intentActivities = getIntentActivityList(context, packageName);
        for (ResolveInfo resolveInfo : intentActivities) {
            if (Objects.equals(getActivityName(resolveInfo), activityName)) {
                return resolveInfo;
            }
        }
        return null;
    }

    private static List<ActivityBean> getActivityBeanList(Context context,
                                                          List<ResolveInfo> intentActivityList) {
        List<ActivityBean> list = new ArrayList<>();
        for (ResolveInfo resolveInfo : intentActivityList) {
            list.add(new ActivityBean(context, resolveInfo));
        }
        return list;
    }

    /**
     * Get a list of ActivityBean of all launchable activity of certain application(s).
     *
     * @param packageName Specify which package should these ActivityBeans belong to. Passing null
     *                    or empty string ("") to get all ActivityBeans of all installed packages.
     */
    public static List<ActivityBean> getActivityBeanList(@NonNull Context context,
                                                         @Nullable String packageName) {
        List<ResolveInfo> list = getIntentActivityList(context, packageName);
        return getActivityBeanList(context, list);
    }
}