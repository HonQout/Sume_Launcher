package com.qch.sumelauncher.ui.launcher.fragment.activitypicker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.qch.sumelauncher.R;
import com.qch.sumelauncher.data.model.launcher.ActivityModel;
import com.qch.sumelauncher.databinding.ActivityPickerBinding;
import com.qch.sumelauncher.recyclerview.adapter.AppGridAdapter;
import com.qch.sumelauncher.recyclerview.adapter.ClickableListAdapter;
import com.qch.sumelauncher.recyclerview.decoration.VerticalGridDecoration;
import com.qch.sumelauncher.ui.launcher.activity.LauncherViewModel;
import com.qch.sumelauncher.ui.settings.root.SettingsViewModel;
import com.qch.sumelauncher.utils.UIUtils;

import java.util.ArrayList;

public class ActivityPicker extends BottomSheetDialogFragment {
    private static final String TAG = "ActivityPicker";
    private ActivityPickerBinding binding;
    private LauncherViewModel launcherViewModel;
    private SettingsViewModel settingsViewModel;
    private OnActivitySelectedListener onActivitySelectedListener;

    public interface OnActivitySelectedListener {
        void onActivitySelected(ActivityModel activityModel);
    }

    private ActivityPicker(OnActivitySelectedListener onActivitySelectedListener) {
        this.onActivitySelectedListener = onActivitySelectedListener;
    }

    public static ActivityPicker newInstance(OnActivitySelectedListener onActivitySelectedListener) {
        return new ActivityPicker(onActivitySelectedListener);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launcherViewModel = new ViewModelProvider(requireActivity()).get(LauncherViewModel.class);
        settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ActivityPickerBinding.inflate(inflater, container, false);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),
                getResources().getInteger(R.integer.grid_column_count));
        binding.activityPickerDrawer.drawerRv.setLayoutManager(gridLayoutManager);

        AppGridAdapter appGridAdapter = new AppGridAdapter(new ArrayList<>());
        appGridAdapter.setOnItemClickListener(new ClickableListAdapter.OnItemClickListener<>() {
            @Override
            public void onItemClick(ActivityModel item, View view) {
                onActivitySelectedListener.onActivitySelected(item);
                dismiss();
            }

            @Override
            public boolean onItemLongClick(ActivityModel item, View view) {
                return false;
            }
        });
        binding.activityPickerDrawer.drawerRv.setAdapter(appGridAdapter);

        VerticalGridDecoration verticalGridDecoration = new VerticalGridDecoration(
                getResources().getInteger(R.integer.grid_column_count), 0, 0);
        binding.activityPickerDrawer.drawerRv.addItemDecoration(verticalGridDecoration);

        binding.activityPickerDrawer.drawerSv.setOnQueryTextFocusChangeListener((v, hasFocus) ->
                binding.activityPickerDrawer.drawerBtnQuit.setVisibility(View.VISIBLE));
        binding.activityPickerDrawer.drawerSv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                AppGridAdapter adapter = (AppGridAdapter) binding.activityPickerDrawer.drawerRv.getAdapter();
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                }
                return true;
            }
        });
        binding.activityPickerDrawer.drawerBtnQuit.setOnClickListener(v -> {
            binding.activityPickerDrawer.drawerSv.setQuery("", false);
            binding.activityPickerDrawer.drawerSv.clearFocus();
            binding.activityPickerDrawer.drawerBtnQuit.setVisibility(View.GONE);
        });

        launcherViewModel.getActivityModelList().observe(requireActivity(), appGridAdapter::setList);
        settingsViewModel.getDisplayStatusBar().observe(getViewLifecycleOwner(), display -> {
            if (display == null) {
                return;
            }
            if (getDialog() != null && getDialog().getWindow() != null) {
                UIUtils.forceHandleStatusBarVisibility(getDialog().getWindow(), display);
            }
        });

        return binding.getRoot();
    }
}