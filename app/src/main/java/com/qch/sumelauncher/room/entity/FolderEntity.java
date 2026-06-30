package com.qch.sumelauncher.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.qch.sumelauncher.bean.ActivityRecord;

import java.util.List;

@Entity(
        tableName = "folders",
        foreignKeys = @ForeignKey(
                entity = LayoutEntity.class,
                parentColumns = "id",
                childColumns = "layout_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index(value = {"layout_name", "cell_x", "cell_y"}, unique = true)}
)
public class FolderEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "layout_name")
    private String layoutName;
    @ColumnInfo(name = "screen_index")
    private int screenIndex;
    @ColumnInfo(name = "cell_x")
    private int cellX;
    @ColumnInfo(name = "cell_y")
    private int cellY;
    @ColumnInfo(name = "span_x")
    private int spanX;
    @ColumnInfo(name = "span_y")
    private int spanY;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "contents")
    private List<ActivityRecord> activityRecordList;

    /**
     * No-Arg Constructor
     * Gson needs this constructor. Do not delete it.
     */
    public FolderEntity() {

    }

    public FolderEntity(String layoutName, int screenIndex, int cellX, int cellY, int spanX, int spanY,
                        String name, List<ActivityRecord> activityRecordList) {
        this.layoutName = layoutName;
        this.screenIndex = screenIndex;
        this.cellX = cellX;
        this.cellY = cellY;
        this.spanX = spanX;
        this.spanY = spanY;
        this.name = name;
        this.activityRecordList = activityRecordList;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setLayoutName(String layoutName) {
        this.layoutName = layoutName;
    }

    public String getLayoutName() {
        return layoutName;
    }

    public void setScreenIndex(int screenIndex) {
        this.screenIndex = screenIndex;
    }

    public int getScreenIndex() {
        return screenIndex;
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

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setActivityRecordList(List<ActivityRecord> activityRecordList) {
        this.activityRecordList = activityRecordList;
    }

    public List<ActivityRecord> getActivityRecordList() {
        return activityRecordList;
    }
}