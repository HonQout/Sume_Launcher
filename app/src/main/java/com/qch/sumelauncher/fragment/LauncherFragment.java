package com.qch.sumelauncher.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.settings.ui.SettingsActivity;
import com.qch.sumelauncher.launcher.page.LauncherPagerAdapter;
import com.qch.sumelauncher.databinding.FragmentLauncherBinding;
import com.qch.sumelauncher.launcher.viewmodel.LauncherViewModel;
import com.qch.sumelauncher.settings.viewmodel.SettingsViewModel;

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
        LauncherPagerAdapter launcherPagerAdapter = new LauncherPagerAdapter(getChildFragmentManager(), getLifecycle());
        binding.fLauncherVp2.setAdapter(launcherPagerAdapter);
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
        binding.fLauncherBtnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), SettingsActivity.class);
            requireActivity().startActivity(intent);
        });
        binding.fLauncherBtnEdit.setOnClickListener(v ->
                launcherViewModel.setLauncherState(LauncherViewModel.LauncherState.EDIT));
        binding.fLauncherBtnApps.setOnClickListener(v ->
                launcherViewModel.setLauncherState(LauncherViewModel.LauncherState.APPS));
        binding.fLauncherBtnPrevPage.setOnClickListener(v -> launcherPageUp());
        binding.fLauncherBtnNextPage.setOnClickListener(v -> launcherPageDown());
        settingsViewModel.getScrollToSwitchPage().observe(getViewLifecycleOwner(), scrollToSwitchPage ->
                binding.fLauncherVp2.setUserInputEnabled(scrollToSwitchPage));
        launcherViewModel.getNumScreen().observe(getViewLifecycleOwner(), integer -> {
            if (integer != null) {
                launcherPagerAdapter.setScreenCount(integer);
                binding.fLauncherTvPage.setText(
                        String.format(
                                ContextCompat.getString(requireContext(), R.string.page_text),
                                launcherViewModel.getCurrentScreenIndexValue() + 1,
                                integer
                        )
                );
            }
        });
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

    public void launcherPageUp() {
        ViewPager2 viewPager2 = binding.fLauncherVp2;
        if (viewPager2.getAdapter() != null) {
            int currentItem = viewPager2.getCurrentItem();
            if (currentItem > 0) {
                currentItem -= 1;
                viewPager2.setCurrentItem(currentItem, false);
            }
        }
    }

    public void launcherPageDown() {
        ViewPager2 viewPager2 = binding.fLauncherVp2;
        if (viewPager2.getAdapter() != null) {
            int currentItem = viewPager2.getCurrentItem();
            if (currentItem < launcherViewModel.getNumScreenValue() - 1) {
                currentItem += 1;
                viewPager2.setCurrentItem(currentItem, false);
            }
        }
    }
}