package com.qch.sumelauncher.adapter.viewpager2;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.qch.sumelauncher.fragment.LauncherPageFragment;

public class AppPagerAdapter extends FragmentStateAdapter {
    private static final String TAG = "AppPagerAdapter";
    private int numPages;

    public AppPagerAdapter(@NonNull FragmentActivity fragmentActivity, int numPages) {
        super(fragmentActivity);
        this.numPages = numPages;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.i(TAG, "Create Fragment #" + position);
        return LauncherPageFragment.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return numPages;
    }

    public void setNumScreen(int numPages) {
        this.numPages = numPages;
        notifyDataSetChanged();
    }
}