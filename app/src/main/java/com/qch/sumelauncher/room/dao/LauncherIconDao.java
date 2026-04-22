package com.qch.sumelauncher.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.qch.sumelauncher.room.entity.IconEntity;
import com.qch.sumelauncher.room.entity.LayoutEntity;

import java.util.List;

@Dao
public interface LauncherIconDao {
    // layout
    @Query("SELECT * FROM layouts")
    List<LayoutEntity> getAllLayouts();

    @Query("SELECT * FROM layouts WHERE is_default = 1")
    LayoutEntity getDefaultLayout();

    @Query("SELECT * FROM layouts WHERE num_rows = :numRows AND num_columns = :numColumns")
    LayoutEntity getLayoutBySize(int numRows, int numColumns);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLayout(LayoutEntity layoutEntity);

    // icons
    @Query("SELECT * FROM icons WHERE layout_name = :layoutName ORDER BY screen_index ASC, " +
            "cell_y ASC, cell_x ASC")
    LiveData<List<IconEntity>> getIconListInLayout(String layoutName);

    @Query("SELECT COUNT(DISTINCT screen_index) FROM icons WHERE layout_name = :layoutName")
    LiveData<Integer> getNumScreens(String layoutName);

    @Query("SELECT * FROM icons WHERE layout_name = :layoutName AND screen_index = :screenIndex " +
            "ORDER BY cell_y ASC, cell_x ASC")
    LiveData<List<IconEntity>> getIconsOnScreen(String layoutName, int screenIndex);

    @Query("SELECT COUNT(*) FROM icons WHERE layout_name = :layoutName " +
            "AND screen_index = :screenIndex")
    int getIconCountOnScreen(String layoutName, int screenIndex);

    @Query("SELECT COUNT(*) FROM icons WHERE layout_name = :layoutName " +
            "AND screen_index = :screenIndex AND cell_x = :cellX AND cell_y = :cellY")
    int isCellOccupied(String layoutName, int screenIndex, int cellX, int cellY);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIcon(IconEntity iconEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIconArray(IconEntity... launcherIconEntities);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIconList(List<IconEntity> iconEntityList);

    @Delete
    void deleteIcon(IconEntity iconEntity);

    @Query("DELETE FROM icons WHERE package_name = :packageName")
    void deleteIconsByPackage(String packageName);

    @Query("DELETE FROM icons WHERE package_name = :packageName AND activity_name = :activityName")
    void deleteIconsByActivity(String packageName, String activityName);

    @Query("DELETE FROM icons WHERE layout_name = :layoutName AND screen_index = :screenIndex")
    void deleteIconsOnScreen(String layoutName, int screenIndex);

    @Query("UPDATE icons SET screen_index = screen_index - 1 WHERE layout_name = :layoutName " +
            "AND screen_index > :deletedScreenIndex")
    void shiftScreenLeft(String layoutName, int deletedScreenIndex);

    @Transaction
    default void collapseAfterDeleting(IconEntity iconEntity) {
        int count = getIconCountOnScreen(iconEntity.getLayoutName(), iconEntity.getScreenIndex());
        if (count == 0) {
            shiftScreenLeft(iconEntity.getLayoutName(), iconEntity.getScreenIndex());
        }
    }

    @Update
    void updateIcon(IconEntity iconEntity);
}