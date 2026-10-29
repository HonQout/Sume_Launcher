package com.qch.sumelauncher.multithread;

import android.os.Looper;
import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class AppExecutors {
    private static final String TAG = "AppExecutors";
    private static final String TAG_CPU = "CPU";
    private static final String TAG_DISK_IO = "DISK_IO";
    private static final String TAG_NETWORK_IO = "NETWORK_IO";
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CPU_THREADS = Math.max(1, CPU_COUNT);
    private static final int NETWORK_THREADS = Math.max(4, CPU_COUNT * 2);
    private static final int QUEUE_CAPACITY = 128;
    private static final RejectedExecutionHandler SMART_REJECT = ((r, executor) -> {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            String msg = "Task rejected on main thread, "
                    + "queue = " + executor.getQueue().size() + ", "
                    + "active = " + executor.getActiveCount();
            Log.e(AppExecutors.TAG, msg);
            throw new RejectedExecutionException(msg);
        }
        Log.w(AppExecutors.TAG, "Pool saturated, caller runs task: " + executor);
        r.run();
    });

    private final ThreadPoolExecutor cpu;
    private final ThreadPoolExecutor diskIO;
    private final ThreadPoolExecutor networkIO;
    private final MainThreadExecutor mainThread;

    public enum OtherExecutors {
        CPU,
        DISK_IO,
        NETWORK_IO
    }

    private AppExecutors(ThreadPoolExecutor cpu,
                         ThreadPoolExecutor diskIO,
                         ThreadPoolExecutor networkIO,
                         MainThreadExecutor mainThread) {
        this.cpu = cpu;
        this.diskIO = diskIO;
        this.networkIO = networkIO;
        this.mainThread = mainThread;
    }

    private static final class Holder {
        private static final AppExecutors INSTANCE = new AppExecutors(
                buildExecutor("cpu", CPU_THREADS, CPU_THREADS, 30),
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
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                core,
                max,
                keepAliveThreads,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(QUEUE_CAPACITY),
                factory,
                SMART_REJECT
        );
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }

    public Executor cpu() {
        return cpu;
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

    public void runOnOthersThenMain(OtherExecutors otherExecutors,
                                    Runnable bgTask,
                                    Runnable uiTask) {
        Executor executor;
        String label;
        switch (otherExecutors) {
            case CPU: {
                executor = cpu;
                label = TAG_CPU;
                break;
            }
            case DISK_IO: {
                executor = diskIO;
                label = TAG_DISK_IO;
                break;
            }
            case NETWORK_IO: {
                executor = networkIO;
                label = TAG_NETWORK_IO;
                break;
            }
            default: {
                Log.e(TAG, "Illegal argument: otherExecutors.");
                return;
            }
        }
        try {
            executor.execute(() -> {
                try {
                    bgTask.run();
                } catch (Throwable t) {
                    Log.e(AppExecutors.TAG, label + " task error", t);
                    return;
                }
                mainThread.execute(uiTask);
            });
        } catch (RejectedExecutionException e) {
            Log.e(AppExecutors.TAG, label + " task rejected", e);
        }
    }
}