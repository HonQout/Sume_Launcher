package com.qch.sumelauncher.fragment;

import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.adapter.recyclerview.AppGridRVAdapter;
import com.qch.sumelauncher.adapter.recyclerview.AppListRVAdapter;
import com.qch.sumelauncher.adapter.recyclerview.FilterableListAdapter;
import com.qch.sumelauncher.bean.ActivityBean;
import com.qch.sumelauncher.databinding.FragmentDrawerBinding;
import com.qch.sumelauncher.recyclerview.GridDecoration;
import com.qch.sumelauncher.utils.ApplicationUtils;
import com.qch.sumelauncher.utils.DialogUtils;
import com.qch.sumelauncher.utils.IntentUtils;
import com.qch.sumelauncher.viewmodel.LauncherViewModel;

import java.util.ArrayList;
import java.util.List;

public class DrawerFragment extends Fragment {
    private static final String TAG = "DrawerFragment";
    private FragmentDrawerBinding binding;
    private LauncherViewModel viewModel;

    public DrawerFragment() {
        // Required empty public constructor
    }

    public static DrawerFragment newInstance() {
        DrawerFragment fragment = new DrawerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(LauncherViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDrawerBinding.inflate(inflater, container, false);
        binding.fDrawerBtnGrid.setOnClickListener(v -> setGridRVAdapter());
        binding.fDrawerBtnList.setOnClickListener(v -> setListRVAdapter());
        binding.fDrawerSv.setOnSearchClickListener(v ->
                binding.fDrawerLl.setVisibility(View.GONE));
        binding.fDrawerSv.setOnCloseListener(() -> {
            binding.fDrawerLl.setVisibility(View.VISIBLE);
            return false;
        });
        setGridRVAdapter();
        return binding.getRoot();
    }

    private void setGridRVAdapter() {
        binding.fDrawerRv.setLayoutManager(new GridLayoutManager(requireActivity(),
                viewModel.getNumColumnValue()));
        AppGridRVAdapter appGridRVAdapter = new AppGridRVAdapter(new ArrayList<>());
        appGridRVAdapter.setOnItemClickListener(new FilterableListAdapter.OnItemClickListener<>() {
            @Override
            public void onItemClick(ActivityBean item, View view) {
                IntentUtils.handleLaunchActivityResult(requireActivity(),
                        IntentUtils.launchActivity(requireActivity(),
                                item.getPackageName(), item.getActivityName(), true));
            }

            @Override
            public boolean onItemLongClick(ActivityBean item, View view) {
                showGridMenu(view, item);
                return true;
            }
        });
        binding.fDrawerRv.setAdapter(appGridRVAdapter);
        int numItemDecorations = binding.fDrawerRv.getItemDecorationCount();
        for (int i = 0; i < numItemDecorations; i++) {
            binding.fDrawerRv.removeItemDecorationAt(0);
        }
        binding.fDrawerRv.addItemDecoration(new GridDecoration(
                viewModel.getNumColumnValue(),
                getResources().getDimensionPixelSize(R.dimen.app_grid_space)
        ));
        binding.fDrawerSv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                AppGridRVAdapter adapter = (AppGridRVAdapter) binding.fDrawerRv.getAdapter();
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                }
                return true;
            }
        });
        viewModel.getActivityBeanList().observe(getViewLifecycleOwner(), appGridRVAdapter::setList);
    }

    private void setListRVAdapter() {
        binding.fDrawerRv.setLayoutManager(new LinearLayoutManager(requireActivity(),
                RecyclerView.VERTICAL, false));
        AppListRVAdapter appListRVAdapter = new AppListRVAdapter(new ArrayList<>());
        appListRVAdapter.setOnItemClickListener(new FilterableListAdapter.OnItemClickListener<>() {
            @Override
            public void onItemClick(ActivityBean item, View view) {
                IntentUtils.handleLaunchActivityResult(requireActivity(),
                        IntentUtils.launchActivity(requireActivity(),
                                item.getPackageName(), item.getActivityName(), true));
            }

            @Override
            public boolean onItemLongClick(ActivityBean item, View view) {
                return false;
            }
        });
        appListRVAdapter.setOnButtonPressedListener((item, view) ->
                showGridMenu(view, item));
        binding.fDrawerRv.setAdapter(appListRVAdapter);
        int numItemDecorations = binding.fDrawerRv.getItemDecorationCount();
        for (int i = 0; i < numItemDecorations; i++) {
            binding.fDrawerRv.removeItemDecorationAt(0);
        }
        binding.fDrawerSv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                AppListRVAdapter adapter = (AppListRVAdapter) binding.fDrawerRv.getAdapter();
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                }
                return true;
            }
        });
        viewModel.getActivityBeanList().observe(getViewLifecycleOwner(), appListRVAdapter::setList);
    }

    private void showGridMenu(@NonNull View view, @NonNull ActivityBean item) {
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
                        requireActivity(),
                        IntentUtils.openAppDetailsPage(requireActivity(), item.getPackageName())
                );
                return true;
            } else if (menuId == R.id.uninstall) {
                ApplicationUtils.ApplicationType type = ApplicationUtils.getApplicationType(
                        requireActivity(), item.getPackageName());
                if (type == ApplicationUtils.ApplicationType.UPDATED_SYSTEM
                        || type == ApplicationUtils.ApplicationType.USER) {
                    IntentUtils.handleLaunchIntentResult(
                            requireActivity(),
                            IntentUtils.requireUninstallApp(requireActivity(), item.getPackageName())
                    );
                } else if (type == ApplicationUtils.ApplicationType.SYSTEM) {
                    showUninstallSystemAppDialog(item.getPackageName());
                } else {
                    Toast.makeText(requireActivity(), R.string.cannot_uninstall_app, Toast.LENGTH_SHORT)
                            .show();
                }
                return true;
            } else if (menuId == R.id.app_market) {
                IntentUtils.handleLaunchIntentResult(
                        requireActivity(),
                        IntentUtils.openAppInMarket(requireActivity(), item.getPackageName())
                );
                return true;
            } else if (menuId >= baseIndex) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    ShortcutInfo shortcutInfo = shortcutInfoList.get(menuId - baseIndex);
                    ApplicationUtils.launchAppShortcut(requireActivity(), item.getPackageName(),
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
    }

    private void showUninstallSystemAppDialog(String packageName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.hint)
                .setMessage(R.string.insist_uninstall_system_app)
                .setPositiveButton(R.string.uninstall, (dialog, which) ->
                        IntentUtils.handleLaunchIntentResult(
                                requireContext(),
                                IntentUtils.requireUninstallApp(requireContext(), packageName)
                        ))
                .setNegativeButton(R.string.cancel, null);
        DialogUtils.show(builder, viewModel.getAnimationValue());
    }
}