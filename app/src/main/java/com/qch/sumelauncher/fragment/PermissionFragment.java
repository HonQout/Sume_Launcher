package com.qch.sumelauncher.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qch.sumelauncher.bean.PermissionBean;
import com.qch.sumelauncher.databinding.FragmentPermissionBinding;
import com.qch.sumelauncher.recyclerview.adapter.FilterableListAdapter;
import com.qch.sumelauncher.recyclerview.adapter.PermissionListRVAdapter;
import com.qch.sumelauncher.viewmodel.PermissionViewModel;

import java.util.ArrayList;

public class PermissionFragment extends Fragment {
    private static final String TAG = "PermissionFragment";
    private FragmentPermissionBinding binding;
    private PermissionViewModel viewModel;

    public PermissionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PermissionViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPermissionBinding.inflate(inflater, container, false);
        binding.fPermissionRv.setLayoutManager(new LinearLayoutManager(requireContext()));
        PermissionListRVAdapter adapter = new PermissionListRVAdapter(new ArrayList<>());
        adapter.setOnItemClickListener(new FilterableListAdapter.OnItemClickListener<PermissionBean>() {
            @Override
            public void onItemClick(PermissionBean item, View view) {
                viewModel.showPermissionDetailDialog(requireActivity(), item);
            }

            @Override
            public boolean onItemLongClick(PermissionBean item, View view) {
                return false;
            }
        });
        binding.fPermissionRv.setAdapter(adapter);
        viewModel.getRequestedPermissionBeanList().observe(getViewLifecycleOwner(), adapter::setList);
        return binding.getRoot();
    }
}