package com.qch.sumelauncher.room.repository;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.qch.sumelauncher.multithread.AppExecutors;
import com.qch.sumelauncher.room.dao.LauncherIconDao;
import com.qch.sumelauncher.room.database.LauncherItemDatabase;
import com.qch.sumelauncher.room.entity.IconEntity;
import com.qch.sumelauncher.room.entity.LayoutEntity;
import com.qch.sumelauncher.utils.ApplicationUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;

public class LauncherIconRepository {
    private static final String TAG = "LauncherIconRepository";
    private final LauncherIconDao dao;
    private final Context appContext;
    private final AppExecutors appExecutors;

    public enum LayoutConfig {
        FOUR_FOUR("4,4"),
        FOUR_FIVE("4,5"),
        FIVE_FOUR("5,4"),
        FIVE_FIVE("5,5");

        private final String config;

        LayoutConfig(String config) {
            this.config = config;
        }

        public String getConfig() {
            return config;
        }
    }

    public LauncherIconRepository(Context context) {
        LauncherItemDatabase db = LauncherItemDatabase.getInstance(context);
        this.dao = db.launcherIconDao();
        this.appContext = context.getApplicationContext();
        this.appExecutors = AppExecutors.getInstance();
    }

    // layout
    public List<LayoutEntity> getAllLayouts() {
        return dao.getAllLayouts();
    }

    public LayoutEntity getDefaultLayout() {
        return dao.getDefaultLayout();
    }

    public LayoutEntity getLayoutBySize(int rows, int columns) {
        return dao.getLayoutBySize(rows, columns);
    }

    public void insertLayout(LayoutEntity layoutEntity) {
        appExecutors.diskIO().execute(() -> dao.insertLayout(layoutEntity));
    }

    // screens
    public List<IconEntity> compactScreens(List<IconEntity> iconEntityList) {
        TreeSet<Integer> oldScreenIndexes = new TreeSet<>();
        for (IconEntity iconEntity : iconEntityList) {
            oldScreenIndexes.add(iconEntity.getScreenIndex());
        }
        Map<Integer, Integer> indexMapping = new HashMap<>();
        int newIndex = 0;
        for (Integer oldIndex : oldScreenIndexes) {
            indexMapping.put(oldIndex, newIndex++);
        }
        for (IconEntity iconEntity : iconEntityList) {
            Integer newScreenIndexInteger = indexMapping.get(iconEntity.getScreenIndex());
            if (newScreenIndexInteger != null) {
                iconEntity.setScreenIndex(newScreenIndexInteger);
            }
        }
        return iconEntityList;
    }

    // icons
    public LiveData<List<IconEntity>> getIconListInLayout(String layoutName) {
        return dao.getIconListInLayout(layoutName);
    }

