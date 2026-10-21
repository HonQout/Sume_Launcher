package com.qch.sumelauncher.multithread;

import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class AppExecutors {
    private static final String TAG = "AppExecutors";
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int NETWORK_THREADS = Math.max(4, CPU_COUNT * 2);
    private static final int QUEUE_CAPACITY = 128;

    private final ThreadPoolExecutor diskIO;
    private final ThreadPoolExecutor networkIO;
    private final MainThreadExecutor mainThread;

    private AppExecutors(ThreadPoolExecutor diskIO,
                         ThreadPoolExecutor networkIO,
                         MainThreadExecutor mainThread) {
        this.diskIO = diskIO;
        this.networkIO = networkIO;
        this.mainThread = mainThread;
    }

    private static final class Holder {
        private static final AppExecutors INSTANCE = new AppExecutors(
                buildExecutor("disk-io", 1, 1, 30),
                buildExecutor("network-io", NETWORK_THREADS, NETWORK_THREADS, 30),
                new MainThreadExecutor()
        );
    }

    public static AppExecutors getInstance() {
        return Holder.INSTANCE;
    }

    private static ThreadPoolExecutor buildExecutor(String name,
                                                    int core,
                                                    int max,
                                                    long keepAliveThreads) {
        ThreadFactory factory = new NamedThreadFactory(name);
        RejectedExecutionHandler rejectedExecutionHandler = (r, executor) -> {
            Log.w(AppExecutors.TAG, "Task rejected, pool = " + name
                            + " queue = " + executor.getQueue().size()
                            + " active = " + executor.getActiveCount(),
                    new IllegalStateException("rejected from " + name));
        };
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                core,
                max,
                keepAliveThreads,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(QUEUE_CAPACITY),
                factory,
                rejectedExecutionHandler
        );
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    public Executor diskIO() {
        return diskIO;
    }

    public Executor networkIO() {
        return networkIO;
    }

    public Executor mainThread() {
        return mainThread;
    }

    public void runOnIOThenMain(Runnable ioTask, Runnable uiTask) {
        diskIO.execute(() -> {
            try {
                ioTask.run();
            } catch (Throwable t) {
                Log.e(AppExecutors.TAG, "IO task error", t);
                return;
            }
            mainThread.getMainHandler().post(uiTask);
        });
    }

    public void shutdown() {
        diskIO.shutdown();
        networkIO.shutdown();
    }
}