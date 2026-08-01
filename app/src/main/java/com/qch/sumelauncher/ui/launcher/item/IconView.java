package com.qch.sumelauncher.ui.launcher.item;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.qch.sumelauncher.R;
import com.qch.sumelauncher.data.model.launcher.ActivityModel;

public class IconView extends AppCompatTextView {
    private static final String TAG = "IconView";
    private final int iconSizePx;
    private final int spaceHeightPx;
    private final int labelWidthPx;
    private final int labelSizePx;
    private final int horizontalPaddingPx;
    private final int verticalPaddingPx;
    private ActivityModel activityModel;
    private boolean isContentLoaded = false;

    public IconView(@NonNull Context context) {
        this(context, null, 0);
    }

    public IconView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // Get sizes
        iconSizePx = context.getResources().getDimensionPixelSize(R.dimen.app_icon_size);
        spaceHeightPx = context.getResources().getDimensionPixelSize(R.dimen.icon_view_space);
        labelWidthPx = context.getResources().getDimensionPixelSize(R.dimen.app_label_width);
        labelSizePx = context.getResources().getDimensionPixelSize(R.dimen.app_label_size);
        horizontalPaddingPx = context.getResources().getDimensionPixelSize(R.dimen.icon_view_horizontal_padding);
        verticalPaddingPx = context.getResources().getDimensionPixelSize(R.dimen.icon_view_vertical_padding);
        // Initialize
        setWidth(labelWidthPx);
        setTextSize(TypedValue.COMPLEX_UNIT_PX, labelSizePx);
        setEllipsize(TextUtils.TruncateAt.END);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setMaxLines(1);
        setCompoundDrawablePadding(spaceHeightPx);
        setPadding(horizontalPaddingPx, verticalPaddingPx,
                horizontalPaddingPx, verticalPaddingPx);
        // Set a placeholder
        Drawable defaultIcon = context.getPackageManager().getDefaultActivityIcon();
        defaultIcon.setBounds(0, 0, iconSizePx, iconSizePx);
        setCompoundDrawables(null, defaultIcon, null, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);

        int measuredWidth = Math.max(iconSizePx, labelWidthPx) + 2 * horizontalPaddingPx;

        Paint paint = getPaint();
        paint.setTextSize(labelSizePx);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float textHeight = fontMetrics.descent - fontMetrics.ascent;
        int measuredHeight = iconSizePx + Math.round(textHeight) + labelSizePx + 2 * verticalPaddingPx;

        switch (widthMode) {
            case MeasureSpec.EXACTLY: {
                measuredWidth = maxWidth;
                break;
            }
            case MeasureSpec.AT_MOST: {
                measuredWidth = Math.min(measuredWidth, maxWidth);
                break;
            }
            case MeasureSpec.UNSPECIFIED: {
                break;
            }
        }

        switch (heightMode) {
            case MeasureSpec.EXACTLY: {
                measuredHeight = maxHeight;
                break;
            }
            case MeasureSpec.AT_MOST: {
                measuredHeight = Math.min(measuredHeight, maxHeight);
                break;
            }
            case MeasureSpec.UNSPECIFIED: {
                break;
            }
        }

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        reloadContentIfNeeded(width, height);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        reloadContentIfNeeded();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelLoadingContent();
        isContentLoaded = false;
    }

    public void setActivityModel(@Nullable ActivityModel activityModel) {
        if (this.activityModel != activityModel) {
            cancelLoadingContent();
            isContentLoaded = false;
        }
        this.activityModel = activityModel;
        reloadContentIfNeeded();
    }

    @Nullable
    public ActivityModel getActivityModel() {
        return activityModel;
    }

    public void loadContent() {
        if (activityModel == null || isContentLoaded) {
            return;
        }
        // Set icon
        Drawable defaultIcon = getContext().getPackageManager().getDefaultActivityIcon();
        defaultIcon.setBounds(0, 0, iconSizePx, iconSizePx);

        Glide.with(this)
                .asDrawable()
                .load(activityModel)
                .override(iconSizePx, iconSizePx)
                .placeholder(defaultIcon)
                .error(defaultIcon)
                .into(new CustomTarget<Drawable>(iconSizePx, iconSizePx) {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource,
                                                @Nullable Transition<? super Drawable> transition) {
                        resource.setBounds(0, 0, iconSizePx, iconSizePx);
                        setCompoundDrawables(null, resource, null, null);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        setCompoundDrawables(null, defaultIcon, null, null);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        setCompoundDrawables(null, defaultIcon, null, null);
                    }
                });
        // Set text
        setText(activityModel.getLabel());

        isContentLoaded = true;
    }

    public void cancelLoadingContent() {
        try {
            Glide.with(this).clear(this);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Failed to cancel loading content.", e);
        }
    }

    public void reloadContentIfNeeded() {
        if (getMeasuredWidth() > 0 && getMeasuredHeight() > 0 && activityModel != null && !isContentLoaded) {
            loadContent();
        }
    }

    public void reloadContentIfNeeded(int width, int height) {
        if (width > 0 && height > 0 && activityModel != null && !isContentLoaded) {
            loadContent();
        }
    }
}