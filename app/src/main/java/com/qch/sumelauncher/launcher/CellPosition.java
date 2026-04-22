package com.qch.sumelauncher.launcher;

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
}