package com.qch.sumelauncher.ui.recyclerview.adapter;

import android.text.TextUtils;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public abstract class FilterableClickableListAdapter<T, VH extends RecyclerView.ViewHolder>
        extends BaseListAdapter<T, VH>
        implements Filterable {
    protected ListFilter listFilter;
    protected OnItemClickListener<T> onItemClickListener;

    protected class ListFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<T> resultList;
            if (TextUtils.isEmpty(constraint)) {
                resultList = list;
            } else {
                resultList = filterList(list, constraint);
            }
            final FilterResults result = new FilterResults();
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

    public FilterableClickableListAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
    }

    public FilterableClickableListAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback,
                                          List<T> list) {
        super(diffCallback, list);
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
    protected abstract List<T> filterList(List<T> list, CharSequence constraint);
}