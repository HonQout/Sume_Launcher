package com.qch.sumelauncher.utils;

import android.content.Context;
import android.util.TypedValue;

import androidx.annotation.NonNull;

public class ThemeUtils {
    public static int resolveAttribute(@NonNull Context context, int resId) {
        TypedValue typedValue = new TypedValue();
        boolean resolveSuccess
                = context.getTheme().resolveAttribute(resId, typedValue, true);
        if (resolveSuccess) {
            return typedValue.data;
        } else {
            return 0;
        }
    }
}
