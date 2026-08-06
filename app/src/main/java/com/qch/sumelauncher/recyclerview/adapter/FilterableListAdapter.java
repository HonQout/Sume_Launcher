package com.qch.sumelauncher.recyclerview.adapter;

import android.text.TextUtils;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class FilterableListAdapter<T, VH extends RecyclerView.ViewHolder>
        extends ClickableListAdapter<T, VH>
        implements Filterable {
    protected ListFilter listFilter;

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
        super(diffCallback, list);
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