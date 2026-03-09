package com.qch.sumelauncher.launcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LauncherDataModel {
    private Map<CellPosition, LauncherItem> items = new HashMap<>();
    private List<OnDataChangeListener> listeners = new ArrayList<>();

    public interface OnDataChangeListener {
        void onItemAdded(LauncherItem item);

        void onItemRemoved(LauncherItem item);
    }

    public void addItem(LauncherItem item) {

    }
}
