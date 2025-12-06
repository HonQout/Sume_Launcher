package com.qch.sumelauncher.bean;

import android.content.Context;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.qch.sumelauncher.utils.ApplicationUtils;

import java.io.Serializable;
import java.util.List;

public class ActivityBean implements Serializable {
    private final ResolveInfo resolveInfo;
    private final String packageName;
    private final String activityName;
    private final String label;
    @DrawableRes
    private final int iconRes;
    private final List<ShortcutInfo> shortcutInfoList;

    public ActivityBean(Context context, @NonNull ResolveInfo resolveInfo) {
        this.resolveInfo = resolveInfo;
        packageName = ApplicationUtils.getPackageName(resolveInfo);
        activityName = ApplicationUtils.getActivityName(resolveInfo);
        label = ApplicationUtils.getActivityLabel(context, resolveInfo);
        iconRes = ApplicationUtils.getApplicationIconId(context, resolveInfo);
        shortcutInfoList = ApplicationUtils.getShortcuts(context, packageName);
    }

    public ResolveInfo getResolveInfo() {
        return resolveInfo;
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