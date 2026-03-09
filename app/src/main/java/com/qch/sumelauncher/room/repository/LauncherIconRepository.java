package com.qch.sumelauncher.room.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.qch.sumelauncher.room.dao.LauncherIconDao;
import com.qch.sumelauncher.room.database.LauncherItemDatabase;
import com.qch.sumelauncher.room.entity.LauncherIconEntity;

import java.util.List;

public class LauncherIconRepository {
    private final LauncherIconDao launcherIconDao;

    public LauncherIconRepository(Context context) {
        LauncherItemDatabase db = LauncherItemDatabase.getInstance(context);
        launcherIconDao = db.launcherIconDao();
    }

    public LiveData<List<LauncherIconEntity>> getItems() {
        return launcherIconDao.getItems();
    }

    public LiveData<List<LauncherIconEntity>> getItemsByGridSize(String gridSize) {
        return launcherIconDao.getItemsByGridSize(gridSize);
    }

    public LiveData<List<LauncherIconEntity>> getItemsByGridSizeAndPageIndex(String gridSize, int pageIndex) {
        return launcherIconDao.getItemsByGridSizeAndPageIndex(gridSize, pageIndex);
    }

    public LiveData<Integer> getNumPageByGridSize(String gridSize) {
        return launcherIconDao.getNumPageByGridSize(gridSize);
    }

    public void insertItem(LauncherIconEntity launcherIconEntity) {
        LauncherItemDatabase.getDatabaseWriteExecutor().execute(() ->
                launcherIconDao.insertItem(launcherIconEntity));
    }

    public void insertItemArray(LauncherIconEntity[] launcherIconEntityArray) {
        LauncherItemDatabase.getDatabaseWriteExecutor().execute(() ->
                launcherIconDao.insertItems(launcherIconEntityArray));
    }

    public void insertItemList(List<LauncherIconEntity> launcherIconEntityList) {
        LauncherItemDatabase.getDatabaseWriteExecutor().execute(() ->
                launcherIconDao.insertItemList(launcherIconEntityList));
    }

    public void deleteItem(LauncherIconEntity launcherIconEntity) {
        LauncherItemDatabase.getDatabaseWriteExecutor().execute(() ->
                launcherIconDao.deleteItem(launcherIconEntity));
    }

    public void deleteItemById(int id) {
        LauncherItemDatabase.getDatabaseWriteExecutor().execute(() ->
                launcherIconDao.deleteItemById(id));
    }

    public void deleteItemByPackageName(String packageName) {
        LauncherItemDatabase.getDatabaseWriteExecutor().execute(() ->
                launcherIconDao.deleteItemByPackageName(packageName));
    }

    public void deleteItemByActivity(String packageName, String activityName) {
        LauncherItemDatabase.getDatabaseWriteExecutor().execute(() ->
                launcherIconDao.deleteItemByActivity(packageName, activityName));
    }

    public boolean isCellOccupied(String gridSize, int screen, int cellX, int cellY) {
        return launcherIconDao.isCellOccupied(gridSize, screen, cellX, cellY) > 0;
    }
}