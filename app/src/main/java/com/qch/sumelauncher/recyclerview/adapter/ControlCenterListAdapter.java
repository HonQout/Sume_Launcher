package com.qch.sumelauncher.recyclerview.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.qch.sumelauncher.data.model.controlcenter.ControlCenterItemModel;
import com.qch.sumelauncher.ui.launcher.fragment.controlcenter.view.ControlCenterShortcutView;

import java.util.List;
import java.util.Objects;

public class ControlCenterListAdapter extends ClickableListAdapter<ControlCenterItemModel, ControlCenterListAdapter.ViewHolder> {
    private static final String TAG = "ControlCenterListAdapter";

    public static final DiffUtil.ItemCallback<ControlCenterItemModel> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull ControlCenterItemModel oldItem, @NonNull ControlCenterItemModel newItem) {
            return Objects.equals(oldItem, newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull ControlCenterItemModel oldItem, @NonNull ControlCenterItemModel newItem) {
            boolean isIconTheSame = Objects.equals(oldItem.getIconRes(), newItem.getIconRes());
            boolean isTitleTheSame = Objects.equals(oldItem.getTitleRes(), newItem.getTitleRes());
            return isIconTheSame && isTitleTheSame;
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ControlCenterShortcutView controlCenterShortcutView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            controlCenterShortcutView = (ControlCenterShortcutView) itemView;
            controlCenterShortcutView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            controlCenterShortcutView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    ControlCenterItemModel model = getItem(position);
                    onItemClickListener.onItemClick(model, v);
                }
            });
            controlCenterShortcutView.setOnLongClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    ControlCenterItemModel model = getItem(position);
                    onItemClickListener.onItemLongClick(model, v);
                }
                return false;
            });
        }
    }

    public ControlCenterListAdapter(List<ControlCenterItemModel> list) {
        super(DIFF_CALLBACK, list);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ControlCenterShortcutView view = new ControlCenterShortcutView(parent.getContext());
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ControlCenterItemModel model = getItem(position);
        holder.controlCenterShortcutView.setModel(model);
    }
}
