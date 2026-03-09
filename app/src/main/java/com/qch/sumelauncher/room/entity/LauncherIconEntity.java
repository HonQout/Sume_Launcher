package com.qch.sumelauncher.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.qch.sumelauncher.launcher.CellPosition;
import com.qch.sumelauncher.launcher.LauncherIconData;

@Entity(
        indices = {@Index(value = {"grid_size", "page_index", "cell_x", "cell_y"}, unique = true)}
)
public class LauncherIconEntity {
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
    @ColumnInfo(name = "package_name")
    public String packageName;
    @ColumnInfo(name = "activity_name")
    public String activityName;

    public LauncherIconEntity(String gridSize, int pageIndex, int cellX, int cellY,
                              String packageName, String activityName) {
        this.packageName = packageName;
        this.activityName = activityName;
        this.gridSize = gridSize;
        this.pageIndex = pageIndex;
        this.cellX = cellX;
        this.cellY = cellY;
    }

    public String getKey() {
        return packageName + ":" + activityName;
    }

    public LauncherIconData toLauncherIconData() {
        CellPosition cellPosition = new CellPosition(gridSize, pageIndex, cellX, cellY, 1, 1);
        return new LauncherIconData(id, cellPosition, packageName, activityName);
    }
}