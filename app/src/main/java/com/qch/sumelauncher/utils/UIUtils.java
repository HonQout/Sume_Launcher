package com.qch.sumelauncher.utils;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class UIUtils {
    public static void handleStatusBarVisibility(@NonNull Window window, boolean display) {
        WindowInsetsControllerCompat windowInsetsControllerCompat
                = WindowCompat.getInsetsController(window, window.getDecorView());
        if (display) {
            windowInsetsControllerCompat.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            );
            windowInsetsControllerCompat.show(WindowInsetsCompat.Type.statusBars());
        } else {
            windowInsetsControllerCompat.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
            windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.statusBars());
        }
        //window.getDecorView().setFitsSystemWindows(display);
    }

    public static void handleNavigationBarVisibility(@NonNull Window window, boolean display) {
        WindowInsetsControllerCompat windowInsetsControllerCompat
                = WindowCompat.getInsetsController(window, window.getDecorView());
        if (display) {
            windowInsetsControllerCompat.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            );
            windowInsetsControllerCompat.show(WindowInsetsCompat.Type.navigationBars());
        } else {
            windowInsetsControllerCompat.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
            windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.navigationBars());
        }
        window.getDecorView().setFitsSystemWindows(display);
    }

    public static int getStatusBarHeight(@NonNull Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }
}
