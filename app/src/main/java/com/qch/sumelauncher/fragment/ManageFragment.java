package com.qch.sumelauncher.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.adapter.recyclerview.AppManageRVAdapter;
import com.qch.sumelauncher.adapter.recyclerview.FilterableListAdapter;
import com.qch.sumelauncher.bean.ActivityBean;
import com.qch.sumelauncher.databinding.FragmentManageBinding;
import com.qch.sumelauncher.utils.ApplicationUtils;
import com.qch.sumelauncher.utils.IntentUtils;
import com.qch.sumelauncher.viewmodel.LauncherViewModel;

public class ManageFragment extends Fragment {
    private static final String TAG = "ManageFragment";
    private FragmentManageBinding binding;
    private LauncherViewModel viewModel;

    public ManageFragment() {
        // Required empty public constructor
    }

    public static ManageFragment newInstance() {
        ManageFragment fragment = new ManageFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
        viewModel = new ViewModelProvider(requireActivity()).get(LauncherViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentManageBinding.inflate(inflater, container, false);
        binding.fManageSv.setOnSearchClickListener(v ->
                binding.fManageTv.setVisibility(View.GONE));
        binding.fManageSv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                AppManageRVAdapter adapter = (AppManageRVAdapter) binding.fManageRv.getAdapter();
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                }
                return true;
            }
        });
        binding.fManageSv.setOnCloseListener(() -> {
            binding.fManageTv.setVisibility(View.VISIBLE);
            return false;
        });
        binding.fManageRv.setLayoutManager(new LinearLayoutManager(requireActivity(),
                RecyclerView.VERTICAL, false));
        AppManageRVAdapter appManageRVAdapter = new AppManageRVAdapter(viewModel.getActivityBeanListValue());
        appManageRVAdapter.setOnItemClickListener(new FilterableListAdapter.OnItemClickListener<>() {
            @Override
            public void onItemClick(ActivityBean item, View view) {
                IntentUtils.handleLaunchActivityResult(requireActivity(),
                        IntentUtils.launchActivity(requireActivity(),
                                item.getPackageName(), item.getActivityName(), true));
            }

            @Override
            public boolean onItemLongClick(ActivityBean item, View view) {
                return false;
            }
        });
        appManageRVAdapter.setOnButtonPressedListener(new AppManageRVAdapter.OnButtonPressedListener() {
            @Override
            public void onHideButtonPressed(ActivityBean item, View view) {

            }

            @Override
            public void onUninstallButtonPressed(ActivityBean item, View view) {
                ApplicationUtils.ApplicationType type = ApplicationUtils.getApplicationType(
                        requireActivity(), item.getPackageName());
                if (type == ApplicationUtils.ApplicationType.UPDATED_SYSTEM
                        || type == ApplicationUtils.ApplicationType.USER) {
                    IntentUtils.handleLaunchIntentResult(
                            requireActivity(),
                            IntentUtils.requireUninstallApp(requireActivity(), item.getPackageName())
                    );
                } else if (type == ApplicationUtils.ApplicationType.SYSTEM) {
                    viewModel.showUninstallSystemAppDialog(requireActivity(), item.getPackageName());
                } else {
                    Toast.makeText(requireActivity(), R.string.cannot_uninstall_app,
                                    Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
        binding.fManageRv.setAdapter(appManageRVAdapter);
        viewModel.getActivityBeanList().observe(getViewLifecycleOwner(), activityBeanList -> {
            appManageRVAdapter.setList(activityBeanList);
            binding.fManageTv.setText(
                    String.format(
                            ContextCompat.getString(
                                    requireContext(),
                                    R.string.num_items), activityBeanList.size()));
        });
        return binding.getRoot();
    }
}