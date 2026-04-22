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
import com.qch.sumelauncher.glide.datafetcher.LauncherIconDataFetcher;
import com.qch.sumelauncher.room.entity.IconEntity;

public class LauncherIconModelLoader implements ModelLoader<IconEntity, Bitmap> {
    private final Context context;

    public LauncherIconModelLoader(Context context) {
        this.context = context.getApplicationContext();
    }

    @Nullable
    @Override
    public LoadData<Bitmap> buildLoadData(@NonNull IconEntity iconEntity, int width, int height,
                                          @NonNull Options options) {
        return new LoadData<>(new GlideUrl(iconEntity.getKey()), new LauncherIconDataFetcher(context, iconEntity));
    }

    @Override
    public boolean handles(@NonNull IconEntity iconEntity) {
        return true;
    }

    public static class Factory implements ModelLoaderFactory<IconEntity, Bitmap> {
        private final Context context;

        public Factory(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public ModelLoader<IconEntity, Bitmap> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new LauncherIconModelLoader(context);
        }

        @Override
        public void teardown() {

        }
    }
}