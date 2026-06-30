package com.qch.sumelauncher.launcher.item;

import com.qch.sumelauncher.bean.ActivityRecord;
import com.qch.sumelauncher.launcher.cell.CellPosition;
import com.qch.sumelauncher.room.entity.FolderEntity;

import java.util.List;

public class LauncherFolder extends LauncherItem {
    private static final String TAG = "LauncherFolder";
    private final String name;
    private final List<ActivityRecord> items;

    public LauncherFolder(long id, CellPosition cellPosition, String name, List<ActivityRecord> items) {
        super(id, cellPosition);
        this.name = name;
        this.items = items;
    }

    @Override
    public Type getType() {
        return Type.FOLDER;
    }

    public String getName() {
        return name;
    }

    public List<ActivityRecord> getItems() {
        return items;
    }

    public FolderEntity toFolderEntity() {
        return new FolderEntity(
                cellPosition.getLayoutName(),
                cellPosition.getScreenIndex(),
                cellPosition.getCellX(),
                cellPosition.getCellY(),
                cellPosition.getSpanX(),
                cellPosition.getSpanY(),
                name,
                items
        );
    }
}