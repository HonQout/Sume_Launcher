package com.qch.sumelauncher.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.qch.sumelauncher.room.entity.HiddenActivity;

import java.util.List;

@Dao
public interface HiddenActivityDao {
    @Query("SELECT * FROM hiddenactivity")
    List<HiddenActivity> getAll();

    @Query("SELECT * FROM hiddenactivity WHERE `key` IN (:hiddenActivityKeys)")
    List<HiddenActivity> getAllByKeys(String[] hiddenActivityKeys);

    @Insert
    void insertAll(HiddenActivity... hiddenActivities);

    @Delete
    void delete(HiddenActivity hiddenActivity);
}
