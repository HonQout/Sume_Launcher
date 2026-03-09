package com.qch.sumelauncher.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.qch.sumelauncher.room.entity.LauncherIconEntity;

import java.util.List;

@Dao
public interface LauncherIconDao {
    @Query("SELECT * FROM LauncherIconEntity")
    LiveData<List<LauncherIconEntity>> getItems();

    @Query("SELECT * FROM LauncherIconEntity WHERE grid_size = :gridSize ORDER BY page_index ASC, cell_y ASC, cell_x ASC")
    LiveData<List<LauncherIconEntity>> getItemsByGridSize(String gridSize);

    @Query("SELECT * FROM LauncherIconEntity WHERE grid_size = :gridSize AND page_index = :pageIndex ORDER BY cell_y ASC, cell_x ASC")
    LiveData<List<LauncherIconEntity>> getItemsByGridSizeAndPageIndex(String gridSize, int pageIndex);

    @Query("SELECT COUNT(DISTINCT page_index) FROM LauncherIconEntity WHERE grid_size = :gridSize")
    LiveData<Integer> getNumPageByGridSize(String gridSize);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertItem(LauncherIconEntity launcherIconEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertItems(LauncherIconEntity... launcherIconEntities);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertItemList(List<LauncherIconEntity> launcherIconEntityList);

    @Delete
    void deleteItem(LauncherIconEntity launcherIconEntity);

    @Query("DELETE FROM LauncherIconEntity WHERE id = :id")
    void deleteItemById(long id);

    @Query("DELETE FROM LauncherIconEntity WHERE package_name = :packageName")
    void deleteItemByPackageName(String packageName);

    @Query("DELETE FROM LauncherIconEntity WHERE package_name = :packageName AND activity_name = :activityName")
    void deleteItemByActivity(String packageName, String activityName);

    @Query("SELECT COUNT(*) FROM LauncherIconEntity WHERE grid_size = :gridSize AND page_index = :pageIndex AND cell_x = :cellX AND cell_y = :cellY")
    int isCellOccupied(String gridSize, int pageIndex, int cellX, int cellY);
}