package com.qch.sumelauncher.ui.launcher.page;

import android.content.Context;
import android.graphics.Canvas;
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
import androidx.core.os.BundleCompat;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.compat.MapCompat;
import com.qch.sumelauncher.data.model.launcher.Coordinate;
import com.qch.sumelauncher.data.model.launcher.GridSize;
import com.qch.sumelauncher.data.model.launcher.IconModel;
import com.qch.sumelauncher.ui.launcher.item.IconView;
import com.qch.sumelauncher.utils.UnitUtils;
import com.qch.sumelauncher.utils.ViewUtils;

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

    // Data
    private int numColumns = GridSize.DEFAULT_NUM_COLUMN;
    private int numRows = GridSize.DEFAULT_NUM_ROW;
    private int borderHorizontalPaddingPx;
    private int borderVerticalPaddingPx;
    private boolean isEditMode = false;
    private Map<Coordinate, IconModel> iconModelMap = new ConcurrentHashMap<>();
    private Map<Coordinate, IconView> iconViewMap = new ConcurrentHashMap<>();

    // listener interface
    public interface OnIconClickListener {
        void onClick(@Nullable View view, IconModel item);

        boolean onLongClick(@Nullable View view, IconModel item);
    }

    public interface OnBlankAreaClickListener {
        void onClick(int x, int y);

        boolean onLongClick(View anchorView, float x, float y, int cellX, int cellY);
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
        borderHorizontalPaddingPx = context.getResources().getDimensionPixelSize(R.dimen.launcher_layout_horizontal_padding);
        borderVerticalPaddingPx = context.getResources().getDimensionPixelSize(R.dimen.launcher_layout_vertical_padding);
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
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
        int measuredWidth;
        int measuredHeight;

        cellWidth = (maxWidth - 2 * borderHorizontalPaddingPx) / numColumns;
        cellHeight = (maxHeight - 2 * borderVerticalPaddingPx) / numRows;

        contentWidth = numColumns * cellWidth;
        contentHeight = numRows * cellHeight;

        measuredWidth = contentWidth + 2 * borderHorizontalPaddingPx;
        measuredHeight = contentHeight + 2 * borderVerticalPaddingPx;

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

        // Measure all child views
        for (IconView item : iconViewMap.values()) {
            int childWidthSpec = MeasureSpec.makeMeasureSpec(cellWidth, MeasureSpec.AT_MOST);
            int childHeightSpec = MeasureSpec.makeMeasureSpec(cellHeight, MeasureSpec.AT_MOST);
            item.measure(childWidthSpec, childHeightSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (MapCompat.isNullOrEmpty(iconModelMap) || MapCompat.isNullOrEmpty(iconViewMap)) {
            return;
        }
        for (Map.Entry<Coordinate, IconView> entry : iconViewMap.entrySet()) {
            Coordinate coordinate = entry.getKey();
            IconView iconView = entry.getValue();
            // Skip null iconView
            if (iconView == null) {
                continue;
            }
            // Find position of iconView
            int col = coordinate.getX();
            int row = coordinate.getY();
            int width = iconView.getMeasuredWidth();
            int height = iconView.getMeasuredHeight();
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
            iconView.layout(itemLeft, itemTop, itemRight, itemBottom);
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
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
            numColumns = bundle.getInt(NUM_COLUMNS, GridSize.DEFAULT_NUM_COLUMN);
            numRows = bundle.getInt(NUM_ROWS, GridSize.DEFAULT_NUM_ROW);
            Parcelable parcelable = BundleCompat.getParcelable(bundle, PARCELABLE, Parcelable.class);
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
                View anchorView = ViewUtils.getDummyAnchorView(LauncherLayout.this);
                if (anchorView == null) {
                    Log.e(TAG, "Failed to get dummy anchor view.");
                    return;
                }
                anchorView.setX(x);
                anchorView.setY(y);
                onBlankAreaClickListener.onLongClick(anchorView, x, y, cellX, cellY);
            }
        };
        postDelayed(longPressRunnable, longPressTimeout);
    }

    private boolean cancelLongPressDetection() {
        return removeCallbacks(longPressRunnable);
    }

    @Nullable
    public Coordinate getItemCoordinateAtPosition(float x, float y) {
        // If touching point is out of bound, return null
        if (x < borderHorizontalPaddingPx
                || y < borderVerticalPaddingPx
                || x > getWidth() - borderHorizontalPaddingPx
                || y > getHeight() - borderVerticalPaddingPx) {
            return null;
        }
        // Try to get column and row
        int col = (int) ((x - borderHorizontalPaddingPx) / cellWidth);
        int row = (int) ((y - borderVerticalPaddingPx) / cellHeight);
        // If column or row is out of bound, return null
        if (col >= numColumns || row >= numRows) {
            return null;
        }
        // Return the position of touched cell item
        return new Coordinate(col, row);
    }

    @Nullable
    public IconModel getItemAtPosition(float x, float y) {
        Coordinate coordinate = getItemCoordinateAtPosition(x, y);
        if (coordinate == null) {
            return null;
        }
        return iconModelMap.get(coordinate);
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
        postInvalidate();
    }

    public void setItems(List<IconModel> itemList) {
        removeAllViews();
        iconViewMap.clear();
        if (itemList == null) {
            this.iconViewMap = new ConcurrentHashMap<>();
            Log.i(TAG, "Cleared iconViewMap through passing null to setItems().");
        } else {
            Log.i(TAG, "Size of new list is " + itemList.size());
            for (int i = 0; i < itemList.size(); i++) {
                IconModel iconModel = itemList.get(i);
                if (iconModel == null) {
                    continue;
                }
                Coordinate coordinate = new Coordinate(iconModel.getCellX(), iconModel.getCellY());
                iconModelMap.put(coordinate, iconModel);
                IconView iconView = new IconView(getContext());
                iconView.setActivityModel(iconModel.getActivityModel());
                iconView.setOnClickListener(v -> {
                    onIconClickListener.onClick(v, iconModel);
                });
                iconView.setOnLongClickListener(v -> {
                    return onIconClickListener.onLongClick(v, iconModel);
                });
                iconViewMap.put(coordinate, iconView);
                LayoutParams lp = new LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT
                );
                addView(iconView, lp);
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

    /**
     * Remove the IconView specified by the given IconModel instance and return its coordinate.
     */
    public Coordinate removeIconView(IconModel iconModel) {
        Coordinate coordinate = new Coordinate(iconModel.getCellX(), iconModel.getCellY());
        iconViewMap.remove(coordinate);
        return coordinate;
    }
}