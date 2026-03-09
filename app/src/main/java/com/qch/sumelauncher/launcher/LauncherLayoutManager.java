package com.qch.sumelauncher.launcher;

import android.content.Context;

import androidx.annotation.NonNull;

import com.qch.sumelauncher.room.entity.LauncherIconEntity;

import java.util.ArrayList;
import java.util.List;

public class LauncherLayoutManager {
    private static final String FOUR_BY_FOUR = "4,4";
    private static final String FOUR_BY_FIVE = "4,5";
    private static final String FIVE_BY_FOUR = "5,4";
    private static final String FIVE_BY_FIVE = "5,5";
    private List<LauncherIconEntity> list44 = new ArrayList<>();
    private List<LauncherIconEntity> list45 = new ArrayList<>();
    private List<LauncherIconEntity> list54 = new ArrayList<>();
    private List<LauncherIconEntity> list55 = new ArrayList<>();

    public LauncherLayoutManager(@NonNull Context context) {

    }
}