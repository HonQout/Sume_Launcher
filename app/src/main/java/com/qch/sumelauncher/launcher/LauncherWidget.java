package com.qch.sumelauncher.launcher;

public class LauncherWidget extends LauncherItem {
    private int spanX;
    private int spanY;

    public LauncherWidget(long id, CellPosition cellPosition, int spanX, int spanY) {
        super(id, cellPosition);
        this.spanX = spanX;
        this.spanY = spanY;
    }

    @Override
    public Type getType() {
        return Type.WIDGET;
    }

    public int getSpanX() {
        return spanX;
    }

    public int getSpanY() {
        return spanY;
    }
}
