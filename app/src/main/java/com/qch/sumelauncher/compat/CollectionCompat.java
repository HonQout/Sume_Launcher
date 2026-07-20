package com.qch.sumelauncher.compat;

import android.os.Build;

import java.util.Collection;
import java.util.Iterator;

public class CollectionCompat {
    public interface Predicate<T> {
        boolean satisfyCondition(T item);
    }

    public static <T> boolean removeIf(Collection<T> collection, Predicate<T> predicate) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            collection.removeIf(predicate::satisfyCondition);
        } else {
            Iterator<T> iterator = collection.iterator();
            while (iterator.hasNext()) {
                if (predicate.satisfyCondition(iterator.next())) {
                    iterator.remove();
                }
            }
        }
        return true;
    }
}