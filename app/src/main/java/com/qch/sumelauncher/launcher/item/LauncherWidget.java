package com.qch.sumelauncher.launcher.item;

import com.qch.sumelauncher.launcher.cell.CellPosition;

public class LauncherWidget extends LauncherItem {
    private final String packageName;
    private final String receiverName;

    public LauncherWidget(long id, CellPosition cellPosition, String packageName, String receiverName) {
        super(id, cellPosition);
        this.packageName = packageName;
        this.receiverName = receiverName;
    }

    @Override
    public Type getType() {
        return Type.WIDGET;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getReceiverName() {
        return receiverName;
    }
}
