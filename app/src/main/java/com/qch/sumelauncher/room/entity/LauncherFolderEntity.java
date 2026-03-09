package com.qch.sumelauncher.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        indices = {@Index(value = {"grid_size", "page_index", "cell_x", "cell_y"}, unique = true)}
)
public class LauncherFolderEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @ColumnInfo(name = "grid_size")
    public String gridSize;
    @ColumnInfo(name = "page_index")
    public int pageIndex;
    @ColumnInfo(name = "cell_x")
    public int cellX;
    @ColumnInfo(name = "cell_y")
    public int cellY;
    @ColumnInfo(name = "name")
    public String name;
}