package com.qch.sumelauncher.room.database;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.qch.sumelauncher.bean.ActivityBean;
import com.qch.sumelauncher.room.dao.LauncherIconDao;
import com.qch.sumelauncher.room.entity.IconEntity;
import com.qch.sumelauncher.room.entity.LayoutEntity;
import com.qch.sumelauncher.room.repository.LauncherIconRepository;
import com.qch.sumelauncher.utils.ApplicationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {LayoutEntity.class, IconEntity.class},
        version = 2,
        exportSchema = false
)
public abstract class LauncherItemDatabase extends RoomDatabase {
    private static final String TAG = "LauncherItemDatabase";

    public abstract LauncherIconDao launcherIconDao();

    private static volatile LauncherItemDatabase instance;

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

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
                                    initLayouts(context.getApplicationContext());
                                }
                            })
                            .fallbackToDestructiveMigration(true)
                            .build();
                }
            }
        }
        return instance;
    }

    public static List<IconEntity> getDefaultLauncherIconList(@NonNull Context context,
                                                              @NonNull LayoutEntity layoutEntity) {
        List<IconEntity> iconEntityList = new ArrayList<>();
        List<ActivityBean> activityBeanList
                = ApplicationUtils.getActivityBeanList(context, null);
        String layoutName = layoutEntity.getName();
        int numColumn = layoutEntity.getNumColumns();
        int numRow = layoutEntity.getNumRows();
        int screenIndex = 0;
        int cellX = 0; // column
        int cellY = 0; // row
        for (int i = 0; i < activityBeanList.size(); i++) {
            Log.i(TAG, "Layout = " + layoutName + " ScreenIndex = " + screenIndex
                    + " Cell_X = " + cellX + " Cell_Y = " + cellY);
            ActivityBean activityBean = activityBeanList.get(i);
            IconEntity iconEntity = new IconEntity(layoutName, screenIndex, cellX, cellY, 1,
                    1, activityBean.getPackageName(), activityBean.getActivityName());
            iconEntityList.add(iconEntity);
            cellX++;
            if (cellX >= numColumn) {
                cellX = 0;
                cellY++;
            }
            if (cellY >= numRow) {
                cellY = 0;
                screenIndex++;
            }
        }
        return iconEntityList;
    }

    private static void initLayouts(@NonNull Context context) {
        executorService.execute(() -> {
            LauncherItemDatabase database = getInstance(context);
            LayoutEntity layout44 =
                    new LayoutEntity(LauncherIconRepository.LayoutConfig.FOUR_BY_FOUR.getConfig(),
                            4, 4, false);
            database.launcherIconDao().insertLayout(layout44);
            database.launcherIconDao().insertIconList(getDefaultLauncherIconList(context, layout44));
            LayoutEntity layout45 =
                    new LayoutEntity(LauncherIconRepository.LayoutConfig.FOUR_BY_FIVE.getConfig(),
                            4, 5, false);
            database.launcherIconDao().insertLayout(layout45);
            database.launcherIconDao().insertIconList(getDefaultLauncherIconList(context, layout45));
            LayoutEntity layout54 =
                    new LayoutEntity(LauncherIconRepository.LayoutConfig.FIVE_BY_FOUR.getConfig(),
                            5, 4, false);
            database.launcherIconDao().insertLayout(layout54);
            database.launcherIconDao().insertIconList(getDefaultLauncherIconList(context, layout54));
            LayoutEntity layout55 =
                    new LayoutEntity(LauncherIconRepository.LayoutConfig.FIVE_BY_FIVE.getConfig(),
                            5, 5, true);
            database.launcherIconDao().insertLayout(layout55);
            database.launcherIconDao().insertIconList(getDefaultLauncherIconList(context, layout55));
        });
    }
}