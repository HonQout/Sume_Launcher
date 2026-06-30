package com.qch.sumelauncher.bean;

import java.io.Serializable;

public class ActivityRecord implements Serializable {
    private String packageName;
    private String activityName;

    /**
     * No-Arg Constructor
     * Gson need this constructor. Do not delete it.
     */
    public ActivityRecord() {

    }

    public ActivityRecord(String packageName, String activityName) {
        this.packageName = packageName;
        this.activityName = activityName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getActivityName() {
        return activityName;
    }
}