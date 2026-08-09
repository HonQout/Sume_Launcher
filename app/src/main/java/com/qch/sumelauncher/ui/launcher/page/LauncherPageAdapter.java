package com.qch.sumelauncher.ui.launcher.page;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.data.model.launcher.GridSize;
import com.qch.sumelauncher.data.model.launcher.IconModel;
import com.qch.sumelauncher.data.model.launcher.PageWithIconModels;

import java.util.ArrayList;
import java.util.List;

public class LauncherPageAdapter extends RecyclerView.Adapter<LauncherPageAdapter.PageViewHolder> {
    private static final String TAG = "LauncherPageAdapter";
    private GridSize gridSize;
    private List<PageWithIconModels> list = new ArrayList<>();
    private LauncherLayout.OnIconClickListener onIconClickListener;
    private LauncherLayout.OnBlankAreaClickListener onBlankAreaClickListener;

    public static class PageViewHolder extends RecyclerView.ViewHolder {
        private final LauncherLayout launcherLayout;

        public PageViewHolder(@NonNull View itemView) {
            super(itemView);
            launcherLayout = itemView.findViewById(R.id.launcher_page_ll);
        }

        public void bindData(GridSize gridSize,
                             List<IconModel> list,
                             LauncherLayout.OnIconClickListener onIconClickListener,
                             LauncherLayout.OnBlankAreaClickListener onBlankAreaClickListener) {
            // Clear all child views
            launcherLayout.removeAllViews();
            // Check if any of the given arguments is null
            if (gridSize == null) {
                Log.e(TAG, "Failed to bind data. Grid size is null.");
                return;
            }
            if (list == null) {
                Log.e(TAG, "Failed to bind data. List of IconModel is null.");
                return;
            }
            if (onIconClickListener == null) {
                Log.e(TAG, "Failed to bind data. OnIconClickListener is null.");
                return;
            }
            if (onBlankAreaClickListener == null) {
                Log.e(TAG, "Failed to bind data. OnBlankAreaClickListener is null.");
            }
            // Set arguments
            launcherLayout.setNumColumns(gridSize.getColumn());
            launcherLayout.setNumRows(gridSize.getRow());
            launcherLayout.setItems(list);
            launcherLayout.setOnIconClickListener(onIconClickListener);
            launcherLayout.setOnBlankClickListener(onBlankAreaClickListener);
        }
    }

    @NonNull
    @Override
    public LauncherPageAdapter.PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.launcher_page, parent, false);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(lp);
        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LauncherPageAdapter.PageViewHolder holder, int position) {
        Log.i(TAG, "Size of list of PageWithIconModels is " + list.size());
        holder.bindData(gridSize, list.get(position).getList(), onIconClickListener, onBlankAreaClickListener);
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setGridSize(GridSize gridSize) {
        if (this.gridSize != gridSize) {
            this.gridSize = gridSize;
            notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setList(List<PageWithIconModels> list) {
        if (this.list != list) {
            this.list = list;
            notifyDataSetChanged();
        }
    }

    public void setOnIconClickListener(LauncherLayout.OnIconClickListener listener) {
        this.onIconClickListener = listener;
    }

    public void setOnBlankAreaClickListener(LauncherLayout.OnBlankAreaClickListener listener) {
        this.onBlankAreaClickListener = listener;
    }
}