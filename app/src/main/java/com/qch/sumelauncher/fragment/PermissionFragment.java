package com.qch.sumelauncher.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qch.sumelauncher.databinding.FragmentPermissionBinding;

public class PermissionFragment extends Fragment {
    private static final String TAG = "PermissionFragment";
    private FragmentPermissionBinding binding;

    public PermissionFragment() {
        // Required empty public constructor
    }

    public static PermissionFragment newInstance() {
        PermissionFragment fragment = new PermissionFragment();
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
        // Inflate the layout for this fragment
        binding = FragmentPermissionBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }
}