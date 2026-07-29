package com.qch.sumelauncher.glide.datafetcher;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.qch.sumelauncher.data.model.launcher.ActivityModel;
import com.qch.sumelauncher.utils.ApplicationUtils;

public class ActivityIconDataFetcher implements DataFetcher<Drawable> {
    private final Context appContext;
    private final String packageName;
    private final String activityName;

    public ActivityIconDataFetcher(Context context, ActivityModel activityModel) {
        this.appContext = context.getApplicationContext();
        this.packageName = activityModel.getPackageName();
        this.activityName = activityModel.getActivityName();
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super Drawable> callback) {
        try {
            Drawable icon = ApplicationUtils.getActivityIcon(appContext, packageName, activityName);
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