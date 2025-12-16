package com.qch.sumelauncher.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.qch.sumelauncher.adapter.recyclerview.FilterableListAdapter;
import com.qch.sumelauncher.adapter.recyclerview.PermissionRVAdapter;
import com.qch.sumelauncher.bean.PermissionBean;
import com.qch.sumelauncher.databinding.ActivityPermissionBinding;
import com.qch.sumelauncher.utils.UIUtils;
import com.qch.sumelauncher.viewmodel.PermissionViewModel;

import java.util.ArrayList;
import java.util.Objects;

public class PermissionActivity extends AppCompatActivity {
    private static final String TAG = "PermissionActivity";
    private ActivityPermissionBinding binding;
    private PermissionViewModel viewModel;
    private final SharedPreferences.OnSharedPreferenceChangeListener listener =
            (sharedPreferences, key) -> {
                if (Objects.equals(key, "display_status_bar")) {
                    boolean displayStatusBar = sharedPreferences.getBoolean(key, true);
                    UIUtils.handleStatusBarVisibility(getWindow(), displayStatusBar);
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPermissionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UIUtils.setViewFitsSystemWindows(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(PermissionViewModel.class);
        binding.aPermissionRv.setLayoutManager(new LinearLayoutManager(this));
        PermissionRVAdapter adapter = new PermissionRVAdapter(new ArrayList<>());
        adapter.setOnItemClickListener(new FilterableListAdapter.OnItemClickListener<>() {
            @Override
            public void onItemClick(PermissionBean item, View view) {
                viewModel.showPermissionDetailDialog(PermissionActivity.this, item);
            }

            @Override
            public boolean onItemLongClick(PermissionBean item, View view) {
                return false;
            }
        });
        binding.aPermissionRv.setAdapter(adapter);
        viewModel.getRequestedPermissionBeanList().observe(this, adapter::setList);
    }
}