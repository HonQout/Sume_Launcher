package com.qch.sumelauncher.recyclerview.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.qch.sumelauncher.data.model.launcher.ActivityModel;
import com.qch.sumelauncher.ui.launcher.item.IconView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AppGridAdapter extends FilterableListAdapter<ActivityModel, AppGridAdapter.ViewHolder> {
    private static final String TAG = "AppGridAdapter";

    private static final DiffUtil.ItemCallback<ActivityModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull ActivityModel oldItem, @NonNull ActivityModel newItem) {
            return Objects.equals(oldItem, newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull ActivityModel oldItem, @NonNull ActivityModel newItem) {
            boolean isIconTheSame = Objects.equals(oldItem.getIconRes(), newItem.getIconRes());
            boolean isLabelTheSame = Objects.equals(oldItem.getLabel(), newItem.getLabel());
            return isIconTheSame && isLabelTheSame;
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final IconView iconView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconView = (IconView) itemView;
            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    ActivityModel item = getItem(position);
                    onItemClickListener.onItemClick(item, v);
                }
            });
            itemView.setOnLongClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    ActivityModel item = getItem(position);
                    return onItemClickListener.onItemLongClick(item, v);
                }
                return false;
            });
        }
    }

    public AppGridAdapter(List<ActivityModel> activityModelList) {
        super(DIFF_CALLBACK, activityModelList);
    }

    @NonNull
    @Override
    public AppGridAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        IconView iconView = new IconView(parent.getContext());
        return new ViewHolder(iconView);
    }

    @Override
    public void onBindViewHolder(@NonNull AppGridAdapter.ViewHolder holder, int position) {
        ActivityModel activityModel = getItem(position);
        holder.iconView.setActivityModel(activityModel);
    }

    @NonNull
    @Override
    protected List<ActivityModel> performFiltering(List<ActivityModel> list, CharSequence constraint) {
        List<ActivityModel> resultList = new ArrayList<>();
        CharSequence cs = constraint.toString().toLowerCase(Locale.ROOT);
        for (ActivityModel item : list) {
            boolean isLabelMatch = item.getLabel().toLowerCase(Locale.ROOT).contains(cs);
            if (isLabelMatch) {
                resultList.add(item);
            }
        }
        return resultList;
    }
}