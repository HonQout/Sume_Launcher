package com.qch.sumelauncher.fragment;

import android.content.pm.ShortcutInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
                popupMenu.setForceShowIcon(true);
                int baseIndex = popupMenu.getMenu().size();
                List<ShortcutInfo> shortcutInfoList = item.getShortcutInfoList();
                for (int i = 0; i < shortcutInfoList.size(); i++) {
                    ShortcutInfo shortcutInfo = shortcutInfoList.get(i);
                    popupMenu.getMenu().add(0, baseIndex + i, baseIndex + i,
                            shortcutInfo.getShortLabel());
                }
                popupMenu.setOnMenuItemClickListener(menuItem -> {
                    int menuId = menuItem.getItemId();
                    if (menuId == R.id.app_info) {
                        boolean result = IntentUtils.openAppDetailsPage(requireContext(), item.getPackageName());
                        if (!result) {
                            Toast.makeText(requireContext(), R.string.open_app_info_failed,
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                        return true;
                    } else if (menuId == R.id.uninstall) {
                        ApplicationUtils.ApplicationType type = ApplicationUtils.getApplicationType(
                                requireContext(), item.getPackageName());
                        if (type == ApplicationUtils.ApplicationType.UPDATED_SYSTEM
                                || type == ApplicationUtils.ApplicationType.USER) {
                            boolean result = IntentUtils.requireUninstallApp(
                                    requireContext(), item.getPackageName());
                            if (!result) {
                                Toast.makeText(requireContext(), R.string.uninstall_app_failed,
                                                Toast.LENGTH_SHORT)
                                        .show();
                            }
                        } else {
                            Toast.makeText(requireContext(), R.string.cannot_uninstall_app,
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                        return true;
                    } else if (menuId == R.id.app_market) {
                        IntentUtils.openAppInMarket(requireContext(), item.getPackageName());
                        return true;
                    } else if (menuId >= baseIndex) {
                        ShortcutInfo shortcutInfo = shortcutInfoList.get(menuId - baseIndex);
                        ApplicationUtils.launchAppShortcut(requireContext(), item.getPackageName(),
                                shortcutInfo.getId());
                        return true;
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
        viewModel.getDisplayTopBar().observe(getViewLifecycleOwner(), displayTopBar -> {
            GridLayoutManager layoutManager = (GridLayoutManager) binding.fAppGridRv.getLayoutManager();
            if (layoutManager != null) {
                layoutManager.setSpanCount(viewModel.getNumColumnInt());
            }
            try {
                int decorCount = binding.fAppGridRv.getItemDecorationCount();
                for (int i = 0; i < decorCount; i++) {
                    binding.fAppGridRv.removeItemDecorationAt(0);
                }
            } catch (Exception e) {
                Log.e(TAG, "RecyclerView doesn't have an ItemDecoration.");
            }
            binding.fAppGridRv.addItemDecoration(
                    new AppItemDecoration(
                            viewModel.getNumRowInt(),
                            viewModel.getNumColumnInt()
                    )
            );
        });
        viewModel.getActivityBeanMap().observe(getViewLifecycleOwner(), map -> {
            if (map != null) {
                List<ActivityBean> list = map.get(position);
                if (list != null) {
                    appRVAdapter.setList(list);
                    GridLayoutManager layoutManager = (GridLayoutManager) binding.fAppGridRv.getLayoutManager();
                    if (layoutManager != null) {
                        layoutManager.setSpanCount(viewModel.getNumColumnInt());
                    }
                    try {
                        int decorCount = binding.fAppGridRv.getItemDecorationCount();
                        for (int i = 0; i < decorCount; i++) {
                            binding.fAppGridRv.removeItemDecorationAt(0);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "RecyclerView doesn't have an ItemDecoration.");
                    }
                    binding.fAppGridRv.addItemDecoration(
                            new AppItemDecoration(
                                    viewModel.getNumRowInt(),
                                    viewModel.getNumColumnInt()
                            )
                    );
                }
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "Fragment #" + position + " onViewCreated");
        super.onViewCreated(view, savedInstanceState);
    }
}