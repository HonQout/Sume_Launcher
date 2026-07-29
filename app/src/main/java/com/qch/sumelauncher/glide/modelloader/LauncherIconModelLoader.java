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
import com.qch.sumelauncher.glide.datafetcher.LauncherIconDataFetcher;
import com.qch.sumelauncher.data.model.launcher.IconModel;

public class LauncherIconModelLoader implements ModelLoader<IconModel, Drawable> {
    private final Context context;

    public LauncherIconModelLoader(Context context) {
        this.context = context.getApplicationContext();
    }

    @Nullable
    @Override
    public LoadData<Drawable> buildLoadData(@NonNull IconModel iconModel, int width, int height,
                                            @NonNull Options options) {
        return new LoadData<>(new GlideUrl(iconModel.getKey()), new LauncherIconDataFetcher(context, iconModel));
    }

    @Override
    public boolean handles(@NonNull IconModel iconModel) {
        return true;
    }

    public static class Factory implements ModelLoaderFactory<IconModel, Drawable> {
        private final Context context;

        public Factory(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public ModelLoader<IconModel, Drawable> build(@NonNull MultiModelLoaderFactory multiFactory) {
            return new LauncherIconModelLoader(context);
        }

        @Override
        public void teardown() {

        }
    }
}