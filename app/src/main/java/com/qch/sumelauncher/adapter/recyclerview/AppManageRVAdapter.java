package com.qch.sumelauncher.adapter.recyclerview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.qch.sumelauncher.R;
import com.qch.sumelauncher.bean.ActivityBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AppManageRVAdapter extends FilterableListAdapter<ActivityBean, AppManageRVAdapter.ViewHolder> {
    private static final String TAG = "AppManageRVAdapter";
    private OnButtonPressedListener onButtonPressedListener;

    public interface OnButtonPressedListener {
        void onHideButtonPressed(ActivityBean item, View view);

        void onUninstallButtonPressed(ActivityBean item, View view);
    }

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
        private final TextView packageName;
        private final ImageButton btnHide;
        private final ImageButton btnUninstall;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.item_app_manage_icon);
            label = (TextView) itemView.findViewById(R.id.item_app_manage_label);
            packageName = (TextView) itemView.findViewById(R.id.item_app_manage_package_name);
            btnHide = (ImageButton) itemView.findViewById(R.id.item_app_manage_hide);
            btnUninstall = (ImageButton) itemView.findViewById(R.id.item_app_manage_uninstall);
            itemView.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    ActivityBean item = getItem(position);
                    onItemClickListener.onItemClick(item, view);
                }
            });
            itemView.setOnLongClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    ActivityBean item = getItem(position);
                    return onItemClickListener.onItemLongClick(item, view);
                }
                return false;
            });
            btnHide.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onButtonPressedListener != null) {
                    ActivityBean item = getItem(position);
                    onButtonPressedListener.onHideButtonPressed(item, view);
                }
            });
            btnUninstall.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onButtonPressedListener != null) {
                    ActivityBean item = getItem(position);
                    onButtonPressedListener.onUninstallButtonPressed(item, view);
                }
            });
        }

        public ImageView getIcon() {
            return icon;
        }

        public TextView getLabel() {
            return label;
        }

        public TextView getPackageName() {
            return packageName;
        }
    }

    public AppManageRVAdapter(List<ActivityBean> activityBeanList) {
        super(DIFF_CALLBACK, activityBeanList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app_manage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Context context = holder.itemView.getContext();
        ActivityBean activityBean = getItem(position);
        Drawable defIcon = context.getPackageManager().getDefaultActivityIcon();
        try {
            Glide.with(context)
                    .load(activityBean)
                    .placeholder(defIcon)
                    .error(defIcon)
                    .into(holder.icon);
        } catch (Exception e) {
            holder.getIcon().setImageDrawable(defIcon);
        }
        holder.getLabel().setText(activityBean.getLabel());
        holder.getPackageName().setText(activityBean.getPackageName());
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

    public void setOnButtonPressedListener(OnButtonPressedListener listener) {
        this.onButtonPressedListener = listener;
    }
}