package com.qch.sumelauncher.data.model.launcher;

public class WidgetModel extends ItemModel {
    private final String packageName;
    private final String receiverName;

    public WidgetModel(long id, CellPosition cellPosition, String packageName, String receiverName) {
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
