package com.qch.sumelauncher.utils;

import android.content.Context;
import android.content.pm.PackageInfo;

public class ConfigUtils {
    public static String getVersionName(Context context) {
        PackageInfo packageInfo = ApplicationUtils.getPackageInfo(context, context.getPackageName());
        if (packageInfo != null) {
            return packageInfo.versionName;
        }
        return "";
    }
}
