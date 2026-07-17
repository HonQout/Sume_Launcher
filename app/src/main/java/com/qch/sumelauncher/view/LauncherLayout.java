package com.qch.sumelauncher.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.launcher.page.Coordinate;
import com.qch.sumelauncher.room.entity.IconEntity;
import com.qch.sumelauncher.utils.UnitUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LauncherLayout extends ViewGroup {
    private static final String TAG = "LauncherLayout";
    private static final String PARCELABLE = "PARCELABLE";
    private static final String NUM_COLUMNS = "NUM_COLUMNS";
    private static final String NUM_ROWS = "NUM_ROWS";

    // Grid
    private int cellWidth;
    private int cellHeight;
    private int contentWidth;
    private int contentHeight;

    // Painter
    private final Paint gridPaint;

    // Data
    private int numColumns = 5;
    private int numRows = 5;
    private int borderHorizontalPaddingPx;
    private int borderVerticalPaddingPx;
    private int gridHorizontalPaddingPx;
    private int gridVerticalPaddingPx;
    private boolean isEditMode = false;
    private Drawable plusDrawable;
    private Map<Coordinate, IconEntity> iconEntityMap = new ConcurrentHashMap<>();
    private Map<Coordinate, LauncherIconView> iconViewMap = new ConcurrentHashMap<>();

    // listener interface
    public interface OnIconClickListener {
        void onIconClick(@Nullable View view, IconEntity item);

        boolean onIconLongClick(@Nullable View view, IconEntity item);
    }

    public interface OnBlankAreaClickListener {
        void onBlankAreaClick(int x, int y);

        boolean onBlankAreaLongClick(int x, int y);
    }

    // interaction
    private OnIconClickListener onIconClickListener;
    private OnBlankAreaClickListener onBlankAreaClickListener;
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
                context.getResources().getDimensionPixelSize(R.dimen.launcher_layout_horizontal_padding));
        borderVerticalPaddingPx = UnitUtils.dpToPx(context,
                context.getResources().getDimensionPixelSize(R.dimen.launcher_layout_vertical_padding));
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
            case MeasureSpec.EXACTLY: {
                measuredWidth = widthSize;
            }
            case MeasureSpec.AT_MOST: {
                measuredWidth = Math.min(measuredWidth, widthSize);
            }
            case MeasureSpec.UNSPECIFIED: {

            }
        }

        switch (heightMode) {
            case MeasureSpec.EXACTLY: {
                measuredHeight = heightSize;
            }
            case MeasureSpec.AT_MOST: {
                measuredHeight = Math.min(measuredHeight, heightSize);
            }
            case MeasureSpec.UNSPECIFIED: {

            }
        }

        setMeasuredDimension(measuredWidth, measuredHeight);

        // Measure all child views
        for (LauncherIconView item : iconViewMap.values()) {
            int childWidthSpec = MeasureSpec.makeMeasureSpec(cellWidth, MeasureSpec.EXACTLY);
            int childHeightSpec = MeasureSpec.makeMeasureSpec(cellHeight, MeasureSpec.EXACTLY);
            item.measure(childWidthSpec, childHeightSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (iconEntityMap == null || iconEntityMap.isEmpty()) {
            return;
        }
        for (LauncherIconView item : iconViewMap.values()) {
            IconEntity iconEntity = item.getLauncherIconEntity();
            // Skip null item
            if (iconEntity == null) {
                continue;
            }
            // Find position of item
            int col = iconEntity.getCellX();
            int row = iconEntity.getCellY();
            int width = item.getMeasuredWidth();
            int height = item.getMeasuredHeight();
            int itemHorizontalMargin = 0;
            int itemVerticalMargin = 0;
            if (width <= cellWidth) {
                itemHorizontalMargin = (cellWidth - width) / 2;
            }
            if (height <= cellHeight) {
                itemVerticalMargin = (cellHeight - height) / 2;
            }
            int cellLeft = borderHorizontalPaddingPx + col * cellWidth;
            int cellTop = borderVerticalPaddingPx + row * cellHeight;
            int cellRight = cellLeft + cellWidth;
            int cellBottom = cellTop + cellHeight;
            int itemLeft = cellLeft + itemHorizontalMargin;
            int itemTop = cellTop + itemVerticalMargin;
            int itemRight = cellRight - itemHorizontalMargin;
            int itemBottom = cellBottom - itemVerticalMargin;
            item.layout(itemLeft, itemTop, itemRight, itemBottom);
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (isEditMode) {

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        Coordinate coordinate = getItemCoordinateAtPosition(x, y);
        if (coordinate == null) {
            Log.i(TAG, "Cannot get position of pressed cell.");
            return super.onTouchEvent(event);
        }
        int col = coordinate.getX();
        int row = coordinate.getY();
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                Log.i(TAG, "Pressed at " + col + "," + row);
                hasPerformedLongPress = false;
                startLongPressDetection(x, y, col, row);
                setPressed(true);
                return true;
            }

            case MotionEvent.ACTION_UP: {
                Log.i(TAG, "Released at " + col + "," + row);
                cancelLongPressDetection();
                setPressed(false);
                if (!hasPerformedLongPress && onIconClickListener != null) {
                    performClick();
                }
                return true;
            }

            case MotionEvent.ACTION_MOVE: {
                float dx = Math.abs(event.getX() - touchedX);
                float dy = Math.abs(event.getY() - touchedY);
                if (dx > touchSlop || dy > touchSlop) {
                    cancelLongPressDetection();
                    setPressed(false);
                }
                return true;
            }

            case MotionEvent.ACTION_CANCEL: {
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
    public boolean performLongClick() {
        return super.performLongClick();
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
        savedState.putInt(NUM_COLUMNS, numColumns);
        savedState.putInt(NUM_ROWS, numRows);
        return parcelable;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            numColumns = bundle.getInt(NUM_COLUMNS, 5);
            numRows = bundle.getInt(NUM_ROWS, 5);
            Parcelable parcelable = bundle.getParcelable(PARCELABLE);
            super.onRestoreInstanceState(parcelable);
            postInvalidate();
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    private void startLongPressDetection(float x, float y, int cellX, int cellY) {
        touchedX = x;
        touchedY = y;
        longPressRunnable = () -> {
            hasPerformedLongPress = true;
            boolean handledBySystem = super.performLongClick();
            Log.i(TAG, "Long press handled by system: " + handledBySystem);
            if (onBlankAreaClickListener != null) {
                onBlankAreaClickListener.onBlankAreaLongClick(cellX, cellY);
            }
        };
        postDelayed(longPressRunnable, longPressTimeout);
    }

    private boolean cancelLongPressDetection() {
        return removeCallbacks(longPressRunnable);
    }

    @Nullable
    public Coordinate getItemCoordinateAtPosition(float x, float y) {
        // If touching point is out of bound, return -1
        if (x < borderHorizontalPaddingPx
                || y < borderVerticalPaddingPx
                || x > getWidth() - borderHorizontalPaddingPx
                || y > getHeight() - borderVerticalPaddingPx) {
            return null;
        }
        // Try to get column and row
        int col = (int) ((x - borderHorizontalPaddingPx) / cellWidth);
        int row = (int) ((y - borderVerticalPaddingPx) / cellHeight);
        // If column or row is out of bound, return -1
        if (col >= numColumns || row >= numRows) {
            return null;
        }
        // Return the position of touched cell item
        return new Coordinate(col, row);
    }

    @Nullable
    public IconEntity getItemAtPosition(float x, float y) {
        Coordinate coordinate = getItemCoordinateAtPosition(x, y);
        if (coordinate == null) {
            return null;
        }
        return iconEntityMap.get(coordinate);
    }

    public void setNumColumns(int numColumns) {
        if (numColumns > 0 && this.numColumns != numColumns) {
            this.numColumns = numColumns;
            requestLayout();
            invalidate();
        }
    }

    public void setNumRows(int numRows) {
        if (numRows > 0 && this.numRows != numRows) {
            this.numRows = numRows;
            requestLayout();
            invalidate();
        }
    }

    public void setGridSize(int numColumns, int numRows) {
        boolean valueSet = false;
        if (numColumns > 0 && this.numColumns != numColumns) {
            this.numColumns = numColumns;
            valueSet = true;
        }
        if (numRows > 0 && this.numRows != numRows) {
            this.numRows = numRows;
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

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
        invalidate();
    }

    public void setIconEntityList(List<IconEntity> iconEntityList) {
        removeAllViews();
        iconViewMap.clear();
        if (iconEntityList == null) {
            this.iconEntityMap = new ConcurrentHashMap<>();
            Log.i(TAG, "Cleared iconEntityMap through passing null to setIconEntityList().");
        } else {
            Log.i(TAG, "Size of new list is " + iconEntityList.size());
            for (int i = 0; i < iconEntityList.size(); i++) {
                IconEntity iconEntity = iconEntityList.get(i);
                Coordinate coordinate = new Coordinate(iconEntity.getCellX(), iconEntity.getCellY());
                iconEntityMap.put(coordinate, iconEntity);
                LauncherIconView launcherIconView = new LauncherIconView(getContext());
                launcherIconView.setLauncherIconEntity(iconEntity);
                launcherIconView.setOnIconClickListener(v -> {
                    onIconClickListener.onIconClick(v, iconEntity);
                });
                launcherIconView.setOnIconLongClickListener(v -> {
                    return onIconClickListener.onIconLongClick(v, iconEntity);
                });
                iconViewMap.put(coordinate, launcherIconView);
                addView(launcherIconView);
            }
        }
        if (!isInLayout()) {
            requestLayout();
        }
        postInvalidate();
    }

    public void setOnIconClickListener(OnIconClickListener listener) {
        this.onIconClickListener = listener;
    }

    public void setOnBlankClickListener(OnBlankAreaClickListener listener) {
        this.onBlankAreaClickListener = listener;
    }

    public Coordinate removeIconView(IconEntity iconEntity) {
        Coordinate coordinate = new Coordinate(iconEntity.getCellX(), iconEntity.getCellY());
        iconViewMap.remove(coordinate);
        return coordinate;
    }
}