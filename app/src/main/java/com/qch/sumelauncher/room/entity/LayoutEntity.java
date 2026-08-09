package com.qch.sumelauncher.room.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "layouts")
public class LayoutEntity {
    @PrimaryKey
    @ColumnInfo(name = "name")
    @NonNull
    private String name;
    @ColumnInfo(name = "column_count")
    private int columnCount;
    @ColumnInfo(name = "row_count")
    private int rowCount;
    @ColumnInfo(name = "is_default")
    private boolean isDefault;

    public LayoutEntity(@NonNull String name, int columnCount, int rowCount, boolean isDefault) {
        this.name = name;
        this.columnCount = columnCount;
        this.rowCount = rowCount;
        this.isDefault = isDefault;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public boolean isDefault() {
        return isDefault;
    }
}