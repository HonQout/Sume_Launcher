package com.qch.sumelauncher.data.model.controlcenter;

import androidx.annotation.NonNull;

public class SettingsShortcutModel extends ControlCenterItemModel {
    private String settingsAction;

    public SettingsShortcutModel(@NonNull String tag, int titleRes, int iconRes, String settingsAction) {
        super(tag, titleRes, iconRes);
        this.settingsAction = settingsAction;
    }

    public void setSettingsAction(String settingsAction) {
        this.settingsAction = settingsAction;
    }

    public String getSettingsAction() {
        return settingsAction;
    }
}