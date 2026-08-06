package com.qch.sumelauncher.recyclerview.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class ClickableListAdapter<T, VH extends RecyclerView.ViewHolder>
        extends BaseListAdapter<T, VH> {
    protected OnItemClickListener<T> onItemClickListener;

    public interface OnItemClickListener<T> {
        void onItemClick(T item, View view);

        boolean onItemLongClick(T item, View view);
    }

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
