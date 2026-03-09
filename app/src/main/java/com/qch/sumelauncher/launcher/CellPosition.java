package com.qch.sumelauncher.launcher;

public class CellPosition {
    private String gridSize;
    private int pageIndex;
    private int cellX;
    private int cellY;
    private int spanX;
    private int spanY;

    public CellPosition(String gridSize, int pageIndex, int cellX, int cellY, int spanX, int spanY) {
        this.gridSize = gridSize;
        this.pageIndex = pageIndex;
        this.cellX = cellX;
        this.cellY = cellY;
        this.spanX = spanX;
        this.spanY = spanY;
    }

    public void setGridSize(String gridSize) {
        this.gridSize = gridSize;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
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

    public String getGridSize() {
        return gridSize;
    }

    public int getPageIndex() {
        return pageIndex;
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