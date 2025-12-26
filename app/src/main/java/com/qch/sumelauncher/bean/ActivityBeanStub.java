package com.qch.sumelauncher.bean;

import android.content.pm.ResolveInfo;

import androidx.annotation.NonNull;

import com.qch.sumelauncher.utils.ApplicationUtils;

import java.io.Serializable;

public class ActivityBeanStub implements Serializable {
    private final String packageName;
    private final String activityName;

    public ActivityBeanStub(String packageName, String activityName) {
        this.packageName = packageName;
        this.activityName = activityName;
    }

    public ActivityBeanStub(@NonNull ResolveInfo resolveInfo) {
        this.packageName = ApplicationUtils.getPackageName(resolveInfo);
        this.activityName = ApplicationUtils.getActivityName(resolveInfo);
    }

    public String getPackageName() {
        return packageName;
    }

    public String getActivityName() {
        return activityName;
    }
}