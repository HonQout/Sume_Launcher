package com.qch.sumelauncher.launcher;

public abstract class LauncherItem {
    protected long id;
    protected CellPosition cellPosition;

    public enum Type {
        ICON, FOLDER, WIDGET
    }

    public LauncherItem(long id, CellPosition cellPosition) {
        this.id = id;
        this.cellPosition = cellPosition;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setCellPosition(CellPosition cellPosition) {
        this.cellPosition = cellPosition;
    }

    public long getId() {
        return id;
    }

    public CellPosition getCellPosition() {
        return cellPosition;
    }

    public abstract Type getType();
}