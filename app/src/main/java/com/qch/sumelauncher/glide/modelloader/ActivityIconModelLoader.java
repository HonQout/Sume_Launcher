package com.qch.sumelauncher.glide.modelloader;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.qch.sumelauncher.data.model.launcher.ActivityModel;
import com.qch.sumelauncher.glide.datafetcher.ActivityIconDataFetcher;

public class ActivityIconModelLoader implements ModelLoader<ActivityModel, Drawable> {
    private final Context context;

    public ActivityIconModelLoader(Context context) {
        this.context = context.getApplicationContext();
    }

    @Nullable
    @Override
    public LoadData<Drawable> buildLoadData(@NonNull ActivityModel activityModel, int width, int height,
                                          @NonNull Options options) {
        return new LoadData<>(new GlideUrl(activityModel.getKey()),
                new ActivityIconDataFetcher(context, activityModel));
    }

    @Override
    public boolean handles(@NonNull ActivityModel activityModel) {
        return true;
    }

    public static class Factory implements ModelLoaderFactory<ActivityModel, Drawable> {
        private final Context context;

        public Factory(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public ModelLoader<ActivityModel, Drawable> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new ActivityIconModelLoader(context);
        }

        @Override
        public void teardown() {

        }
    }
}