package com.qch.sumelauncher.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.databinding.FragmentLauncherBinding;

public class LauncherFragment extends Fragment {
    private static final String TAG = "LauncherFragment";
    private FragmentLauncherBinding binding;

    public LauncherFragment() {
        // Required empty public constructor
    }

    public static LauncherFragment newInstance(String param1, String param2) {
        LauncherFragment fragment = new LauncherFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLauncherBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}