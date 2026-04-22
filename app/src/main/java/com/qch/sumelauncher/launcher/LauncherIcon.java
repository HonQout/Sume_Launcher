package com.qch.sumelauncher.launcher;

import com.qch.sumelauncher.room.entity.IconEntity;

public class LauncherIcon extends LauncherItem {
    private String packageName;
    private String activityName;

    public LauncherIcon(long id, CellPosition cellPosition, String packageName, String activityName) {
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

    public IconEntity toLauncherIconEntity() {
        return new IconEntity(
                cellPosition.getLayoutName(),
                cellPosition.getScreenIndex(),
                cellPosition.getCellX(),
                cellPosition.getCellY(),
                1,
                1,
                packageName,
                activityName
        );
    }
}