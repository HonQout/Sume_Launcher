package com.qch.sumelauncher.utils;

import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class UIUtils {
    public static void setViewFitsSystemWindows(@NonNull View view) {
        ViewCompat.setOnApplyWindowInsetsListener(view, ((v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            v.setPadding(v.getPaddingLeft(), statusBarHeight, v.getPaddingRight(), navigationBarHeight);
            return insets;
        }));
    }

    public static void handleStatusBarVisibility(@NonNull Window window, boolean visible) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            handleStatusBarVisibilityApi30(window, visible);
        } else {
            handleStatusBarVisibilityApi1(window, visible);
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private static void handleStatusBarVisibilityApi30(@NonNull Window window, boolean visible) {
        WindowInsetsControllerCompat windowInsetsControllerCompat
                = WindowCompat.getInsetsController(window, window.getDecorView());
        if (visible) {
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
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    private static void handleStatusBarVisibilityApi16(@NonNull Window window, boolean visible) {
        if (!visible) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    private static void handleStatusBarVisibilityApi1(@NonNull Window window, boolean visible) {
        if (!visible) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public static void handleNavigationBarVisibility(@NonNull Window window, boolean visible) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            handleNavigationBarVisibilityApi30(window, visible);
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private static void handleNavigationBarVisibilityApi30(@NonNull Window window, boolean visible) {
        WindowInsetsControllerCompat windowInsetsControllerCompat
                = WindowCompat.getInsetsController(window, window.getDecorView());
        if (visible) {
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
    }
}
