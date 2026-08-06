package com.qch.sumelauncher.ui.launcher.fragment.controlcenter;

import android.app.admin.DevicePolicyManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.qch.sumelauncher.R;
import com.qch.sumelauncher.br.MyDeviceAdminReceiver;
import com.qch.sumelauncher.data.model.controlcenter.ActivityShortcutModel;
import com.qch.sumelauncher.data.model.controlcenter.ControlCenterItemModel;
import com.qch.sumelauncher.data.model.controlcenter.SettingsShortcutModel;
import com.qch.sumelauncher.databinding.ControlCenterBinding;
import com.qch.sumelauncher.recyclerview.adapter.ClickableListAdapter;
import com.qch.sumelauncher.recyclerview.adapter.ControlCenterListAdapter;
import com.qch.sumelauncher.recyclerview.decoration.VerticalGridDecoration;
import com.qch.sumelauncher.ui.settings.root.SettingsViewModel;
import com.qch.sumelauncher.utils.DeviceAdminUtils;
import com.qch.sumelauncher.utils.UIUtils;

import java.util.ArrayList;
import java.util.Objects;

public class ControlCenter extends BottomSheetDialogFragment {
    private static final String TAG = "ControlCenter";
    private ControlCenterBinding binding;
    private ControlCenterViewModel controlCenterViewModel;
    private SettingsViewModel settingsViewModel;

    public static ControlCenter newInstance() {
        return new ControlCenter();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        controlCenterViewModel = new ViewModelProvider(requireActivity()).get(ControlCenterViewModel.class);
        settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ControlCenterBinding.inflate(inflater, container, false);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),
                getResources().getInteger(R.integer.control_center_column_count));
        binding.controlCenterRv.setLayoutManager(gridLayoutManager);

        ControlCenterListAdapter adapter = new ControlCenterListAdapter(new ArrayList<>());
        adapter.setOnItemClickListener(new ClickableListAdapter.OnItemClickListener<>() {
            @Override
            public void onItemClick(ControlCenterItemModel item, View view) {
                if (item instanceof SettingsShortcutModel) {
                    SettingsShortcutModel settingsShortcutModel = (SettingsShortcutModel) item;
                    Intent intent = new Intent(settingsShortcutModel.getSettingsAction());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        view.getContext().startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Log.e(TAG, "Failed to start activity. Cannot find required activity.", e);
                    }
                } else if (item instanceof ActivityShortcutModel) {
                    ActivityShortcutModel activityShortcutModel = (ActivityShortcutModel) item;
                    Intent intent = new Intent(view.getContext().getApplicationContext(), activityShortcutModel.getCls());
                    try {
                        view.getContext().startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Log.e(TAG, "Failed to start activity. Cannot find required activity.", e);
                    }
                } else if (Objects.equals(item.getTag(), "LOCK_SCREEN")) {
                    DevicePolicyManager manager = DeviceAdminUtils.getDevicePolicyManager(requireContext());
                    ComponentName adminComponent = new ComponentName(requireContext(), MyDeviceAdminReceiver.class);
                    if (manager.isAdminActive(adminComponent)) {
                        manager.lockNow();
                    } else {
                        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
                        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                ContextCompat.getString(requireContext(), R.string.add_device_admin_reason));
                        startActivity(intent);
                    }
                }
                dismiss();
            }

            @Override
            public boolean onItemLongClick(ControlCenterItemModel item, View view) {
                return false;
            }
        });
        binding.controlCenterRv.setAdapter(adapter);

        VerticalGridDecoration verticalGridDecoration = new VerticalGridDecoration(
                getResources().getInteger(R.integer.control_center_column_count), 20, 20
        );
        binding.controlCenterRv.addItemDecoration(verticalGridDecoration);

        controlCenterViewModel.getItemList().observe(getViewLifecycleOwner(), adapter::setList);
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