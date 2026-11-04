package com.qch.sumelauncher.data.model.controlcenter;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public class ControlCenterItemModel {
    private String tag;
    @StringRes
    private int titleRes;
    @DrawableRes
    private int iconRes;

    public ControlCenterItemModel(@NonNull String tag,
                                  @StringRes int titleRes,
                                  @DrawableRes int iconRes) {
        this.tag = tag;
        this.titleRes = titleRes;
        this.iconRes = iconRes;
    }

    public void setTag(@NonNull String tag) {
        this.tag = tag;
    }

    @NonNull
    public String getTag() {
        return tag;
    }

    public void setTitleRes(@StringRes int titleRes) {
        this.titleRes = titleRes;
    }

    @StringRes
    public int getTitleRes() {
        return titleRes;
    }

    public void setIconRes(@DrawableRes int iconRes) {
        this.iconRes = iconRes;
    }

    @DrawableRes
    public int getIconRes() {
        return iconRes;
    }
}