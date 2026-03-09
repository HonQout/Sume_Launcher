package com.qch.sumelauncher.room.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.qch.sumelauncher.room.dao.LauncherIconDao;
import com.qch.sumelauncher.room.entity.LauncherIconEntity;
import com.qch.sumelauncher.utils.LauncherUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {LauncherIconEntity.class}, version = 1)
public abstract class LauncherItemDatabase extends RoomDatabase {
    public abstract LauncherIconDao launcherIconDao();

    private static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);
    private static volatile LauncherItemDatabase instance;

    public static LauncherItemDatabase getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (LauncherItemDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    LauncherItemDatabase.class,
                                    "launcher_item_database"
                            )
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    initializeDefaultLayout(context);
                                }
                            })
                            .fallbackToDestructiveMigration(true)
                            .build();
                }
            }
        }
        return instance;
    }

    public static ExecutorService getDatabaseWriteExecutor() {
        return databaseWriteExecutor;
    }

    private static void initializeDefaultLayout(@NonNull Context context) {
        getDatabaseWriteExecutor().execute(() -> {
            LauncherItemDatabase database = getInstance(context);
            database.launcherIconDao().insertItemList(
                    LauncherUtils.getDefaultLauncherIconList(context, LauncherUtils.FOUR_BY_FOUR));
            database.launcherIconDao().insertItemList(
                    LauncherUtils.getDefaultLauncherIconList(context, LauncherUtils.FOUR_BY_FIVE));
            database.launcherIconDao().insertItemList(
                    LauncherUtils.getDefaultLauncherIconList(context, LauncherUtils.FIVE_BY_FOUR));
            database.launcherIconDao().insertItemList(
                    LauncherUtils.getDefaultLauncherIconList(context, LauncherUtils.FIVE_BY_FIVE));
        });
    }
}