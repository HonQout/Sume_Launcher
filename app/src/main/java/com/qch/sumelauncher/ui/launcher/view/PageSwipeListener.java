package com.qch.sumelauncher.ui.launcher.view;

import android.content.Context;
import android.view.GestureDetector;

import androidx.annotation.NonNull;

public class PageSwipeListener extends GestureDetector.SimpleOnGestureListener {
    private static final String TAG = "PageSwipeListener";
    private final GestureDetector detector;
    private final OnFlip onFlip;
    private final float minDistance;

    public interface OnFlip {
        void previous();

        void next();
    }

    public PageSwipeListener(@NonNull Context context, OnFlip onFlip) {
        this.onFlip = onFlip;
        this.minDistance = context.getResources().getDisplayMetrics().widthPixels / 5f;
        this.detector = new GestureDetector(context, this);
    }
}
