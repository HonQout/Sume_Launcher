package com.qch.sumelauncher.ui.controlcenter.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.data.model.controlcenter.ControlCenterItemModel;

public class ControlCenterShortcutView extends AppCompatTextView {
    private static final String TAG = "ControlCenterShortcutView";
    private final int iconSizePx;
    private final int spacePx;
    private final int labelWidthPx;
    private final int labelSizePx;
    private final int horizontalPaddingPx;
    private final int verticalPaddingPx;
    private ControlCenterItemModel model;
    private boolean isContentLoaded = false;

    public ControlCenterShortcutView(@NonNull Context context) {
        this(context, null, 0);
    }

    public ControlCenterShortcutView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ControlCenterShortcutView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // Get sizes
        iconSizePx = Math.round(context.getResources().getDimension(R.dimen.shortcut_icon_size));
        spacePx = Math.round(context.getResources().getDimension(R.dimen.shortcut_space));
        labelWidthPx = Math.round(context.getResources().getDimension(R.dimen.shortcut_label_width));
        labelSizePx = Math.round(context.getResources().getDimension(R.dimen.shortcut_label_size));
        horizontalPaddingPx = Math.round(context.getResources().getDimension(R.dimen.shortcut_view_horizontal_padding));
        verticalPaddingPx = Math.round(context.getResources().getDimension(R.dimen.shortcut_view_vertical_padding));
        // Initialize
        setWidth(labelWidthPx);
        setTextSize(TypedValue.COMPLEX_UNIT_PX, labelSizePx);
        setEllipsize(TextUtils.TruncateAt.END);
        setGravity(Gravity.CENTER_VERTICAL);
        setMaxLines(1);
        setCompoundDrawablePadding(spacePx);
        setPadding(horizontalPaddingPx, verticalPaddingPx, horizontalPaddingPx, verticalPaddingPx);
        // Add border
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadius(20f);
        gradientDrawable.setStroke(2, Color.GRAY);
        gradientDrawable.setColor(Color.TRANSPARENT);
        setBackground(gradientDrawable);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);

        int measuredWidth = iconSizePx + spacePx + labelWidthPx + 2 * horizontalPaddingPx;

        Paint paint = getPaint();
        paint.setTextSize(labelSizePx);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float textHeight = fontMetrics.descent - fontMetrics.ascent;
        int measuredHeight = Math.max(iconSizePx, Math.round(textHeight)) + 2 * verticalPaddingPx;

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
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        reloadContentIfNeeded();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isContentLoaded = false;
    }

    public void setModel(@Nullable ControlCenterItemModel model) {
        if (this.model != model) {
            isContentLoaded = false;
            this.model = model;
            loadContent();
        }
    }

    private void loadContent() {
        if (model == null) {
            return;
        }
        Drawable icon = ContextCompat.getDrawable(getContext(), model.getIconRes());
        if (icon != null) {
            icon.setBounds(0, 0, iconSizePx, iconSizePx);
            setCompoundDrawables(icon, null, null, null);
        }
        setText(model.getTitleRes());
    }

    public void reloadContentIfNeeded() {
        reloadContentIfNeeded(getMeasuredWidth(), getMeasuredHeight());
    }

    public void reloadContentIfNeeded(int width, int height) {
        if (width > 0 && height > 0 && model != null && !isContentLoaded) {
            loadContent();
        }
    }
}
