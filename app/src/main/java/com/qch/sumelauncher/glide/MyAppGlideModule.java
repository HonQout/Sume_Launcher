package com.qch.sumelauncher.glide;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.qch.sumelauncher.bean.ActivityBean;
import com.qch.sumelauncher.glide.modelloader.ActivityIconModelLoader;
import com.qch.sumelauncher.glide.modelloader.LauncherIconModelLoader;
import com.qch.sumelauncher.room.entity.LauncherIconEntity;

@GlideModule
public class MyAppGlideModule extends AppGlideModule {
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide,
                                   @NonNull Registry registry) {
        super.registerComponents(context, glide, registry);
        registry.append(ActivityBean.class, Bitmap.class, new ActivityIconModelLoader.Factory(context));
        registry.append(LauncherIconEntity.class, Bitmap.class, new LauncherIconModelLoader.Factory(context));
    }
}