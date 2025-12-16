package com.qch.sumelauncher.adapter.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.bean.PermissionBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PermissionRVAdapter extends FilterableListAdapter<PermissionBean, PermissionRVAdapter.ViewHolder> implements Filterable {
    private static final String TAG = "PermissionRVAdapter";

    public static final DiffUtil.ItemCallback<PermissionBean> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull PermissionBean oldItem, @NonNull PermissionBean newItem) {
            boolean isNameTheSame = Objects.equals(oldItem.getName(), newItem.getName());
            boolean isLabelTheSame = Objects.equals(oldItem.getLabel(), newItem.getLabel());
            return isNameTheSame && isLabelTheSame;
        }

        @Override
        public boolean areContentsTheSame(@NonNull PermissionBean oldItem, @NonNull PermissionBean newItem) {
            boolean isPermissionInfoTheSame = oldItem.getPermissionInfo() == newItem.getPermissionInfo();
            return isPermissionInfoTheSame;
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView content;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.item_tc_v_title);
            content = (TextView) itemView.findViewById(R.id.item_tc_v_content);
            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    PermissionBean item = getItem(position);
                    onItemClickListener.onItemClick(item, v);
                }
            });
            itemView.setOnLongClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    PermissionBean item = getItem(position);
                    return onItemClickListener.onItemLongClick(item, v);
                }
                return false;
            });
        }

        public TextView getTitle() {
            return title;
        }

        public TextView getContent() {
            return content;
        }
    }

    public PermissionRVAdapter(List<PermissionBean> permissionBeanList) {
        super(DIFF_CALLBACK, permissionBeanList);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tc_v, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PermissionBean permissionBean = getItem(position);
        holder.getTitle().setText(permissionBean.getName());
        holder.getContent().setText(permissionBean.getLabel());
    }

    @NonNull
    @Override
    protected List<PermissionBean> performFiltering(List<PermissionBean> list, CharSequence constraint) {
        List<PermissionBean> resultList = new ArrayList<>();
        CharSequence cs = constraint.toString().toLowerCase(Locale.ROOT);
        for (PermissionBean item : list) {
            boolean isNameMatch = item.getName().toLowerCase(Locale.ROOT).contains(cs);
            boolean isLabelMatch = item.getLabel().toLowerCase(Locale.ROOT).contains(cs);
            if (isNameMatch || isLabelMatch) {
                resultList.add(item);
            }
        }
        return resultList;
    }
}