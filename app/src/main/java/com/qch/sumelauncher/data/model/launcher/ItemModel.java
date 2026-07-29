package com.qch.sumelauncher.data.model.launcher;

public abstract class ItemModel {
    protected long id;
    protected CellPosition cellPosition;

    public enum Type {
        ICON, FOLDER, WIDGET
    }

    public ItemModel(long id, CellPosition cellPosition) {
        this.id = id;
        this.cellPosition = cellPosition;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setCellPosition(CellPosition cellPosition) {
        this.cellPosition = cellPosition;
    }

    public CellPosition getCellPosition() {
        return cellPosition;
    }

    public void setCellX(int cellX) {
        cellPosition.setCellX(cellX);
    }

    public int getCellX() {
        return cellPosition.getCellX();
    }

    public void setCellY(int cellY) {
        cellPosition.setCellY(cellY);
    }

    public int getCellY() {
        return cellPosition.getCellY();
    }

    public void setSpanX(int spanX) {
        cellPosition.setSpanX(spanX);
    }

    public int getSpanX() {
        return cellPosition.getSpanX();
    }

    public void setSpanY(int spanY) {
        cellPosition.setSpanY(spanY);
    }

    public int getSpanY() {
        return cellPosition.getSpanY();
    }

    public abstract Type getType();
}