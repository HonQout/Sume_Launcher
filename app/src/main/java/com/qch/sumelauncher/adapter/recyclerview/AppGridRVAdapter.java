package com.qch.sumelauncher.adapter.recyclerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.qch.sumelauncher.R;
import com.qch.sumelauncher.bean.ActivityBean;
import com.qch.sumelauncher.utils.DrawableUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AppGridRVAdapter extends FilterableListAdapter<ActivityBean, AppGridRVAdapter.ViewHolder> {
    private static final String TAG = "AppRVAdapter";

    public static final DiffUtil.ItemCallback<ActivityBean> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull ActivityBean oldItem, @NonNull ActivityBean newItem) {
            boolean isPackageNameTheSame = Objects.equals(oldItem.getPackageName(), newItem.getActivityName());
            boolean isActivityNameTheSame = Objects.equals(oldItem.getActivityName(), newItem.getActivityName());
            return isPackageNameTheSame && isActivityNameTheSame;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ActivityBean oldItem, @NonNull ActivityBean newItem) {
            boolean isIconTheSame = Objects.equals(oldItem.getIconRes(), newItem.getIconRes());
            boolean isLabelTheSame = Objects.equals(oldItem.getLabel(), newItem.getLabel());
            return isIconTheSame && isLabelTheSame;
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView icon;
        private final TextView label;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.item_app_grid_icon);
            label = (TextView) itemView.findViewById(R.id.item_app_grid_label);
            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    ActivityBean item = getItem(position);
                    onItemClickListener.onItemClick(item, v);
                }
            });
            itemView.setOnLongClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    ActivityBean item = getItem(position);
                    return onItemClickListener.onItemLongClick(item, v);
                }
                return false;
            });
        }

        public ImageView getIcon() {
            return icon;
        }

        public TextView getLabel() {
            return label;
        }
    }

    public AppGridRVAdapter(List<ActivityBean> activityBeanList) {
        super(DIFF_CALLBACK, activityBeanList);
    }

    @NonNull
    @Override
    public AppGridRVAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppGridRVAdapter.ViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        ActivityBean activityBean = getItem(position);
        Drawable defIconDrawable = context.getPackageManager().getDefaultActivityIcon();
        Bitmap defIconBitmap = DrawableUtils.toBitmap(defIconDrawable);
        try {
            Glide.with(context)
                    .asBitmap()
                    .load(activityBean)
                    .placeholder(defIconDrawable)
                    .error(defIconDrawable)
                    .into(holder.icon);
        } catch (Exception e) {
            holder.getIcon().setImageBitmap(defIconBitmap);
        }
        holder.getLabel().setText(activityBean.getLabel());
    }

    @NonNull
    @Override
    protected List<ActivityBean> performFiltering(List<ActivityBean> list, CharSequence constraint) {
        List<ActivityBean> resultList = new ArrayList<>();
        CharSequence cs = constraint.toString().toLowerCase(Locale.ROOT);
        for (ActivityBean item : list) {
            boolean isLabelMatch = item.getLabel().toLowerCase(Locale.ROOT).contains(cs);
            if (isLabelMatch) {
                resultList.add(item);
            }
        }
        return resultList;
    }

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.app_op_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    }
}