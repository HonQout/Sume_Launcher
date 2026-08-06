package com.qch.sumelauncher.data.model.controlcenter;

import androidx.annotation.NonNull;

public class ActivityShortcutModel extends ControlCenterItemModel {
    private Class<?> cls;

    public ActivityShortcutModel(@NonNull String tag, int titleRes, int iconRes, Class<?> cls) {
        super(tag, titleRes, iconRes);
        this.cls = cls;
    }

    public void setCls(Class<?> cls) {
        this.cls = cls;
    }

    public Class<?> getCls() {
        return cls;
    }
}