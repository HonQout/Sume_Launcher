package com.qch.sumelauncher.data.model.launcher;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ShortcutInfo;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.qch.sumelauncher.utils.ApplicationUtils;

import java.io.Serializable;
import java.util.List;

public class ActivityModel implements Serializable {
    private final String packageName;
    private final String activityName;
    private final String label;
    @DrawableRes
    private final int iconRes;
    private final List<ShortcutInfo> shortcutInfoList;

    public ActivityModel(Context context, String packageName, String activityName) {
        this.packageName = packageName;
        this.activityName = activityName;
        ActivityInfo activityInfo = ApplicationUtils.getActivityInfo(context, packageName, activityName);
        this.label = ApplicationUtils.getActivityLabel(context, activityInfo);
        this.iconRes = ApplicationUtils.getActivityIconId(activityInfo);
        this.shortcutInfoList = ApplicationUtils.getShortcuts(context, packageName);
    }

    public ActivityModel(Context context, @NonNull ActivityInfo activityInfo) {
        this.packageName = activityInfo.packageName;
        this.activityName = activityInfo.name;
        this.label = ApplicationUtils.getActivityLabel(context, activityInfo);
        this.iconRes = ApplicationUtils.getActivityIconId(activityInfo);
        this.shortcutInfoList = ApplicationUtils.getShortcuts(context, packageName);
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