package com.qch.sumelauncher.glide;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.qch.sumelauncher.bean.ActivityBean;

public class ActivityIconModelLoader implements ModelLoader<ActivityBean, Drawable> {
    private final Context context;

    public ActivityIconModelLoader(Context context) {
        this.context = context.getApplicationContext();
    }

    @Nullable
    @Override
    public LoadData<Drawable> buildLoadData(@NonNull ActivityBean o, int width, int height, @NonNull Options options) {
        return new LoadData<>(new GlideUrl(o.getKey()), new ActivityIconDataFetcher(context, o));
    }

    @Override
    public boolean handles(@NonNull ActivityBean o) {
        return true;
    }

    public static class Factory implements ModelLoaderFactory<ActivityBean, Drawable> {
        private final Context context;

        public Factory(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public ModelLoader<ActivityBean, Drawable> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new ActivityIconModelLoader(context);
        }

        @Override
        public void teardown() {

        }
    }
}