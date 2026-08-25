package com.qch.sumelauncher.compat;

import java.util.List;

public class ListCompat {
    // Private constructor prevents this class from being instantiated.
    private ListCompat() {

    }

    public static <T> boolean isNullOrEmpty(List<T> list) {
        return list == null || list.isEmpty();
    }
}