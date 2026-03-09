package com.qch.sumelauncher.launcher;

import com.qch.sumelauncher.room.entity.LauncherIconEntity;

public class LauncherIconData extends LauncherItem {
    private String packageName;
    private String activityName;

    public LauncherIconData(long id, CellPosition cellPosition, String packageName, String activityName) {
        super(id, cellPosition);
        this.packageName = packageName;
        this.activityName = activityName;
    }

    @Override
    public Type getType() {
        return Type.ICON;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getActivityName() {
        return activityName;
    }

    public LauncherIconEntity toLauncherIconEntity() {
        return new LauncherIconEntity(cellPosition.getGridSize(), cellPosition.getPageIndex(),
                cellPosition.getCellX(), cellPosition.getCellY(), packageName, activityName);
    }
}