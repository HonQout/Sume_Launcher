package com.qch.sumelauncher.utils;

import android.app.WallpaperManager;
import android.content.Context;

import androidx.annotation.NonNull;

public class WallpaperUtils {
    public static WallpaperManager getWallpaperManager(@NonNull Context context) {
        return WallpaperManager.getInstance(context);
    }
}
