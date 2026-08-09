package com.qch.sumelauncher.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "icons",
        indices = {
                @Index(value = {"page_id", "cell_x", "cell_y"}, unique = true),
                @Index(value = {"page_id"})
        },
        foreignKeys = @ForeignKey(
                entity = PageEntity.class,
                parentColumns = "page_id",
                childColumns = "page_id",
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.CASCADE
        )
)
public class IconEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "page_id")
    private long pageId;

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

    public IconEntity(long pageId, int cellX, int cellY, int spanX, int spanY, String packageName, String activityName) {
        this.pageId = pageId;
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

    public long getId() {
        return id;
    }

    public void setPageId(long pageId) {
        this.pageId = pageId;
    }

    public long getPageId() {
        return pageId;
    }

    public void setCellX(int cellX) {
        this.cellX = cellX;
    }

    public int getCellX() {
        return cellX;
    }

    public void setCellY(int cellY) {
        this.cellY = cellY;
    }

    public int getCellY() {
        return cellY;
    }

    public void setSpanX(int spanX) {
        this.spanX = spanX;
    }

    public int getSpanX() {
        return spanX;
    }

    public void setSpanY(int spanY) {
        this.spanY = spanY;
    }

    public int getSpanY() {
        return spanY;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getKey() {
        return packageName + ":" + activityName;
    }
}