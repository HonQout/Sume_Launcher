package com.qch.sumelauncher.launcher;

import java.util.List;

public class LauncherFolder extends LauncherItem {
    private String name;
    private List<LauncherIcon> items;

    public LauncherFolder(long id, CellPosition cellPosition, String name, List<LauncherIcon> items) {
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

    public List<LauncherIcon> getItems() {
        return items;
    }
}