package com.qch.sumelauncher.adapter.recyclerview;

import android.text.TextUtils;
import android.view.View;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class FilterableListAdapter<T, VH extends RecyclerView.ViewHolder> extends ListAdapter<T, VH> implements Filterable {
    protected List<T> list;
    protected OnItemClickListener<T> onItemClickListener;
    protected ListFilter listFilter;

    public interface OnItemClickListener<T> {
        void onItemClick(T item, View view);

        boolean onItemLongClick(T item, View view);
    }

    protected class ListFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults result = new FilterResults();
            List<T> resultList;
            if (TextUtils.isEmpty(constraint)) {
                resultList = list;
            } else {
                resultList = FilterableListAdapter.this.performFiltering(list, constraint);
            }
            result.values = resultList;
            result.count = resultList.size();
            return result;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<?> resultList = (List<?>) results.values;
            List<T> newList = new ArrayList<>();
            for (Object item : resultList) {
                newList.add((T) item);
            }
            submitList(newList);
        }
    }

    public FilterableListAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
    }

    public FilterableListAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback, List<T> list) {
        super(diffCallback);
        setList(list);
    }

    public void setList(List<T> list) {
        if (list == null) {
            submitList(new ArrayList<>());
            this.list = new ArrayList<>();
        } else {
            submitList(new ArrayList<>(list));
            this.list = new ArrayList<>(list);
        }
    }

    public void setOnItemClickListener(@NonNull OnItemClickListener<T> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public Filter getFilter() {
        if (listFilter == null) {
            listFilter = new ListFilter();
        }
        return listFilter;
    }

    @NonNull
    protected abstract List<T> performFiltering(List<T> list, CharSequence constraint);
}