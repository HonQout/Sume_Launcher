package com.qch.sumelauncher.data.model.launcher;

public class CellPosition {
    private String layoutName;
    private int screenIndex;
    private int cellX;
    private int cellY;
    private int spanX;
    private int spanY;

    public CellPosition(String layoutName, int screenIndex, int cellX, int cellY, int spanX, int spanY) {
        this.layoutName = layoutName;
        this.screenIndex = screenIndex;
        this.cellX = cellX;
        this.cellY = cellY;
        this.spanX = spanX;
        this.spanY = spanY;
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
}