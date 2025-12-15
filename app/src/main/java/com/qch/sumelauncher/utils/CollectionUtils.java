package com.qch.sumelauncher.utils;

import androidx.annotation.NonNull;

import java.util.Iterator;
import java.util.List;

public class CollectionUtils {
    public interface RemoveCallback<T> {
        boolean shouldRemove(T item);
    }

    public static <T> boolean removeConditionally(@NonNull List<T> list, RemoveCallback<T> callback) {
        Iterator<T> iterator = list.iterator();
        while (iterator.hasNext()) {
            if (callback.shouldRemove(iterator.next())) {
                iterator.remove();
            }
        }
        return true;
    }
}