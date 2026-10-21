package com.qch.sumelauncher.ui.recyclerview.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class ClickableListAdapter<T, VH extends RecyclerView.ViewHolder>
        extends BaseListAdapter<T, VH> {
    protected OnItemClickListener<T> onItemClickListener;

    public ClickableListAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
    }

    public ClickableListAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback, List<T> list) {
        super(diffCallback, list);
    }

    public void setOnItemClickListener(@NonNull OnItemClickListener<T> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
