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
        preferenceDataStore.putBooleanAsync(key, value);
    }

    @Override
    public void putInt(String key, int value) {
        preferenceDataStore.putIntegerAsync(key, value);
    }

    @Override
    public void putLong(String key, long value) {
        preferenceDataStore.putLongAsync(key, value);
    }

    @Override
    public void putFloat(String key, float value) {
        preferenceDataStore.putFloatAsync(key, value);
    }

    @Override
    public void putString(String key, @Nullable String value) {
        preferenceDataStore.putStringAsync(key, value);
    }

    @Override
    public void putStringSet(String key, @Nullable Set<String> values) {
        preferenceDataStore.putStringSetAsync(key, values);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return preferenceDataStore.getBooleanSync(key, defValue);
    }

    @Override
    public int getInt(String key, int defValue) {
        return preferenceDataStore.getIntegerSync(key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return preferenceDataStore.getLongSync(key, defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return preferenceDataStore.getFloatSync(key, defValue);
    }

    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        return preferenceDataStore.getStringSync(key, defValue);
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        return preferenceDataStore.getStringSetSync(key, defValues);
    }
}