package com.qch.sumelauncher;

import android.app.Application;

import com.qch.sumelauncher.persistence.PreferenceDataStoreImpl;

public class MyApplication extends Application {
    private static PreferenceDataStoreImpl preferenceDataStore;

    @Override
    public void onCreate() {
        super.onCreate();
        preferenceDataStore = new PreferenceDataStoreImpl(this);
    }

    public static PreferenceDataStoreImpl getPreferenceDataStore() {
        return preferenceDataStore;
    }
}
