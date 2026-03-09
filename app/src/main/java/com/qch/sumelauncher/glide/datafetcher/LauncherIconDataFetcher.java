package com.qch.sumelauncher.glide.datafetcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.qch.sumelauncher.room.entity.LauncherIconEntity;
import com.qch.sumelauncher.utils.ApplicationUtils;
import com.qch.sumelauncher.utils.DrawableUtils;

public class LauncherIconDataFetcher implements DataFetcher<Bitmap> {
    private final Context context;
    private final String packageName;
    private final String activityName;

    public LauncherIconDataFetcher(Context context, LauncherIconEntity launcherIconEntity) {
        this.context = context;
        this.packageName = launcherIconEntity.packageName;
        this.activityName = launcherIconEntity.activityName;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super Bitmap> callback) {
        try {
            Drawable icon = ApplicationUtils.getActivityIcon(context, packageName, activityName);
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