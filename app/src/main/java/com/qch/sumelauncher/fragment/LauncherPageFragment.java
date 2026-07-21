package com.qch.sumelauncher.fragment;

import android.content.pm.ActivityInfo;
import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.bean.ActivityBean;
import com.qch.sumelauncher.databinding.FragmentLauncherPageBinding;
import com.qch.sumelauncher.launcher.view.LauncherLayout;
import com.qch.sumelauncher.launcher.viewmodel.LauncherViewModel;
import com.qch.sumelauncher.room.entity.IconEntity;
import com.qch.sumelauncher.settings.viewmodel.SettingsViewModel;
import com.qch.sumelauncher.utils.ApplicationUtils;
import com.qch.sumelauncher.utils.DialogUtils;
import com.qch.sumelauncher.utils.IntentUtils;

import java.util.ArrayList;
import java.util.List;

public class LauncherPageFragment extends Fragment {
    private static final String TAG = "LauncherPageFragment";
    private static final String ARG_POSITION = "POSITION";
    private FragmentLauncherPageBinding binding;
    private int position;
    private LauncherViewModel launcherViewModel;
    private SettingsViewModel settingsViewModel;

    public LauncherPageFragment() {
        // Required empty public constructor
    }

    public static LauncherPageFragment newInstance(int position) {
        LauncherPageFragment fragment = new LauncherPageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "LauncherPageFragment #" + position + " onCreate");
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            position = args.getInt(ARG_POSITION);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "LauncherPageFragment #" + position + " onCreateView");
        binding = FragmentLauncherPageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "LauncherPageFragment #" + position + " onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        launcherViewModel = new ViewModelProvider(requireActivity()).get(LauncherViewModel.class);
        settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
        launcherViewModel.getNumRow().observe(getViewLifecycleOwner(), integer -> {
            if (integer != null) {
                binding.fLauncherLl.setNumRows(integer);
            }
        });
        launcherViewModel.getNumColumn().observe(getViewLifecycleOwner(), integer -> {
            if (integer != null) {
                binding.fLauncherLl.setNumColumns(integer);
            }
        });
        launcherViewModel.getLauncherIconMap().observe(getViewLifecycleOwner(), map -> {
            if (map == null) {
                Log.e(TAG, "Map of paged icons is null.");
                return;
            }
            List<IconEntity> list = map.get(position);
            if (list == null) {
                Log.e(TAG, "List of icons of page #" + position + " is null.");
                return;
            }
            binding.fLauncherLl.setIconEntityList(list);
            Log.i(TAG, "Set list of IconEntity.\nPageIndex: " + position + "\nSize: "
                    + list.size());
            binding.fLauncherLl.setOnIconClickListener(new LauncherLayout.OnIconClickListener() {
                @Override
                public void onIconClick(@Nullable View view, IconEntity item) {
                    IntentUtils.launchActivity(requireActivity(), item.getPackageName(),
                            item.getActivityName(), true);
                }

                @Override
                public boolean onIconLongClick(@Nullable View view,
                                               IconEntity iconEntity) {
                    if (view == null) {
                        Log.e(TAG, "Cannot find corresponding view.");
                        return false;
                    }
                    showIconActionMenu(view, iconEntity);
                    return true;
                }
            });
            binding.fLauncherLl.setOnBlankClickListener(new LauncherLayout.OnBlankAreaClickListener() {
                @Override
                public void onBlankAreaClick(int x, int y) {
                    Log.i(TAG, "Clicked blank cell " + x + "," + y);
                }

                @Override
                public boolean onBlankAreaLongClick(int x, int y) {
                    Log.i(TAG, "Long clicked blank cell " + x + "," + y);
                    launcherViewModel.setLauncherState(LauncherViewModel.LauncherState.EDIT);
                    return false;
                }
            });
        });
        launcherViewModel.getLauncherState().observe(getViewLifecycleOwner(), launcherState -> {
            binding.fLauncherLl.setEditMode(launcherState == LauncherViewModel.LauncherState.EDIT);
        });
    }

    private void showIconActionMenu(@NonNull View view, @NonNull IconEntity iconEntity) {
        ActivityInfo activityInfo = ApplicationUtils.getActivityInfo(
                requireContext(),
                iconEntity.getPackageName(),
                iconEntity.getActivityName()
        );
        if (activityInfo == null) {
            Log.e(TAG, "Cannot find corresponding ActivityInfo.");
            return;
        }
        ActivityBean activityBean = new ActivityBean(requireContext(), activityInfo);
        PopupMenu popupMenu = new PopupMenu(requireActivity(), view);
        popupMenu.getMenuInflater().inflate(R.menu.launcher_icon_op_menu, popupMenu.getMenu());
        int baseIndex = popupMenu.getMenu().size();
        List<ShortcutInfo> shortcutInfoList;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutInfoList = activityBean.getShortcutInfoList();
            for (int i = 0; i < shortcutInfoList.size(); i++) {
                ShortcutInfo shortcutInfo = shortcutInfoList.get(i);
                popupMenu.getMenu().add(
                        0,
                        baseIndex + i,
                        baseIndex + i,
                        shortcutInfo.getShortLabel()
                );
            }
        } else {
            shortcutInfoList = new ArrayList<>();
        }
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            int menuId = menuItem.getItemId();
            if (menuId == R.id.move_to_new_screen) {
                launcherViewModel.moveIconToNewScreen(iconEntity);
                return true;
            } else if (menuId == R.id.remove_icon) {
                binding.fLauncherLl.removeIconView(iconEntity);
                launcherViewModel.removeIcon(iconEntity);
                return true;
            } else if (menuId == R.id.uninstall) {
                ApplicationUtils.ApplicationType type = ApplicationUtils.getApplicationType(
                        requireActivity(), activityBean.getPackageName());
                if (requireContext().getPackageName().equals(activityBean.getPackageName())) {
                    showUninstallThisAppDialog();
                } else if (type == ApplicationUtils.ApplicationType.UPDATED_SYSTEM
                        || type == ApplicationUtils.ApplicationType.USER) {
                    IntentUtils.handleLaunchIntentResult(
                            requireActivity(),
                            IntentUtils.requireUninstallApp(requireActivity(), activityBean.getPackageName())
                    );
                } else if (type == ApplicationUtils.ApplicationType.SYSTEM) {
                    showUninstallSystemAppDialog(activityBean.getPackageName());
                } else {
                    Toast.makeText(requireActivity(), R.string.cannot_uninstall_app, Toast.LENGTH_SHORT)
                            .show();
                }
                return true;
            } else if (menuId == R.id.app_info) {
                IntentUtils.handleLaunchIntentResult(
                        requireActivity(),
                        IntentUtils.openAppDetailsPage(requireActivity(), activityBean.getPackageName())
                );
                return true;
            } else if (menuId == R.id.app_market) {
                IntentUtils.handleLaunchIntentResult(
                        requireActivity(),
                        IntentUtils.openAppInMarket(requireActivity(), activityBean.getPackageName())
                );
                return true;
            } else if (menuId >= baseIndex) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    ShortcutInfo shortcutInfo = shortcutInfoList.get(menuId - baseIndex);
                    ApplicationUtils.launchAppShortcut(requireActivity(), activityBean.getPackageName(),
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
        DialogUtils.show(builder, settingsViewModel.getAnimationValue());
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
        DialogUtils.show(builder, settingsViewModel.getAnimationValue());
    }
}