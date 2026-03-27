package com.qch.sumelauncher.view;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.qch.sumelauncher.R;
import com.qch.sumelauncher.room.entity.LauncherIconEntity;
import com.qch.sumelauncher.utils.ApplicationUtils;

public class LauncherIconView extends FrameLayout {
    private static final String TAG = "LauncherIconView";
    private final ImageView imageView;
    private final TextView textView;
    private LauncherIconEntity launcherIconEntity;
    private RequestBuilder<Bitmap> requestBuilder;

    public LauncherIconView(@NonNull Context context) {
        this(context, null, 0, 0);
    }

    public LauncherIconView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public LauncherIconView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public LauncherIconView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        View view = LayoutInflater.from(context).inflate(R.layout.item_launcher_icon, this, true);
        imageView = view.findViewById(R.id.item_app_grid_icon);
        textView = view.findViewById(R.id.item_app_grid_label);
        if (imageView == null) {
            Log.e(TAG, "Cannot find ImageView.");
        }
        if (textView == null) {
            Log.e(TAG, "Cannot find TextView.");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.i(TAG, "Width = " + widthMeasureSpec + "; Height = " + heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if ((w > 0 && w != oldw) || (h > 0 && h != oldh)) {
            cancelLoadingIcon();
            loadIcon();
            loadLabel();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (launcherIconEntity != null && getMeasuredWidth() > 0 && getMeasuredHeight() > 0) {
            loadIcon();
            loadLabel();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelLoadingIcon();
    }

    public void setLauncherIconEntity(LauncherIconEntity launcherIconEntity) {
        this.launcherIconEntity = launcherIconEntity;
        if (getMeasuredWidth() > 0 && getMeasuredHeight() > 0) {
            loadIcon();
            loadLabel();
        }
    }

    public LauncherIconEntity getLauncherIconEntity() {
        return launcherIconEntity;
    }

    public void refresh() {
        if (launcherIconEntity != null && getMeasuredWidth() > 0 && getMeasuredHeight() > 0) {
            cancelLoadingIcon();
            loadIcon();
            loadLabel();
        }
    }

    public void loadIcon() {
        if (launcherIconEntity == null) {
            return;
        }

        Drawable defIcon = getContext().getPackageManager().getDefaultActivityIcon();
        requestBuilder = Glide.with(this)
                .asBitmap()
                .load(launcherIconEntity)
                .placeholder(defIcon)
                .error(defIcon)
                .diskCacheStrategy(DiskCacheStrategy.ALL);
        requestBuilder.into(imageView);
    }

    public void cancelLoadingIcon() {
        if (requestBuilder != null) {
            try {
                Glide.with(this).clear(imageView);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Cannot cancel loading icon.", e);
            }
            requestBuilder = null;
        }
    }


    public void loadLabel() {
        if (launcherIconEntity == null) {
            return;
        }

        ActivityInfo activityInfo = ApplicationUtils.getActivityInfo(getContext(),
                launcherIconEntity.packageName, launcherIconEntity.activityName);
        textView.setText(ApplicationUtils.getActivityLabel(getContext(), activityInfo));
        textView.requestLayout();
    }
}