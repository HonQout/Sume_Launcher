package com.qch.sumelauncher.glide.modelloader;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.qch.sumelauncher.bean.ActivityBean;
import com.qch.sumelauncher.glide.datafetcher.ActivityIconDataFetcher;

public class ActivityIconModelLoader implements ModelLoader<ActivityBean, Bitmap> {
    private final Context context;

    public ActivityIconModelLoader(Context context) {
        this.context = context.getApplicationContext();
    }

    @Nullable
    @Override
    public LoadData<Bitmap> buildLoadData(@NonNull ActivityBean activityBean, int width, int height,
                                          @NonNull Options options) {
        return new LoadData<>(new GlideUrl(activityBean.getKey()),
                new ActivityIconDataFetcher(context, activityBean));
    }

    @Override
    public boolean handles(@NonNull ActivityBean activityBean) {
        return true;
    }

    public static class Factory implements ModelLoaderFactory<ActivityBean, Bitmap> {
        private final Context context;

        public Factory(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public ModelLoader<ActivityBean, Bitmap> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new ActivityIconModelLoader(context);
        }

        @Override
        public void teardown() {

        }
    }
}