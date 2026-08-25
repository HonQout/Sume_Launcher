package com.qch.sumelauncher.application;

import android.app.Application;

import com.qch.sumelauncher.persistence.PreferenceDataStoreImpl;

public class MyApplication extends Application {
    private static PreferenceDataStoreImpl preferenceDataStoreImpl;

    @Override
    public void onCreate() {
        super.onCreate();
        preferenceDataStoreImpl = new PreferenceDataStoreImpl(this);
    }

    public static PreferenceDataStoreImpl getPreferenceDataStoreImpl() {
        return preferenceDataStoreImpl;
    }
}