    public LiveData<Map<Integer, List<IconEntity>>> getIconMapInLayout(String layoutName) {
        LiveData<List<IconEntity>> list = getIconListInLayout(layoutName);
        return Transformations.map(list, iconEntities -> {
            // error correction
            List<IconEntity> compactedList = compactScreens(iconEntities);
            // place IconEntity items into map
            Map<Integer, List<IconEntity>> map = new TreeMap<>();
            for (IconEntity iconEntity : compactedList) {
                // check if this record is significant
                String packageName = iconEntity.getPackageName();
                String activityName = iconEntity.getActivityName();
                if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(activityName)) {
                    deleteIcon(iconEntity, true);
                    continue;
                }
                // check if the activity corresponding to this record still exists
                // TODO: There should be optimization of this operation to avoid waiting for
                //       communication with system service
                ActivityInfo activityInfo =
                        ApplicationUtils.getActivityInfo(appContext, packageName, activityName);
                if (activityInfo == null) {
                    deleteIcon(iconEntity, true);
                    continue;
                }
                if (!map.containsKey(iconEntity.getScreenIndex())) {
                    map.put(iconEntity.getScreenIndex(), new ArrayList<>());
                }
                Objects.requireNonNull(map.get(iconEntity.getScreenIndex())).add(iconEntity);
            }
            return map;
        });
    }

    public LiveData<Integer> getNumScreens(String layoutName) {
        return dao.getNumScreens(layoutName);
    }

    public LiveData<List<IconEntity>> getIconsOnScreen(String layoutName, int screenIndex) {
        return dao.getIconsOnScreen(layoutName, screenIndex);
    }

    public int getIconCountOnScreen(String layoutName, int screenIndex) {
        return dao.getIconCountOnScreen(layoutName, screenIndex);
    }

    public boolean isCellOccupied(String layoutName, int screenIndex, int cellX, int cellY) {
        return dao.isCellOccupied(layoutName, screenIndex, cellX, cellY) > 0;
    }

    public boolean[][] getOccupiedCells(LayoutEntity layoutEntity, int screenIndex) {
        String layoutName = layoutEntity.getName();
        int rows = layoutEntity.getNumRows();
        int columns = layoutEntity.getNumColumns();
        boolean[][] occupied = new boolean[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                occupied[i][j] = false;
            }
        }
        List<IconEntity> iconEntityList = getIconsOnScreen(layoutName, screenIndex).getValue();
        if (iconEntityList != null) {
            for (IconEntity iconEntity : iconEntityList) {
                for (int y = 0; y < iconEntity.getSpanY(); y++) {
                    for (int x = 0; x < iconEntity.getSpanX(); x++) {
                        int targetY = iconEntity.getCellY();
                        int targetX = iconEntity.getCellX();
                        if (targetY < rows && targetX < columns) {
                            occupied[targetY][targetX] = true;
                        }
                    }
                }
            }
        }
        return occupied;
    }

    public void insertIcon(IconEntity iconEntity) {
        appExecutors.diskIO().execute(() -> dao.insertIcon(iconEntity));
    }

    public void insertIconArray(IconEntity[] iconEntityArray) {
        appExecutors.diskIO().execute(() -> dao.insertIconArray(iconEntityArray));
    }

    public void insertIconList(List<IconEntity> iconEntityList) {
        appExecutors.diskIO().execute(() -> dao.insertIconList(iconEntityList));
    }

    public void moveIconToNewScreen(IconEntity iconEntity) {
        appExecutors.diskIO().execute(() -> {
            LiveData<Integer> numScreensLD = getNumScreens(iconEntity.getLayoutName());
            if (numScreensLD == null) {
                return;
            }
            if (numScreensLD.getValue() == null) {
                return;
            }
            int numScreens = numScreensLD.getValue();
            iconEntity.setScreenIndex(numScreens);
            iconEntity.setCellX(0);
            iconEntity.setCellY(0);
            int indexUpdated = dao.updateIcon(iconEntity);
            Log.i(TAG, "Updated record #" + indexUpdated);
        });
    }

    public void deleteIcon(IconEntity iconEntity, boolean collapse) {
        appExecutors.diskIO().execute(() -> {
            dao.deleteIcon(iconEntity);
            if (collapse) {
                dao.collapseAfterDeleting(iconEntity);
            }
        });
    }

    public void deleteIconsByPackage(String packageName) {
        appExecutors.diskIO().execute(() -> dao.deleteIconsByPackage(packageName));
    }

    public void deleteIconsByActivity(String packageName, String activityName) {
        appExecutors.diskIO().execute(() -> dao.deleteIconsByActivity(packageName, activityName));
    }

    public void deleteIconsOnScreen(String layoutName, int screenIndex) {
        appExecutors.diskIO().execute(() -> dao.deleteIconsOnScreen(layoutName, screenIndex));
    }

    public void collapseAfterDeleting(IconEntity iconEntity) {
        appExecutors.diskIO().execute(() -> dao.collapseAfterDeleting(iconEntity));
    }

    public void updateIcon(IconEntity iconEntity) {
        appExecutors.diskIO().execute(() -> {
            int indexUpdated = dao.updateIcon(iconEntity);
            Log.i(TAG, "Updated record #" + indexUpdated);
        });
    }
}