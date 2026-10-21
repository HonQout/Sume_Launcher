package com.qch.sumelauncher.log;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qch.sumelauncher.BuildConfig;

public class AppLog {
    // Private constructor prevents this class from being instantiated.
    private AppLog() {

    }

    public static void v(@Nullable String tag, @NonNull String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, msg);
        }
    }

    public static void v(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, msg, tr);
        }
    }

    public static void d(@Nullable String tag, @NonNull String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void d(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg, tr);
        }
    }

    public static void i(@Nullable String tag, @NonNull String msg) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, msg);
        }
    }

    public static void i(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, msg, tr);
        }
    }

    public static void w(@Nullable String tag, @NonNull String msg) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, msg);
        }
    }

    public static void w(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, msg, tr);
        }
    }

    public static int w(@Nullable String tag, @Nullable Throwable tr) {
        if (BuildConfig.DEBUG) {
            return Log.w(tag, tr);
        }
        return 0;
    }

    public static void e(@Nullable String tag, @NonNull String msg) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg);
        }
    }

    public static void e(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg, tr);
        }
    }
}