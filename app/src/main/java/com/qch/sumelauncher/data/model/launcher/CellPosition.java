package com.qch.sumelauncher.data.model.launcher;

public class CellPosition {
    private long pageId;
    private int cellX;
    private int cellY;
    private int spanX;
    private int spanY;

    public CellPosition(long pageId, int cellX, int cellY, int spanX, int spanY) {
        this.pageId = pageId;
        this.cellX = cellX;
        this.cellY = cellY;
        this.spanX = spanX;
        this.spanY = spanY;
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
}