package com.qch.sumelauncher.room.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.qch.sumelauncher.room.dao.HiddenActivityDao;
import com.qch.sumelauncher.room.entity.HiddenActivity;

@Database(entities = {HiddenActivity.class}, version = 1)
public abstract class HiddenActivityDatabase extends RoomDatabase {
    public abstract HiddenActivityDao hiddenActivityDao();
}