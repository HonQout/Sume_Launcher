package com.qch.sumelauncher.multithread;

import android.util.Log;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
    private static final String TAG = "NamedThreadFactory";
    private final String prefix;
    private final AtomicInteger index = new AtomicInteger(1);

    public NamedThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, prefix + "-" + index.getAndIncrement());
        t.setDaemon(false);
        t.setUncaughtExceptionHandler((thread, e) ->
                Log.e(NamedThreadFactory.TAG, "Uncaught in " + thread.getName(), e));
        return t;
    }
}
