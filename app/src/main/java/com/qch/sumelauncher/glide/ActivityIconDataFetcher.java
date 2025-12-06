package com.qch.sumelauncher.glide;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.qch.sumelauncher.bean.ActivityBean;
import com.qch.sumelauncher.utils.ApplicationUtils;

public class ActivityIconDataFetcher implements DataFetcher<Drawable> {
    private final Context context;
    private final PackageManager pm;
    private final String packageName;
    private final String activityName;

    public ActivityIconDataFetcher(Context context, ActivityBean activityBean) {
        this.context = context;
        this.pm = context.getPackageManager();
        this.packageName = activityBean.getPackageName();
        this.activityName = activityBean.getActivityName();
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super Drawable> callback) {
        try {
            Drawable icon = ApplicationUtils.getActivityIcon(context, packageName, activityName);
            callback.onDataReady(icon);
        } catch (Exception e) {
            callback.onLoadFailed(e);
        }
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void cancel() {

    }

    @NonNull
    @Override
    public Class<Drawable> getDataClass() {
        return Drawable.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }
}