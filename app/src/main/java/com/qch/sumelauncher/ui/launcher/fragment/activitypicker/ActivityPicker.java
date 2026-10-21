package com.qch.sumelauncher.ui.launcher.fragment.activitypicker;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.data.model.launcher.ActivityModel;
import com.qch.sumelauncher.databinding.ActivityPickerBinding;
import com.qch.sumelauncher.ui.recyclerview.adapter.AppGridAdapter;
import com.qch.sumelauncher.ui.recyclerview.adapter.OnItemClickListener;
import com.qch.sumelauncher.ui.recyclerview.decoration.VerticalGridDecoration;
import com.qch.sumelauncher.ui.launcher.activity.LauncherViewModel;
import com.qch.sumelauncher.ui.settings.root.SettingsViewModel;
import com.qch.sumelauncher.utils.UIUtils;

import java.util.ArrayList;

public class ActivityPicker extends DialogFragment {
    private static final String TAG = "ActivityPicker";
    private ActivityPickerBinding binding;
    private LauncherViewModel launcherViewModel;
    private SettingsViewModel settingsViewModel;
    private OnActivitySelectedListener onActivitySelectedListener;

    public interface OnActivitySelectedListener {
        void onActivitySelected(ActivityModel activityModel);
    }

    // Require empty constructor.
    public ActivityPicker() {

    }

    public ActivityPicker(OnActivitySelectedListener onActivitySelectedListener) {
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

        binding.activityPickerToolbar.setNavigationOnClickListener(v -> dismiss());

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),
                getResources().getInteger(R.integer.drawer_column_count));
        binding.activityPickerDrawer.drawerRv.setLayoutManager(gridLayoutManager);

        AppGridAdapter appGridAdapter = new AppGridAdapter(new ArrayList<>());
        appGridAdapter.setOnItemClickListener(new OnItemClickListener<>() {
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
                getResources().getInteger(R.integer.drawer_column_count), 0, 0);
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
                    return true;
                }
                return false;
            }
        });
        binding.activityPickerDrawer.drawerBtnQuit.setOnClickListener(v -> {
            binding.activityPickerDrawer.drawerSv.setQuery("", false);
            binding.activityPickerDrawer.drawerSv.clearFocus();
            binding.activityPickerDrawer.drawerBtnQuit.setVisibility(View.GONE);
        });

        launcherViewModel.getActivityModelList().observe(requireActivity(), activityModels -> {
            appGridAdapter.setList(activityModels);
            appGridAdapter.getFilter().filter(binding.activityPickerDrawer.drawerSv.getQuery());
        });
        launcherViewModel.getDrawerColumnCount().observe(requireActivity(), integer -> {
            gridLayoutManager.setSpanCount(integer);
            verticalGridDecoration.setSpanCount(integer);
        });
        settingsViewModel.getDisplayStatusBar().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean == null) {
                return;
            }
            if (getDialog() != null && getDialog().getWindow() != null) {
                UIUtils.forceHandleStatusBarVisibility(getDialog().getWindow(), aBoolean);
            }
        });
        settingsViewModel.getForceToApplyBgColor().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean == null) {
                return;
            }
            if (getDialog() != null && getDialog().getWindow() != null) {
                binding.activityPickerRoot.setBackgroundColor(
                        getResources()
                                .getColor(aBoolean ? R.color.windowBackgroundColor : R.color.transparent, null)
                );
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
        }
    }
}