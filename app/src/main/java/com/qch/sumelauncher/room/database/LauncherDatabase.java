package com.qch.sumelauncher.room.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.qch.sumelauncher.data.model.launcher.ActivityModel;
import com.qch.sumelauncher.data.model.launcher.GridSize;
import com.qch.sumelauncher.room.converter.Converters;
import com.qch.sumelauncher.room.dao.LauncherDao;
import com.qch.sumelauncher.room.entity.IconEntity;
import com.qch.sumelauncher.room.entity.LayoutEntity;
import com.qch.sumelauncher.room.entity.PageEntity;
import com.qch.sumelauncher.utils.ApplicationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {LayoutEntity.class, PageEntity.class, IconEntity.class},
        version = 4,
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class LauncherDatabase extends RoomDatabase {
    private static final String TAG = "LauncherDatabase";

    public abstract LauncherDao launcherIconDao();

    private static volatile LauncherDatabase instance;

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static LauncherDatabase getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (LauncherDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    LauncherDatabase.class,
                                    "launcher_database"
                            )
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    executorService.execute(() ->
                                            initLayouts(context.getApplicationContext()));
                                }
                            })
                            .fallbackToDestructiveMigration(true)
                            .build();
                }
            }
        }
        return instance;
    }

    private static void initLayout(@NonNull LauncherDao dao,
                                   @NonNull LayoutEntity layoutEntity,
                                   @NonNull List<ActivityModel> activityModelList) {
        String layoutName = layoutEntity.getName();
        int columnCount = layoutEntity.getColumnCount();
        int rowCount = layoutEntity.getRowCount();

        int currentRank = 0;
        long currentPageId = dao.insertPageSync(new PageEntity(layoutName, currentRank));

        int cellX = 0; // column
        int cellY = 0; // row
        List<IconEntity> iconEntityList = new ArrayList<>();

        for (ActivityModel activityModel : activityModelList) {
            IconEntity iconEntity = new IconEntity(currentPageId, cellX, cellY, 1,
                    1, activityModel.getPackageName(), activityModel.getActivityName());
            iconEntityList.add(iconEntity);
            cellX++;
            if (cellX >= columnCount) {
                cellX = 0;
                cellY++;
            }
            if (cellY >= rowCount) {
                cellY = 0;
                currentRank++;
                currentPageId = dao.insertPageSync(new PageEntity(layoutName, currentRank));
            }
        }

        if (!iconEntityList.isEmpty()) {
            dao.insertIconListSync(iconEntityList);
        }
    }

    private static void initLayouts(@NonNull Context context) {
        LauncherDatabase database = getInstance(context);
        LauncherDao dao = database.launcherIconDao();

        database.runInTransaction(() -> {
            LayoutEntity layout44 =
                    new LayoutEntity(new GridSize(4, 4).toString(), 4, 4, false);
            LayoutEntity layout45 =
                    new LayoutEntity(new GridSize(4, 5).toString(), 4, 5, false);
            LayoutEntity layout54 =
                    new LayoutEntity(new GridSize(5, 4).toString(), 5, 4, false);
            LayoutEntity layout55 =
                    new LayoutEntity(new GridSize(5, 5).toString(), 5, 5, true);

            dao.insertLayout(layout44);
            dao.insertLayout(layout45);
            dao.insertLayout(layout54);
            dao.insertLayout(layout55);

            List<ActivityModel> activityModelList = ApplicationUtils.getActivityModelList(context, null);
            if (activityModelList.isEmpty()) {
                return;
            }

            initLayout(dao, layout44, activityModelList);
            initLayout(dao, layout45, activityModelList);
            initLayout(dao, layout54, activityModelList);
            initLayout(dao, layout55, activityModelList);
        });
    }
}