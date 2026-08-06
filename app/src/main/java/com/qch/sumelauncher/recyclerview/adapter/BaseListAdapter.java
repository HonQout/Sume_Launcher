package com.qch.sumelauncher.recyclerview.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseListAdapter<T, VH extends RecyclerView.ViewHolder> extends ListAdapter<T, VH> {
    protected List<T> list;

    public BaseListAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
    }

    public BaseListAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback, List<T> list) {
        super(diffCallback);
        setList(list);
    }

    public void setList(List<T> list) {
        List<T> mList = list == null ? new ArrayList<>() : new ArrayList<>(list);
        submitList(mList);
        this.list = mList;
    }
}