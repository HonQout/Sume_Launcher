package com.qch.sumelauncher.drawer;

import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.adapter.recyclerview.FilterableListAdapter;
import com.qch.sumelauncher.adapter.recyclerview.GridDrawerRVAdapter;
import com.qch.sumelauncher.bean.ActivityBean;
import com.qch.sumelauncher.databinding.FragmentGridDrawerBinding;
import com.qch.sumelauncher.recyclerview.GridDecoration;
import com.qch.sumelauncher.utils.ApplicationUtils;
import com.qch.sumelauncher.utils.DialogUtils;
import com.qch.sumelauncher.utils.IntentUtils;
import com.qch.sumelauncher.viewmodel.LauncherViewModel;

import java.util.ArrayList;
import java.util.List;

public class GridDrawerFragment extends Fragment {
    private static final String TAG = "GridDrawerFragment";
    private static final String KEY_GRID_LAYOUT_STATE = "GRID_LAYOUT_STATE";
    private FragmentGridDrawerBinding binding;
    private LauncherViewModel viewModel;
    private GridLayoutManager gridLayoutManager;
    private GridDrawerRVAdapter gridDrawerRVAdapter;

    public GridDrawerFragment() {
        // Required empty public constructor
    }

    public static GridDrawerFragment newInstance() {
        GridDrawerFragment fragment = new GridDrawerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        binding = FragmentGridDrawerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        // Initialize view model
        viewModel = new ViewModelProvider(requireActivity()).get(LauncherViewModel.class);
        // Initialize layout manager
        gridLayoutManager = new GridLayoutManager(requireContext(), viewModel.getNumColumnValue());
        // Initialize adapter of recycler view
        gridDrawerRVAdapter = new GridDrawerRVAdapter(new ArrayList<>());
        gridDrawerRVAdapter.setOnItemClickListener(new FilterableListAdapter.OnItemClickListener<>() {
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
        // Restore state of layout manager
        if (savedInstanceState == null) {
            binding.fGridDrawerRv.setLayoutManager(gridLayoutManager);
            binding.fGridDrawerRv.setAdapter(gridDrawerRVAdapter);
            binding.fGridDrawerRv.addItemDecoration(new GridDecoration(
                    viewModel.getNumColumnValue(),
                    getResources().getDimensionPixelSize(R.dimen.app_grid_space)
            ));
        } else {
            Parcelable gridLayoutState = savedInstanceState.getParcelable(KEY_GRID_LAYOUT_STATE);
            if (gridLayoutState != null) {
                gridLayoutManager.onRestoreInstanceState(gridLayoutState);
            }
        }
        viewModel.getActivityBeanList().observe(getViewLifecycleOwner(), gridDrawerRVAdapter::setList);
        // Initialize view
        binding.fGridDrawerSv.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                binding.fGridBtnQuit.setVisibility(View.VISIBLE);
            }
        });
        binding.fGridDrawerSv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                GridDrawerRVAdapter adapter = (GridDrawerRVAdapter) binding.fGridDrawerRv.getAdapter();
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                }
                return true;
            }
        });
        binding.fGridBtnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.fGridDrawerSv.clearFocus();
                binding.fGridBtnQuit.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.i(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_GRID_LAYOUT_STATE, gridLayoutManager.onSaveInstanceState());
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
                if (requireContext().getPackageName().equals(item.getPackageName())) {
                    showUninstallThisAppDialog();
                } else if (type == ApplicationUtils.ApplicationType.UPDATED_SYSTEM
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

    private void showUninstallThisAppDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.hint)
                .setMessage(R.string.insist_uninstall_this_app)
                .setPositiveButton(R.string.uninstall, (dialog, which) ->
                        IntentUtils.handleLaunchIntentResult(
                                requireContext(),
                                IntentUtils.requireUninstallApp(requireContext(),
                                        requireContext().getPackageName())
                        ))
                .setNegativeButton(R.string.cancel, null);
        DialogUtils.show(builder, viewModel.getAnimationValue());
    }
}