package com.qch.sumelauncher.fragment;

import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.qch.sumelauncher.R;
import com.qch.sumelauncher.adapter.recyclerview.AppRVAdapter;
import com.qch.sumelauncher.adapter.recyclerview.FilterableListAdapter;
import com.qch.sumelauncher.bean.ActivityBean;
import com.qch.sumelauncher.databinding.FragmentAppGridBinding;
import com.qch.sumelauncher.recyclerview.AppItemDecoration;
import com.qch.sumelauncher.utils.ApplicationUtils;
import com.qch.sumelauncher.utils.IntentUtils;
import com.qch.sumelauncher.viewmodel.AppViewModel;

import java.util.ArrayList;
import java.util.List;

public class AppGridFragment extends Fragment {
    private static final String TAG = "AppGridFragment";
    private static final String ARG_POSITION = "POSITION";
    private FragmentAppGridBinding binding;
    private int position;
    private AppViewModel viewModel;

    public AppGridFragment() {
        // Required empty public constructor
    }

    public static AppGridFragment newInstance(int position) {
        AppGridFragment fragment = new AppGridFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            position = args.getInt(ARG_POSITION);
        }
        viewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "Fragment #" + position + " onCreateView");
        binding = FragmentAppGridBinding.inflate(inflater, container, false);
        binding.fAppGridRv.setLayoutManager(new GridLayoutManager(requireContext(), viewModel.getNumColumnInt()));
        AppRVAdapter appRVAdapter = new AppRVAdapter(new ArrayList<>());
        appRVAdapter.setOnItemClickListener(new FilterableListAdapter.OnItemClickListener<>() {
            @Override
            public void onItemClick(ActivityBean item, View view) {
                IntentUtils.LaunchActivityResult result =
                        IntentUtils.launchActivity(requireContext(), item.getPackageName(),
                                item.getActivityName(), true);
                switch (result) {
                    case NOT_EXPORTED: {
                        Toast.makeText(requireContext(), R.string.cannot_access_unexported_activity, Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case REQUIRE_PERMISSION: {
                        Toast.makeText(requireContext(), R.string.activity_requires_extra_permission, Toast.LENGTH_SHORT).show();
                        break;
                    }
                    case NOT_FOUND: {
                        Toast.makeText(requireContext(), R.string.cannot_find_activity, Toast.LENGTH_SHORT).show();
                        break;
                    }
                    default:
                        break;
                }
            }

            @Override
            public boolean onItemLongClick(ActivityBean item, View view) {
                PopupMenu popupMenu = new PopupMenu(requireActivity(), view);
                popupMenu.getMenuInflater().inflate(R.menu.app_op_menu, popupMenu.getMenu());
                int baseIndex = popupMenu.getMenu().size();
                List<ShortcutInfo> shortcutInfoList;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    shortcutInfoList = item.getShortcutInfoList();
                    for (int i = 0; i < shortcutInfoList.size(); i++) {
                        ShortcutInfo shortcutInfo = shortcutInfoList.get(i);
                        popupMenu.getMenu()
                                .add(0, baseIndex + i, baseIndex + i, shortcutInfo.getShortLabel());
                    }
                } else {
                    shortcutInfoList = new ArrayList<>();
                }
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    int menuId = menuItem.getItemId();
                    if (menuId == R.id.app_info) {
                        IntentUtils.handleLaunchIntentResult(
                                requireContext(),
                                IntentUtils.openAppDetailsPage(requireContext(), item.getPackageName())
                        );
                        return true;
                    } else if (menuId == R.id.uninstall) {
                        ApplicationUtils.ApplicationType type = ApplicationUtils.getApplicationType(
                                requireContext(), item.getPackageName());
                        if (type == ApplicationUtils.ApplicationType.UPDATED_SYSTEM
                                || type == ApplicationUtils.ApplicationType.USER) {
                            IntentUtils.handleLaunchIntentResult(
                                    requireContext(),
                                    IntentUtils.requireUninstallApp(requireContext(), item.getPackageName())
                            );
                        } else if (type == ApplicationUtils.ApplicationType.SYSTEM) {
                            new MaterialAlertDialogBuilder(requireContext())
                                    .setTitle(R.string.hint)
                                    .setMessage(R.string.insist_uninstall_system_app)
                                    .setPositiveButton(R.string.uninstall, (dialog, which) ->
                                            IntentUtils.handleLaunchIntentResult(
                                                    requireContext(),
                                                    IntentUtils.requireUninstallApp(requireContext(), item.getPackageName())
                                            ))
                                    .setNegativeButton(R.string.cancel, null)
                                    .show();
                        } else {
                            Toast.makeText(requireContext(), R.string.cannot_uninstall_app,
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                        return true;
                    } else if (menuId == R.id.app_market) {
                        IntentUtils.handleLaunchIntentResult(
                                requireContext(),
                                IntentUtils.openAppInMarket(requireContext(), item.getPackageName())
                        );
                        return true;
                    } else if (menuId >= baseIndex) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                            ShortcutInfo shortcutInfo = shortcutInfoList.get(menuId - baseIndex);
                            ApplicationUtils.launchAppShortcut(requireContext(), item.getPackageName(),
                                    shortcutInfo.getId());
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                });
                popupMenu.show();
                return true;
            }
        });
        binding.fAppGridRv.setAdapter(appRVAdapter);
        binding.fAppGridRv.addItemDecoration(
                new AppItemDecoration(
                        viewModel.getNumRowInt(),
                        viewModel.getNumColumnInt()
                )
        );
        viewModel.getDisplayStatusBar().observe(getViewLifecycleOwner(), displayStatusBar ->
                reLayoutAppGrid()
        );
        viewModel.getDisplayTopBar().observe(getViewLifecycleOwner(), displayTopBar ->
                reLayoutAppGrid()
        );
        viewModel.getEdgeToEdge().observe(getViewLifecycleOwner(), edgeToEdge ->
                reLayoutAppGrid()
        );
        viewModel.getActivityBeanMap().observe(getViewLifecycleOwner(), map -> {
            if (map != null) {
                List<ActivityBean> list = map.get(position);
                if (list != null) {
                    appRVAdapter.setList(list);
                    reLayoutAppGrid();
                }
            }
        });
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(),
                (v, insets) -> {
                    binding.fAppGridRv.invalidateItemDecorations();
                    return insets;
                });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "Fragment #" + position + " onViewCreated");
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        Log.i(TAG, "Fragment #" + position + " onStart");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "Fragment #" + position + " onResume");
        super.onResume();
    }

    private void reLayoutAppGrid() {
        Log.i(TAG, "Prepare to re-layout app grid.");
        GridLayoutManager layoutManager = (GridLayoutManager) binding.fAppGridRv.getLayoutManager();
        if (layoutManager != null) {
            layoutManager.setSpanCount(viewModel.getNumColumnInt());
        }
        int decorCount = binding.fAppGridRv.getItemDecorationCount();
        try {
            for (int i = 0; i < decorCount; i++) {
                binding.fAppGridRv.removeItemDecorationAt(0);
            }
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "Required index of ItemDecoration is out of bound.");
        }
        binding.fAppGridRv.addItemDecoration(
                new AppItemDecoration(
                        viewModel.getNumRowInt(),
                        viewModel.getNumColumnInt()
                )
        );
        if (!binding.fAppGridRv.isInLayout()) {
            binding.fAppGridRv.requestLayout();
        }
    }
}