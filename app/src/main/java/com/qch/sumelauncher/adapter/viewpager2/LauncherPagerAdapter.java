package com.qch.sumelauncher.adapter.viewpager2;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.qch.sumelauncher.fragment.LauncherPageFragment;

public class LauncherPagerAdapter extends FragmentStateAdapter {
    private static final String TAG = "LauncherPagerAdapter";
    private int mScreenCount = 0;

    public LauncherPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.i(TAG, "Create Fragment #" + position);
        return LauncherPageFragment.newInstance(position);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setScreenCount(int screenCount) {
        this.mScreenCount = screenCount;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mScreenCount;
    }
}