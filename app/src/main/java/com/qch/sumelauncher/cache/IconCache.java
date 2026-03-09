package com.qch.sumelauncher.cache;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.util.LruCache;

import androidx.annotation.Nullable;

public class IconCache {
    private static final String TAG = "IconCache";
    private static final int MAX_CACHE_SIZE = 50 * 1024 * 1024;
    private final LruCache<String, Bitmap> cache;

    public IconCache() {
        cache = new LruCache<>(MAX_CACHE_SIZE) {
            @SuppressLint("ObsoleteSdkInt")
            @Override
            protected int sizeOf(String key, Bitmap value) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    return value.getAllocationByteCount();
                } else {
                    return value.getByteCount();
                }
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
            }
        };
    }

    public void put(String key, Bitmap value) {
        try {
            cache.put(key, value);
        } catch (Exception e) {
            Log.e(TAG, "Cannot put value corresponding to key " + key + " to cache.", e);
        }
    }

    @Nullable
    public Bitmap get(String key) {
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