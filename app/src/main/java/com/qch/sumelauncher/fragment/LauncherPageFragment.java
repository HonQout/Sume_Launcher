package com.qch.sumelauncher.fragment;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qch.sumelauncher.bean.ActivityBean;
import com.qch.sumelauncher.databinding.FragmentLauncherPageBinding;
import com.qch.sumelauncher.room.entity.LauncherIconEntity;
import com.qch.sumelauncher.utils.ApplicationUtils;
import com.qch.sumelauncher.utils.IntentUtils;
import com.qch.sumelauncher.view.LauncherLayout;
import com.qch.sumelauncher.viewmodel.LauncherViewModel;

import java.util.List;

public class LauncherPageFragment extends Fragment {
    private static final String TAG = "LauncherPageFragment";
    private static final String ARG_POSITION = "POSITION";
    private FragmentLauncherPageBinding binding;
    private int position;
    private LauncherViewModel viewModel;

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
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            position = args.getInt(ARG_POSITION);
        }
        viewModel = new ViewModelProvider(requireActivity()).get(LauncherViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "LauncherPageFragment #" + position + " onCreateView");
        binding = FragmentLauncherPageBinding.inflate(inflater, container, false);
        viewModel.getNumRow().observe(getViewLifecycleOwner(), integer -> {
            if (integer != null) {
                binding.fLauncherLl.setNumRows(integer);
            }
        });
        viewModel.getNumColumn().observe(getViewLifecycleOwner(), integer -> {
            if (integer != null) {
                binding.fLauncherLl.setNumColumns(integer);
            }
        });
        viewModel.getPagedIcons().observe(getViewLifecycleOwner(), map -> {
            if (map != null) {
                List<LauncherIconEntity> list = map.get(position);
                if (list != null) {
                    binding.fLauncherLl.setEntityList(list);
                    Log.i(TAG, "Set list of LauncherIconEntity.\nPageIndex: " + position
                            + "\nSize: " + list.size());
                    binding.fLauncherLl.setOnItemClickListener(new LauncherLayout.OnItemClickListener() {
                        @Override
                        public void onItemClick(LauncherIconEntity item, int position) {
                            IntentUtils.launchActivity(requireActivity(), item.packageName,
                                    item.activityName, true);
                        }

                        @Override
                        public boolean onItemLongClick(LauncherIconEntity item, int position,
                                                       float x, float y, @Nullable View view) {
                            if (view == null) {
                                Log.e(TAG, "Cannot find corresponding view.");
                                return false;
                            }
                            ActivityInfo activityInfo = ApplicationUtils.getActivityInfo(
                                    requireContext(), item.packageName, item.activityName);
                            if (activityInfo == null) {
                                Log.e(TAG, "Cannot find corresponding ActivityInfo.");
                                return false;
                            }
                            ActivityBean activityBean = new ActivityBean(requireContext(), activityInfo);
                            viewModel.showGridMenu(requireActivity(), activityBean, view);
                            return true;
                        }
                    });
                }
            }
        });
        return binding.getRoot();
    }
}