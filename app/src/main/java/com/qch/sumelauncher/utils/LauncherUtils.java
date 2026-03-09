package com.qch.sumelauncher.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.qch.sumelauncher.bean.ActivityBean;
import com.qch.sumelauncher.room.entity.LauncherIconEntity;

import java.util.ArrayList;
import java.util.List;

public class LauncherUtils {
    private static final String TAG = "LauncherUtils";
    public static final String FOUR_BY_FOUR = "4,4";
    public static final String FOUR_BY_FIVE = "4,5";
    public static final String FIVE_BY_FOUR = "5,4";
    public static final String FIVE_BY_FIVE = "5,5";

    public static List<LauncherIconEntity> getDefaultLauncherIconList(@NonNull Context context,
                                                                      @NonNull String gridSize) {
        List<LauncherIconEntity> launcherIconEntityList = new ArrayList<>();
        List<ActivityBean> activityBeanList
                = ApplicationUtils.getActivityBeanList(context, null);
        String[] split = gridSize.split(",");
        int numRow = 5;
        int numColumn = 5;
        try {
            numRow = Integer.parseInt(split[0]);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get numRow.", e);
        }
        try {
            numColumn = Integer.parseInt(split[1]);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get numColumn.", e);
        }
        int pageIndex = 0;
        int cellX = 0;
        int cellY = 0;
        for (int i = 0; i < activityBeanList.size(); i++) {
            ActivityBean activityBean = activityBeanList.get(i);
            LauncherIconEntity launcherIconEntity = new LauncherIconEntity(gridSize, pageIndex, cellX, cellY,
                    activityBean.getPackageName(), activityBean.getActivityName());
            launcherIconEntityList.add(launcherIconEntity);
            cellX++;
            if (cellX >= numColumn) {
                cellX = 0;
                cellY++;
            }
            if (cellY >= numRow) {
                cellY = 0;
                pageIndex++;
            }
        }
        return launcherIconEntityList;
    }
}