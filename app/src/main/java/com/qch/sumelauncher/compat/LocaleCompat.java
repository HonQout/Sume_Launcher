package com.qch.sumelauncher.compat;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

public class LocaleCompat {
    // Private constructor prevents this class from being instantiated.
    private LocaleCompat(){

    }

    @SuppressWarnings("deprecation")
    public static Locale getCurrentLocale() {
        Configuration configuration = Resources.getSystem().getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return configuration.getLocales().get(0);
        } else {
            return configuration.locale;
        }
    }

    public static String getCurrentLanguage() {
        return getCurrentLocale().getLanguage();
    }
}