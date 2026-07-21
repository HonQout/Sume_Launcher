package com.qch.sumelauncher.topbar.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qch.sumelauncher.R;

public class BatteryView extends View {
    private final float horizontalPadding = 2f;
    private final float verticalPadding = 2f;
    private final float borderWidth = 2f;
    private final float borderCornerRadius = 12f;
    private final float fillCornerRadius = 8f;
    private int level = 100;
    private boolean isCharging = false;

    private Paint borderPaint;
    private Paint capPaint;
    private Paint fillPaint;
    private Paint lightningPaint;

    private RectF bodyRect = new RectF();
    private RectF capRect = new RectF();
    private RectF fillRect = new RectF();
    private Path lightningPath = new Path();

    public BatteryView(Context context) {
        this(context, null, 0, 0);
    }

    public BatteryView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public BatteryView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BatteryView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        int borderColor = getContext().getColor(R.color.batteryBorderColor);
        int capColor = getContext().getColor(R.color.batteryCapColor);
        int fillColor = getContext().getColor(R.color.batteryFillColor);
        int lightningColor = getContext().getColor(R.color.batteryLightningColor);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);
        borderPaint.setColor(borderColor);

        capPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        capPaint.setStyle(Paint.Style.FILL);
        capPaint.setColor(capColor);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(fillColor);

        lightningPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        lightningPaint.setStyle(Paint.Style.FILL);
        lightningPaint.setColor(lightningColor);
    }

    public void setLevel(int level) {
        this.level = Math.max(0, Math.min(100, level));
        postInvalidate();
    }

    public void setCharging(boolean isCharging) {
        this.isCharging = isCharging;
        postInvalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            height = (int) (width * 0.4f);
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        calculateBatteryRects(getWidth(), getHeight());
        canvas.drawRoundRect(bodyRect, borderCornerRadius, borderCornerRadius, borderPaint);
        canvas.drawRect(capRect, capPaint);
        canvas.drawRoundRect(fillRect, fillCornerRadius, fillCornerRadius, fillPaint);

        if (isCharging) {
            calculateLightningPath();
            canvas.drawPath(lightningPath, lightningPaint);
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        invalidate();
    }

    private void calculateBatteryRects(int width, int height) {
        float mHorizontalPadding = borderWidth / 2 + horizontalPadding;
        float mVerticalPadding = borderWidth / 2 + verticalPadding;

        float capWidth = width * 0.10f;
        float capHeight = height * 0.40f;
        float bodyWidth = width - 2 * mHorizontalPadding - capWidth;
        float bodyHeight = height - 2 * mVerticalPadding;

        float fillMargin = borderWidth / 2 + 2f;
        float fillLeft = mHorizontalPadding + fillMargin;
        float fillTop = mVerticalPadding + fillMargin;
        float fillWidth = (bodyWidth - 2 * fillMargin) * level / 100f;
        float fillHeight = bodyHeight - 2 * fillMargin;

        bodyRect.set(mHorizontalPadding, mVerticalPadding,
                width - mHorizontalPadding - capWidth, height - mVerticalPadding);
        capRect.set(mHorizontalPadding + bodyWidth, mVerticalPadding + (bodyHeight - capHeight) / 2,
                width - mHorizontalPadding, height - mVerticalPadding - (bodyHeight - capHeight) / 2);
        fillRect.set(fillLeft, fillTop, fillLeft + fillWidth, fillTop + fillHeight);
    }

    private void calculateLightningPath() {
        float centerX = bodyRect.centerX();
        float centerY = bodyRect.centerY();

        float lHeight = bodyRect.height() * 0.9f;
        float lWidth = lHeight * 0.5f;

        lightningPath.reset();

        lightningPath.moveTo(centerX + lWidth * 0.2f, centerY - lHeight * 0.5f);
        lightningPath.lineTo(centerX - lWidth * 0.5f, centerY + lHeight * 0.05f);
        lightningPath.lineTo(centerX - lWidth * 0.1f, centerY + lHeight * 0.05f);
        lightningPath.lineTo(centerX - lWidth * 0.2f, centerY + lHeight * 0.5f);
        lightningPath.lineTo(centerX + lWidth * 0.5f, centerY - lHeight * 0.05f);
        lightningPath.lineTo(centerX + lWidth * 0.1f, centerY - lHeight * 0.05f);
    }
}
