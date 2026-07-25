package com.qch.sumelauncher.persistence;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceDataStore;

import java.util.Set;

public class PreferenceDataStoreBridge extends PreferenceDataStore {
    private static final String TAG = "PreferenceDataStoreBridge";
    private final PreferenceDataStoreImpl preferenceDataStore;

    public PreferenceDataStoreBridge(PreferenceDataStoreImpl preferenceDataStore) {
        this.preferenceDataStore = preferenceDataStore;
    }

    @Override
    public void putBoolean(String key, boolean value) {
        preferenceDataStore.putBoolean(key, value);
    }

    @Override
    public void putInt(String key, int value) {
        preferenceDataStore.putInteger(key, value);
    }

    @Override
    public void putLong(String key, long value) {
        preferenceDataStore.putLong(key, value);
    }

    @Override
    public void putFloat(String key, float value) {
        preferenceDataStore.putFloat(key, value);
    }

    @Override
    public void putString(String key, @Nullable String value) {
        preferenceDataStore.putString(key, value);
    }

    @Override
    public void putStringSet(String key, @Nullable Set<String> values) {
        preferenceDataStore.putStringSet(key, values);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return preferenceDataStore.getBoolean(key, defValue);
    }

    @Override
    public int getInt(String key, int defValue) {
        return preferenceDataStore.getInteger(key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return preferenceDataStore.getLong(key, defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return preferenceDataStore.getFloat(key, defValue);
    }

    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        return preferenceDataStore.getString(key, defValue);
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        return preferenceDataStore.getStringSet(key, defValues);
    }
}