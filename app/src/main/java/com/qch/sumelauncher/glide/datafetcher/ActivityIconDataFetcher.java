package com.qch.sumelauncher.glide.datafetcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.qch.sumelauncher.bean.ActivityBean;
import com.qch.sumelauncher.utils.ApplicationUtils;
import com.qch.sumelauncher.utils.DrawableUtils;

public class ActivityIconDataFetcher implements DataFetcher<Bitmap> {
    private final Context appContext;
    private final String packageName;
    private final String activityName;

    public ActivityIconDataFetcher(Context context, ActivityBean activityBean) {
        this.appContext = context.getApplicationContext();
        this.packageName = activityBean.getPackageName();
        this.activityName = activityBean.getActivityName();
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super Bitmap> callback) {
        try {
            Drawable icon = ApplicationUtils.getActivityIcon(appContext, packageName, activityName);
            Bitmap bitmap = DrawableUtils.toBitmap(icon);
            callback.onDataReady(bitmap);
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
    public Class<Bitmap> getDataClass() {
        return Bitmap.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.LOCAL;
    }
}