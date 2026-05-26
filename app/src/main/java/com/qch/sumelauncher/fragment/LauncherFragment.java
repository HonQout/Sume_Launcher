package com.qch.sumelauncher.fragment;

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
import com.qch.sumelauncher.adapter.viewpager2.AppPagerAdapter;
import com.qch.sumelauncher.databinding.FragmentLauncherBinding;
import com.qch.sumelauncher.viewmodel.LauncherViewModel;

public class LauncherFragment extends Fragment {
    private static final String TAG = "LauncherFragment";
    private FragmentLauncherBinding binding;
    private LauncherViewModel viewModel;

    public LauncherFragment() {
        // Required empty public constructor
    }

    public static LauncherFragment newInstance() {
        LauncherFragment fragment = new LauncherFragment();
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
        binding = FragmentLauncherBinding.inflate(inflater, container, false);
        AppPagerAdapter appPagerAdapter = new AppPagerAdapter(requireActivity(), 0);
        binding.fLauncherVp2.setAdapter(appPagerAdapter);
        binding.fLauncherVp2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                Log.i(TAG, "ViewPager2 onPageSelected position " + (position + 1));
                viewModel.setCurrentScreen(position + 1);
            }
        });
        binding.fLauncherBtnSettings.setOnClickListener(v ->
                viewModel.setLauncherState(LauncherViewModel.LauncherState.SETTINGS));
        binding.fLauncherBtnEdit.setOnClickListener(v ->
                viewModel.setLauncherState(LauncherViewModel.LauncherState.EDIT));
        binding.fLauncherBtnApps.setOnClickListener(v ->
                viewModel.setLauncherState(LauncherViewModel.LauncherState.APPS));
        binding.fLauncherBtnPrevPage.setOnClickListener(v -> launcherPageUp());
        binding.fLauncherBtnNextPage.setOnClickListener(v -> launcherPageDown());
        viewModel.getScrollToSwitchPage().observe(getViewLifecycleOwner(), scrollToSwitchPage ->
                binding.fLauncherVp2.setUserInputEnabled(scrollToSwitchPage));
        viewModel.getNumScreen().observe(getViewLifecycleOwner(), integer -> {
            if (integer != null) {
                appPagerAdapter.setNumScreen(integer);
                binding.fLauncherTvPage.setText(
                        String.format(
                                ContextCompat.getString(requireContext(), R.string.page_text),
                                viewModel.getCurrentScreenValue(), integer
                        ));
            }
        });
        viewModel.getCurrentScreen().observe(getViewLifecycleOwner(), integer -> {
            if (integer != null) {
                binding.fLauncherTvPage.setText(
                        String.format(
                                ContextCompat.getString(requireContext(), R.string.page_text),
                                integer, viewModel.getNumScreenValue()
                        ));
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View rootView = binding.getRoot();
        rootView.setFocusableInTouchMode(true);
        rootView.requestFocus();

        rootView.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_VOLUME_UP: {
                        if (viewModel.getVolumeKeySwitchPageValue()) {
                            launcherPageUp();
                            return true;
                        }
                        break;
                    }
                    case KeyEvent.KEYCODE_VOLUME_DOWN: {
                        if (viewModel.getVolumeKeySwitchPageValue()) {
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
            if (currentItem < viewPager2.getAdapter().getItemCount() - 1) {
                currentItem += 1;
                viewPager2.setCurrentItem(currentItem, false);
            }
        }
    }
}