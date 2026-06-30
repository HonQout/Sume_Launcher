package com.qch.sumelauncher.launcher.item;

import com.qch.sumelauncher.launcher.cell.CellPosition;
import com.qch.sumelauncher.room.entity.IconEntity;

public class LauncherIcon extends LauncherItem {
    private static final String TAG = "LauncherIcon";
    private final String packageName;
    private final String activityName;

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

    public IconEntity toIconEntity() {
        return new IconEntity(
                cellPosition.getLayoutName(),
                cellPosition.getScreenIndex(),
                cellPosition.getCellX(),
                cellPosition.getCellY(),
                cellPosition.getSpanX(),
                cellPosition.getSpanY(),
                packageName,
                activityName
        );
    }
}