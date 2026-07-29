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

public class GridDrawerRVAdapter extends FilterableListAdapter<ActivityModel, GridDrawerRVAdapter.ViewHolder> {
    private static final String TAG = "GridDrawerRVAdapter";

    public static final DiffUtil.ItemCallback<ActivityModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull ActivityModel oldItem, @NonNull ActivityModel newItem) {
            boolean isPackageNameTheSame = Objects.equals(oldItem.getPackageName(), newItem.getActivityName());
            boolean isActivityNameTheSame = Objects.equals(oldItem.getActivityName(), newItem.getActivityName());
            return isPackageNameTheSame && isActivityNameTheSame;
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

    public GridDrawerRVAdapter(List<ActivityModel> activityModelList) {
        super(DIFF_CALLBACK, activityModelList);
    }

    @NonNull
    @Override
    public GridDrawerRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        IconView iconView = new IconView(parent.getContext());
        return new ViewHolder(iconView);
    }

    @Override
    public void onBindViewHolder(@NonNull GridDrawerRVAdapter.ViewHolder holder, int position) {
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