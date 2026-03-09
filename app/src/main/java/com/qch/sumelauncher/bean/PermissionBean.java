package com.qch.sumelauncher.bean;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;

public class PermissionBean {
    private final String name;
    private final String label;
    private final String description;

    public PermissionBean(Context context, PermissionInfo permissionInfo) {
        this.name = permissionInfo.name;
        PackageManager pm = context.getPackageManager();
        this.label = permissionInfo.loadLabel(pm).toString();
        CharSequence descriptionCS = permissionInfo.loadDescription(pm);
        this.description = descriptionCS == null ? "" : descriptionCS.toString();
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}