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
    @ColumnInfo(name = "num_rows")
    private int numRows;
    @ColumnInfo(name = "num_columns")
    private int numColumns;
    @ColumnInfo(name = "is_default")
    private boolean isDefault;

    public LayoutEntity(@NonNull String name, int numColumns, int numRows, boolean isDefault) {
        this.name = name;
        this.numColumns = numColumns;
        this.numRows = numRows;
        this.isDefault = isDefault;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
    }

    public void setNumRows(int numRows) {
        this.numRows = numRows;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public int getNumColumns() {
        return numColumns;
    }

    public int getNumRows() {
        return numRows;
    }

    public boolean isDefault() {
        return isDefault;
    }
}