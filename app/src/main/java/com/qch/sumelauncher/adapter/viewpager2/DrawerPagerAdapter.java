package com.qch.sumelauncher.adapter.viewpager2;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.qch.sumelauncher.drawer.GridDrawerFragment;
import com.qch.sumelauncher.drawer.ListDrawerFragment;

public class DrawerPagerAdapter extends FragmentStateAdapter {
    private static final String TAG = "DrawerPagerAdapter";
    private final int mScreenCount = 2;

    public DrawerPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: {
                return GridDrawerFragment.newInstance();
            }
            case 1: {
                return ListDrawerFragment.newInstance();
            }
            default: {
                throw new IllegalArgumentException("Argument position is out of boundary.");
            }
        }
    }

    @Override
    public int getItemCount() {
        return mScreenCount;
    }
}