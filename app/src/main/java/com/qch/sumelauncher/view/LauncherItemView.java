package com.qch.sumelauncher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qch.sumelauncher.launcher.LauncherItem;

public abstract class LauncherItemView extends FrameLayout {
    protected LauncherItem launcherItem;

    public LauncherItemView(@NonNull Context context) {
        this(context, null, 0, 0);
    }

    public LauncherItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public LauncherItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public LauncherItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        // update layout params
    }
}
