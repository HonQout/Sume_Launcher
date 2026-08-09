package com.qch.sumelauncher.data.model.launcher;

import com.qch.sumelauncher.room.entity.FolderEntity;

import java.util.List;

public class FolderModel extends ItemModel {
    private static final String TAG = "FolderModel";
    private final String name;
    private final List<ActivityRecord> items;

    public FolderModel(long id, CellPosition cellPosition, String name, List<ActivityRecord> items) {
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
                cellPosition.getPageId(),
                cellPosition.getCellX(),
                cellPosition.getCellY(),
                cellPosition.getSpanX(),
                cellPosition.getSpanY(),
                name,
                items
        );
    }
}