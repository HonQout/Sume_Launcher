package com.qch.sumelauncher.glide;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.qch.sumelauncher.bean.ActivityBean;

@GlideModule
public class ActivityGlideModule extends AppGlideModule {
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        super.registerComponents(context, glide, registry);
        registry.append(ActivityBean.class, Drawable.class, new ActivityIconModelLoader.Factory(context));
    }
}