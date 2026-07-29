package com.qch.sumelauncher.data.model.launcher;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

public class GridSize {
    private static final String TAG = "GridSize";
    public static final int DEFAULT_NUM_COLUMN = 5;
    public static final int DEFAULT_NUM_ROW = 5;
    public static final String SEPARATOR = ",";
    private int column;
    private int row;

    public GridSize(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getColumn() {
        return column;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getRow() {
        return row;
    }

    @NonNull
    public static GridSize parse(String string, @NonNull GridSize defaultValue) {
        if (TextUtils.isEmpty(string)) {
            return defaultValue;
        }
        String[] split = string.split(SEPARATOR);
        int column;
        int row;
        try {
            column = Integer.parseInt(split[0]);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Failed to parse column.", e);
            return defaultValue;
        }
        try {
            row = Integer.parseInt(split[1]);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Failed to parse row.", e);
            return defaultValue;
        }
        return new GridSize(row, column);
    }

    @NonNull
    @Override
    public String toString() {
        return getColumn() + SEPARATOR + getRow();
    }
}