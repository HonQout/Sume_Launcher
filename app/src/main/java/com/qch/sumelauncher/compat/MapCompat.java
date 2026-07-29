package com.qch.sumelauncher.compat;

import java.util.Map;

public class MapCompat {
    // Private constructor prevents this class from being instantiated.
    private MapCompat() {

    }

    public static <K, V> boolean isNullOrEmpty(Map<K, V> map) {
        return map == null || map.isEmpty();
    }
}
