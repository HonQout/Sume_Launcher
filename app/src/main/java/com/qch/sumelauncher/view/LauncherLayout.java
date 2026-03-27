package com.qch.sumelauncher.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.room.entity.LauncherIconEntity;
import com.qch.sumelauncher.utils.UnitUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class LauncherLayout extends ViewGroup {
    private static final String TAG = "LauncherLayout";
    private static final String PARCELABLE = "PARCELABLE";
    private static final String NUM_ROWS = "NUM_ROWS";
    private static final String NUM_COLUMNS = "NUM_COLUMNS";

    // Grid
    private int cellWidth;
    private int cellHeight;
    private int contentWidth;
    private int contentHeight;

    // Painter
    private final Paint gridPaint;

    // Data
    private int numRows = 5;
    private int numColumns = 5;
    private int borderHorizontalPaddingPx;
    private int borderVerticalPaddingPx;
    private int gridHorizontalPaddingPx;
    private int gridVerticalPaddingPx;
    private List<LauncherIconEntity> entityList = new ArrayList<>();
    private List<LauncherIconView> viewList = new ArrayList<>();
    private final AtomicBoolean isAccessingList = new AtomicBoolean(false);
    private final Object accessListLock = new Object();

    // listener interface
    public interface OnItemClickListener {
        void onItemClick(LauncherIconEntity item, int position);

        boolean onItemLongClick(LauncherIconEntity item, int position, float x, float y, @Nullable View view);
    }

    // interaction
    private OnItemClickListener onItemClickListener;
    private int longPressTimeout;
    private Runnable longPressRunnable;
    private boolean hasPerformedLongPress = false;
    private int touchSlop;
    private float touchedX;
    private float touchedY;

    public LauncherLayout(Context context) {
        this(context, null, 0, 0);
    }

    public LauncherLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public LauncherLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public LauncherLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr,
                          int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        // Get sizes
        borderHorizontalPaddingPx = UnitUtils.dpToPx(context,
                context.getResources().getDimensionPixelSize(R.dimen.app_border_horizontal_padding));
        borderVerticalPaddingPx = UnitUtils.dpToPx(context,
                context.getResources().getDimensionPixelSize(R.dimen.app_border_vertical_padding));
        gridHorizontalPaddingPx = UnitUtils.dpToPx(context,
                context.getResources().getDimensionPixelSize(R.dimen.grid_horizontal_padding));
        gridVerticalPaddingPx = UnitUtils.dpToPx(context,
                context.getResources().getDimensionPixelSize(R.dimen.grid_vertical_padding));
        // Initialize painters
        gridPaint = new Paint();
        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1);
        // Initialize interaction
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        longPressTimeout = ViewConfiguration.getLongPressTimeout();
        touchSlop = viewConfiguration.getScaledTouchSlop();
        // Receive touch event
        setClickable(true);
        setFocusable(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i(TAG, "onMeasure");
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int measuredWidth;
        int measuredHeight;

        cellWidth = (widthSize - 2 * borderHorizontalPaddingPx) / numColumns;
        cellHeight = (heightSize - 2 * borderVerticalPaddingPx) / numRows;

        contentWidth = numColumns * cellWidth;
        contentHeight = numRows * cellHeight;

        measuredWidth = contentWidth + 2 * borderHorizontalPaddingPx;
        measuredHeight = contentHeight + 2 * borderVerticalPaddingPx;

        switch (widthMode) {
            case MeasureSpec.EXACTLY -> {
                measuredWidth = widthSize;
            }
            case MeasureSpec.AT_MOST -> {
                measuredWidth = Math.min(measuredWidth, widthSize);
            }
            case MeasureSpec.UNSPECIFIED -> {

            }
        }

        switch (heightMode) {
            case MeasureSpec.EXACTLY -> {
                measuredHeight = heightSize;
            }
            case MeasureSpec.AT_MOST -> {
                measuredHeight = Math.min(measuredHeight, heightSize);
            }
            case MeasureSpec.UNSPECIFIED -> {

            }
        }

        setMeasuredDimension(measuredWidth, measuredHeight);

        // Measure all child views
        if (isAccessingList.compareAndSet(false, true)) {
            try {
                synchronized (accessListLock) {
                    for (LauncherIconView item : viewList) {
                        int childWidthSpec = MeasureSpec.makeMeasureSpec(cellWidth, MeasureSpec.EXACTLY);
                        int childHeightSpec = MeasureSpec.makeMeasureSpec(cellHeight, MeasureSpec.EXACTLY);
                        item.measure(childWidthSpec, childHeightSpec);
                    }
                }
            } finally {
                isAccessingList.set(false);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (entityList == null || entityList.isEmpty()) {
            return;
        }
        if (isAccessingList.compareAndSet(false, true)) {
            try {
                synchronized (accessListLock) {
                    for (LauncherIconView item : viewList) {
                        LauncherIconEntity entity = item.getLauncherIconEntity();
                        if (entity == null) {
                            continue;
                        }
                        int col = entity.cellX;
                        int row = entity.cellY;

                        int cellLeft = borderHorizontalPaddingPx + col * cellWidth;
                        int cellTop = borderVerticalPaddingPx + row * cellHeight;
                        int cellRight = cellLeft + cellWidth;
                        int cellBottom = cellTop + cellHeight;

                        item.layout(cellLeft, cellTop, cellRight, cellBottom);
                    }
                }
            } finally {
                isAccessingList.set(false);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int index = getItemIndexAtPosition(x, y);
        LauncherIconEntity item = index >= 0 ? entityList.get(index) : null;
        if (index == -1 || item == null) {
            Log.i(TAG, "Pressed item is null.");
            return false;
        }
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN -> {
                Log.i(TAG, "Pressed at " + item.cellX + "," + item.cellY);
                hasPerformedLongPress = false;
                startLongPressDetection(x, y, index, item);
                setPressed(true);
                return true;
            }

            case MotionEvent.ACTION_UP -> {
                Log.i(TAG, "Released at " + item.cellX + "," + item.cellY);
                cancelLongPressDetection();
                setPressed(false);
                if (!hasPerformedLongPress && onItemClickListener != null) {
                    onItemClickListener.onItemClick(item, index);
                }
                return true;
            }

            case MotionEvent.ACTION_MOVE -> {
                float dx = Math.abs(event.getX() - touchedX);
                float dy = Math.abs(event.getY() - touchedY);
                if (dx > touchSlop || dy > touchSlop) {
                    cancelLongPressDetection();
                    setPressed(false);
                }
                return true;
            }

            case MotionEvent.ACTION_CANCEL -> {
                cancelLongPressDetection();
                setPressed(false);
                hasPerformedLongPress = false;
                return true;
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Cancel long press detection
        cancelLongPressDetection();
        longPressRunnable = null;
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable parcelable = super.onSaveInstanceState();
        Bundle savedState = new Bundle();
        savedState.putParcelable(PARCELABLE, parcelable);
        savedState.putInt(NUM_ROWS, numRows);
        savedState.putInt(NUM_COLUMNS, numColumns);
        return parcelable;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle bundle) {
            numRows = bundle.getInt(NUM_ROWS, 5);
            numColumns = bundle.getInt(NUM_COLUMNS, 5);
            Parcelable parcelable = bundle.getParcelable(PARCELABLE);
            super.onRestoreInstanceState(parcelable);
            postInvalidate();
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    private void startLongPressDetection(float x, float y, int index, LauncherIconEntity item) {
        touchedX = x;
        touchedY = y;
        longPressRunnable = () -> {
            hasPerformedLongPress = true;
            boolean handledBySystem = super.performLongClick();
            Log.i(TAG, "Long press handled by system: " + handledBySystem);
            if (onItemClickListener != null) {
                onItemClickListener.onItemLongClick(item, index, x, y, getChildAt(index));
            }
        };
        postDelayed(longPressRunnable, longPressTimeout);
    }

    private boolean cancelLongPressDetection() {
        return removeCallbacks(longPressRunnable);
    }

    public int getItemIndexAtPosition(float x, float y) {
        // If touching point is out of bound, return -1
        if (x < borderHorizontalPaddingPx
                || y < borderVerticalPaddingPx
                || x > getWidth() - borderHorizontalPaddingPx
                || y > getHeight() - borderVerticalPaddingPx) {
            return -1;
        }
        // Try to get column and row
        int col = (int) ((x - borderHorizontalPaddingPx) / cellWidth);
        int row = (int) ((y - borderVerticalPaddingPx) / cellHeight);
        // If column or row is out of bound, return -1
        if (col >= numColumns || row >= numRows) {
            return -1;
        }
        // Return the index of touched cell item
        int index = row * numColumns + col;
        return index < entityList.size() ? index : -1;
    }

    @Nullable
    public LauncherIconEntity getItemAtPosition(float x, float y) {
        int index = getItemIndexAtPosition(x, y);
        return index >= 0 ? entityList.get(index) : null;
    }

    public void setNumRows(int numRows) {
        if (numRows > 0 && this.numRows != numRows) {
            this.numRows = numRows;
            requestLayout();
            invalidate();
        }
    }

    public void setNumColumns(int numColumns) {
        if (numColumns > 0 && this.numColumns != numColumns) {
            this.numColumns = numColumns;
            requestLayout();
            invalidate();
        }
    }

    public void setGridSize(int numRows, int numColumns) {
        boolean valueSet = false;
        if (numRows > 0 && this.numRows != numRows) {
            this.numRows = numRows;
            valueSet = true;
        }
        if (numColumns > 0 && this.numColumns != numColumns) {
            this.numColumns = numColumns;
            valueSet = true;
        }
        if (valueSet) {
            requestLayout();
            invalidate();
        }
    }

    public void setGridPadding(int gridHorizontalPaddingDp, int gridVerticalPaddingDp) {
        if (gridHorizontalPaddingDp >= 0 && gridVerticalPaddingDp >= 0) {
            this.gridHorizontalPaddingPx = UnitUtils.dpToPx(getContext(), gridHorizontalPaddingDp);
            this.gridVerticalPaddingPx = UnitUtils.dpToPx(getContext(), gridVerticalPaddingDp);
            requestLayout();
            invalidate();
        } else {
            throw new IllegalArgumentException("Grid padding cannot be less than 0.");
        }
    }

    public void setGridHorizontalPadding(int gridHorizontalPaddingDp) {
        if (gridHorizontalPaddingDp >= 0) {
            this.gridHorizontalPaddingPx = UnitUtils.dpToPx(getContext(), gridHorizontalPaddingDp);
            requestLayout();
            invalidate();
        } else {
            throw new IllegalArgumentException("Grid horizontal padding cannot be less than 0.");
        }
    }

    public void setGridVerticalPadding(int gridVerticalPaddingDp) {
        if (gridVerticalPaddingDp >= 0) {
            this.gridVerticalPaddingPx = UnitUtils.dpToPx(getContext(), gridVerticalPaddingDp);
            requestLayout();
            invalidate();
        } else {
            throw new IllegalArgumentException("Grid vertical padding cannot be less than 0.");
        }
    }

    public void setBorderPadding(int borderHorizontalPaddingDp, int borderVerticalPaddingDp) {
        if (borderHorizontalPaddingDp >= 0 && borderVerticalPaddingDp >= 0) {
            this.borderHorizontalPaddingPx = UnitUtils.dpToPx(getContext(), borderHorizontalPaddingDp);
            this.borderVerticalPaddingPx = UnitUtils.dpToPx(getContext(), borderVerticalPaddingDp);
            requestLayout();
            invalidate();
        } else {
            throw new IllegalArgumentException("Border padding cannot be less than 0.");
        }
    }

    public void setBorderHorizontalPadding(int borderHorizontalPaddingDp) {
        if (borderHorizontalPaddingDp >= 0) {
            this.borderHorizontalPaddingPx = UnitUtils.dpToPx(getContext(), borderHorizontalPaddingDp);
            requestLayout();
            invalidate();
        } else {
            throw new IllegalArgumentException("Border horizontal padding cannot be less than 0.");
        }
    }

    public void setBorderVerticalPadding(int borderVerticalPaddingDp) {
        if (borderVerticalPaddingDp >= 0) {
            this.borderVerticalPaddingPx = UnitUtils.dpToPx(getContext(), borderVerticalPaddingDp);
            requestLayout();
            invalidate();
        } else {
            throw new IllegalArgumentException("Border vertical padding cannot be less than 0.");
        }
    }

    public void setEntityList(List<LauncherIconEntity> entityList) {
        if (isAccessingList.compareAndSet(false, true)) {
            try {
                synchronized (accessListLock) {
                    if (entityList == null) {
                        this.entityList = new ArrayList<>();
                        removeAllViews();
                        viewList.clear();
                        Log.i(TAG, "Cleared list through passing null to setList.");
                    } else {
                        this.entityList = new ArrayList<>(entityList);
                        Log.i(TAG, "Size of new list is " + entityList.size());
                        removeAllViews();
                        viewList.clear();
                        for (int i = 0; i < entityList.size(); i++) {
                            LauncherIconView launcherIconView = new LauncherIconView(getContext());
                            launcherIconView.setLauncherIconEntity(entityList.get(i));
                            viewList.add(launcherIconView);
                            addView(launcherIconView);
                        }
                    }
                    requestLayout();
                    postInvalidate();
                }
            } finally {
                isAccessingList.set(false);
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
}