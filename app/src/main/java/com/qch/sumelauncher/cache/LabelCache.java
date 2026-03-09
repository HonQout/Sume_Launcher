package com.qch.sumelauncher.cache;

import android.util.Log;
import android.util.LruCache;

import androidx.annotation.Nullable;

public class LabelCache {
    private static final String TAG = "LabelCache";
    private static final int MAX_CACHE_SIZE = 1024 * 1024;
    private final LruCache<String, String> cache;

    public LabelCache() {
        cache = new LruCache<>(MAX_CACHE_SIZE) {
            @Override
            protected int sizeOf(String key, String value) {
                return super.sizeOf(key, value);
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, String oldValue, String newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
            }
        };
    }

    public void put(String key, String value) {
        try {
            cache.put(key, value);
        } catch (Exception e) {
            Log.e(TAG, "Cannot put value corresponding to key " + key + " to cache.", e);
        }
    }

    @Nullable
    public String get(String key) {
        try {
            return cache.get(key);
        } catch (Exception e) {
            Log.e(TAG, "Cannot get value corresponding to key " + key + ".", e);
            return null;
        }
    }

    public boolean contains(String key) {
        try {
            return cache.get(key) != null;
        } catch (Exception e) {
            Log.e(TAG, "Cannot get value corresponding to key " + key + ".", e);
            return false;
        }
    }

    public void remove(String key) {
        try {
            cache.remove(key);
        } catch (Exception e) {
            Log.e(TAG, "Cannot remove value corresponding to key " + key + " from cache.", e);
        }
    }

    public void clear() {
        cache.evictAll();
    }
}
