package com.qch.sumelauncher.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "icons",
        foreignKeys = @ForeignKey(
                entity = LayoutEntity.class,
                parentColumns = "name",
                childColumns = "layout_name",
                onDelete = ForeignKey.CASCADE
        ),
        indices = @Index(value = {"layout_name", "screen_index", "cell_x", "cell_y"}, unique = true)
)
public class IconEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "layout_name")
    private String layoutName;
    @ColumnInfo(name = "screen_index")
    private int screenIndex;
    @ColumnInfo(name = "cell_x")
    private int cellX; // column
    @ColumnInfo(name = "cell_y")
    private int cellY; // row
    @ColumnInfo(name = "span_x")
    private int spanX;
    @ColumnInfo(name = "span_y")
    private int spanY;
    @ColumnInfo(name = "package_name")
    private String packageName;
    @ColumnInfo(name = "activity_name")
    private String activityName;

    public IconEntity(String layoutName, int screenIndex, int cellX, int cellY, int spanX, int spanY,
                      String packageName, String activityName) {
        this.layoutName = layoutName;
        this.screenIndex = screenIndex;
        this.cellX = cellX;
        this.cellY = cellY;
        this.spanX = spanX;
        this.spanY = spanY;
        this.packageName = packageName;
        this.activityName = activityName;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setLayoutName(String layoutName) {
        this.layoutName = layoutName;
    }

    public void setScreenIndex(int screenIndex) {
        this.screenIndex = screenIndex;
    }

    public void setCellX(int cellX) {
        this.cellX = cellX;
    }

    public void setCellY(int cellY) {
        this.cellY = cellY;
    }

    public void setSpanX(int spanX) {
        this.spanX = spanX;
    }

    public void setSpanY(int spanY) {
        this.spanY = spanY;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public long getId() {
        return id;
    }

    public String getLayoutName() {
        return layoutName;
    }

    public int getScreenIndex() {
        return screenIndex;
    }

    public int getCellX() {
        return cellX;
    }

    public int getCellY() {
        return cellY;
    }

    public int getSpanX() {
        return spanX;
    }

    public int getSpanY() {
        return spanY;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getKey() {
        return packageName + ":" + activityName;
    }
}