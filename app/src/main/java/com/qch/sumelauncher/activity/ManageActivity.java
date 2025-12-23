package com.qch.sumelauncher.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.qch.sumelauncher.databinding.ActivityManageBinding;
import com.qch.sumelauncher.utils.UIUtils;

public class ManageActivity extends AppCompatActivity {
    private static final String TAG = "ManageActivity";
    private ActivityManageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding=ActivityManageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UIUtils.setViewFitsSystemWindows(binding.getRoot());
    }
}