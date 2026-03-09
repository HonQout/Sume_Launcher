package com.qch.sumelauncher.bean;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.qch.sumelauncher.utils.ApplicationUtils;

import java.io.Serializable;
import java.util.List;

public class ActivityBean implements Serializable {
    private final String packageName;
    private final String activityName;
    private final String label;
    @DrawableRes
    private final int iconRes;
    private final List<ShortcutInfo> shortcutInfoList;

    public ActivityBean(Context context, @NonNull ResolveInfo resolveInfo) {
        this(context, resolveInfo.activityInfo);
    }

    public ActivityBean(Context context, @NonNull ActivityInfo activityInfo) {
        packageName = activityInfo.packageName;
        activityName = activityInfo.name;
        label = ApplicationUtils.getActivityLabel(context, activityInfo);
        iconRes = ApplicationUtils.getActivityIconId(activityInfo);
        shortcutInfoList = ApplicationUtils.getShortcuts(context, packageName);
    }

    public String getPackageName() {
        return packageName;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getKey() {
        return packageName + ":" + activityName;
    }

    public String getLabel() {
        return label;
    }

    @DrawableRes
    public int getIconRes() {
        return iconRes;
    }

    public List<ShortcutInfo> getShortcutInfoList() {
        return shortcutInfoList;
    }
}