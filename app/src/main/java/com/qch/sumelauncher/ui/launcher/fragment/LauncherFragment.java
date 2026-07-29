package com.qch.sumelauncher.ui.launcher.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.data.model.launcher.ActivityModel;
import com.qch.sumelauncher.data.model.launcher.IconModel;
import com.qch.sumelauncher.ui.launcher.page.LauncherPageAdapter;
import com.qch.sumelauncher.ui.launcher.page.LauncherLayout;
import com.qch.sumelauncher.settings.ui.SettingsActivity;
import com.qch.sumelauncher.databinding.FragmentLauncherBinding;
import com.qch.sumelauncher.ui.launcher.activity.LauncherViewModel;
import com.qch.sumelauncher.settings.viewmodel.SettingsViewModel;
import com.qch.sumelauncher.utils.ApplicationUtils;
import com.qch.sumelauncher.utils.DialogUtils;
import com.qch.sumelauncher.utils.IntentUtils;

import java.util.ArrayList;
import java.util.List;

public class LauncherFragment extends Fragment {
    private static final String TAG = "LauncherFragment";
    private FragmentLauncherBinding binding;
    private LauncherViewModel launcherViewModel;
    private SettingsViewModel settingsViewModel;

    public LauncherFragment() {
        // Required empty public constructor
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
        binding = FragmentLauncherBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        // Initialize ViewModel
        launcherViewModel = new ViewModelProvider(requireActivity()).get(LauncherViewModel.class);
        settingsViewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
        // Initialize ViewPager2
        LauncherPageAdapter launcherPageAdapter = new LauncherPageAdapter();
        binding.fLauncherVp2.setAdapter(launcherPageAdapter);
        // Restore index of current page
        if (launcherViewModel.getCurrentScreenIndexValue() != 0) {
            Log.i(TAG, "Saved current screen index = " + launcherViewModel.getCurrentScreenIndexValue());
            binding.fLauncherVp2.post(() ->
                    binding.fLauncherVp2.setCurrentItem(launcherViewModel.getCurrentScreenIndexValue(), false));
        }
        // Save index of current page
        binding.fLauncherVp2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                Log.i(TAG, "ViewPager2 onPageSelected position #" + (position + 1));
                launcherViewModel.setCurrentScreenIndex(position);
                binding.fLauncherTvPage.setText(
                        String.format(
                                ContextCompat.getString(requireContext(), R.string.page_text),
                                position + 1,
                                launcherViewModel.getNumScreenValue()
                        ));
            }
        });
        // Settings button
        binding.fLauncherBtnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), SettingsActivity.class);
            requireActivity().startActivity(intent);
        });
        // Edit button
        binding.fLauncherBtnEdit.setOnClickListener(v ->
                launcherViewModel.setLauncherState(LauncherViewModel.LauncherState.EDIT));
        // Apps button
        binding.fLauncherBtnApps.setOnClickListener(v ->
                launcherViewModel.setLauncherState(LauncherViewModel.LauncherState.APPS));
        // Prev page button
        binding.fLauncherBtnPrevPage.setOnClickListener(v -> launcherPageUp());
        // Next page button
        binding.fLauncherBtnNextPage.setOnClickListener(v -> launcherPageDown());
        // Launcher layout
        launcherViewModel.getGridSize().observe(getViewLifecycleOwner(), gridSize -> {
            if (gridSize == null) {
                Log.e(TAG, "Grid size is null.");
                return;
            }
            launcherPageAdapter.setGridSize(gridSize);
        });
        launcherViewModel.getIconModelMap().observe(getViewLifecycleOwner(), map -> {
            if (map == null) {
                Log.e(TAG, "Map of paged icons is null.");
                return;
            }
            launcherPageAdapter.setMap(map);
            launcherPageAdapter.setOnIconClickListener(new LauncherLayout.OnIconClickListener() {
                @Override
                public void onClick(@Nullable View view, IconModel item) {
                    if (view == null) {
                        Log.e(TAG, "Clicked IconModel is null.");
                        return;
                    }
                    IntentUtils.launchActivity(requireActivity(), item.getPackageName(),
                            item.getActivityName(), true);
                }

                @Override
                public boolean onLongClick(@Nullable View view, IconModel item) {
                    if (view == null) {
                        Log.e(TAG, "Long clicked IconModel is null.");
                        return false;
                    }
                    showIconActionMenu(view, item);
                    return true;
                }
            });
            launcherPageAdapter.setOnBlankAreaClickListener(new LauncherLayout.OnBlankAreaClickListener() {
                @Override
                public void onClick(int x, int y) {
                    Log.i(TAG, "Clicked blank cell " + x + "," + y);
                }

                @Override
                public boolean onLongClick(int x, int y) {
                    Log.i(TAG, "Long clicked blank cell " + x + "," + y);
                    launcherViewModel.setLauncherState(LauncherViewModel.LauncherState.EDIT);
                    return true;
                }
            });
        });
        // Set num screen
        launcherViewModel.getNumScreen().observe(getViewLifecycleOwner(), integer -> {
            if (integer != null) {
                binding.fLauncherTvPage.setText(
                        String.format(
                                ContextCompat.getString(requireContext(), R.string.page_text),
                                launcherViewModel.getCurrentScreenIndexValue() + 1,
                                integer
                        )
                );
            }
        });
        // Pattern of switching between pages
        settingsViewModel.getScrollToSwitchPage().observe(getViewLifecycleOwner(), scrollToSwitchPage ->
                binding.fLauncherVp2.setUserInputEnabled(scrollToSwitchPage));
        // Set key listener
        View rootView = binding.getRoot();
        rootView.setFocusableInTouchMode(true);
        rootView.requestFocus();
        rootView.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_VOLUME_UP: {
                        if (settingsViewModel.getVolumeKeySwitchPageValue()) {
                            launcherPageUp();
                            return true;
                        }
                        break;
                    }
                    case KeyEvent.KEYCODE_VOLUME_DOWN: {
                        if (settingsViewModel.getVolumeKeySwitchPageValue()) {
                            launcherPageDown();
                            return true;
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
            return false;
        });
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView");
        super.onDestroyView();
        binding = null;
    }

    private void launcherPageUp() {
        ViewPager2 viewPager2 = binding.fLauncherVp2;
        if (viewPager2.getAdapter() != null) {
            int currentItem = viewPager2.getCurrentItem();
            if (currentItem > 0) {
                currentItem -= 1;
                viewPager2.setCurrentItem(currentItem, false);
            }
        }
    }

    private void launcherPageDown() {
        ViewPager2 viewPager2 = binding.fLauncherVp2;
        if (viewPager2.getAdapter() != null) {
            int currentItem = viewPager2.getCurrentItem();
            if (currentItem < launcherViewModel.getNumScreenValue() - 1) {
                currentItem += 1;
                viewPager2.setCurrentItem(currentItem, false);
            }
        }
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

    private void showIconActionMenu(@NonNull View view, @NonNull IconModel iconModel) {
        Context context = view.getContext();

        ActivityInfo activityInfo = ApplicationUtils.getActivityInfo(
                context,
                iconModel.getPackageName(),
                iconModel.getActivityName()
        );
        if (activityInfo == null) {
            Log.e(TAG, "Cannot find corresponding ActivityInfo.");
            return;
        }
        ActivityModel activityModel = new ActivityModel(context, activityInfo);

        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.launcher_icon_op_menu, popupMenu.getMenu());
        int baseIndex = popupMenu.getMenu().size();
        List<ShortcutInfo> shortcutInfoList;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutInfoList = activityModel.getShortcutInfoList();
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
            if (menuId == R.id.remove_icon) {
                binding.fLauncherVp2.getAdapter();
                launcherViewModel.removeIcon(iconModel.toIconEntity());
                return true;
            } else if (menuId == R.id.uninstall) {
                ApplicationUtils.ApplicationType type = ApplicationUtils.getApplicationType(
                        requireActivity(), activityModel.getPackageName());
                if (requireContext().getPackageName().equals(activityModel.getPackageName())) {
                    showUninstallThisAppDialog();
                } else if (type == ApplicationUtils.ApplicationType.UPDATED_SYSTEM
                        || type == ApplicationUtils.ApplicationType.USER) {
                    IntentUtils.handleLaunchIntentResult(
                            requireActivity(),
                            IntentUtils.requireUninstallApp(requireActivity(), activityModel.getPackageName())
                    );
                } else if (type == ApplicationUtils.ApplicationType.SYSTEM) {
                    showUninstallSystemAppDialog(activityModel.getPackageName());
                } else {
                    Toast.makeText(requireActivity(), R.string.cannot_uninstall_app, Toast.LENGTH_SHORT)
                            .show();
                }
                return true;
            } else if (menuId == R.id.app_info) {
                IntentUtils.handleLaunchIntentResult(
                        requireActivity(),
                        IntentUtils.openAppDetailsPage(requireActivity(), activityModel.getPackageName())
                );
                return true;
            } else if (menuId == R.id.app_market) {
                IntentUtils.handleLaunchIntentResult(
                        requireActivity(),
                        IntentUtils.openAppInMarket(requireActivity(), activityModel.getPackageName())
                );
                return true;
            } else if (menuId >= baseIndex) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    ShortcutInfo shortcutInfo = shortcutInfoList.get(menuId - baseIndex);
                    ApplicationUtils.launchAppShortcut(context, activityModel.getPackageName(),
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