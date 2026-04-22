package com.qch.sumelauncher.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "folders",
        foreignKeys = @ForeignKey(
                entity = LayoutEntity.class,
                parentColumns = "id",
                childColumns = "layout_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index(value = {"layout_id", "cell_x", "cell_y"}, unique = true)}
)
public class FolderEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @ColumnInfo(name = "layout_id")
    public int layoutId;
    @ColumnInfo(name = "screen_index")
    private int screenIndex;
    @ColumnInfo(name = "cell_x")
    public int cellX;
    @ColumnInfo(name = "cell_y")
    public int cellY;
    @ColumnInfo(name = "span_x")
    private int spanX;
    @ColumnInfo(name = "span_y")
    private int spanY;
    @ColumnInfo(name = "name")
    public String name;

    public FolderEntity(int layoutId, int screenIndex, int cellX, int cellY, int spanX, int spanY,
                        String name) {
        this.layoutId = layoutId;
        this.screenIndex = screenIndex;
        this.cellX = cellX;
        this.cellY = cellY;
        this.spanX = spanX;
        this.spanY = spanY;
        this.name = name;
    }

    public void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
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

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public int getLayoutId() {
        return layoutId;
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

    public String getName() {
        return name;
    }
}