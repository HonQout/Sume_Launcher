package com.qch.sumelauncher.ui.recyclerview.adapter;

import android.view.View;

public interface OnItemClickListener<T> {
    void onItemClick(T item, View view);

    boolean onItemLongClick(T item, View view);
}
