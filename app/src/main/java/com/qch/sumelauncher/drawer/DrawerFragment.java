package com.qch.sumelauncher.drawer;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qch.sumelauncher.adapter.viewpager2.DrawerPagerAdapter;
import com.qch.sumelauncher.databinding.FragmentDrawerBinding;
import com.qch.sumelauncher.viewmodel.LauncherViewModel;

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
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        binding = FragmentDrawerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        // Initialize view model
        viewModel = new ViewModelProvider(requireActivity()).get(LauncherViewModel.class);
        // Initialize view
        DrawerPagerAdapter drawerPagerAdapter = new DrawerPagerAdapter(getChildFragmentManager(), getLifecycle());
        binding.fDrawerVp2.setAdapter(drawerPagerAdapter);
        binding.fDrawerBtnGrid.setOnClickListener(v -> {
            Log.i(TAG, "Drawer button grid onClicked");
            binding.fDrawerVp2.setCurrentItem(0);
        });
        binding.fDrawerBtnList.setOnClickListener(v -> {
            Log.i(TAG, "Drawer button list onClicked");
            binding.fDrawerVp2.setCurrentItem(1);
        });
        binding.fDrawerBtnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.popBackStack();
        });
        // Set onBackPressed callback
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                NavController navController = Navigation.findNavController(view);
                navController.popBackStack();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), onBackPressedCallback);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.i(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }
}