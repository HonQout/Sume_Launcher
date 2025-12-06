package com.qch.sumelauncher.recyclerview;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qch.sumelauncher.R;
import com.qch.sumelauncher.utils.UnitUtils;

public class AppItemDecoration extends RecyclerView.ItemDecoration {
    private static final String TAG = "AppItemDecoration";
    private int numRow;
    private int numColumn;
    private int lastParentHeightPx = 0;
    private int verticalSpacingPx = -1;

    public AppItemDecoration(int numRow, int numColumn) {
        this.numRow = numRow;
        this.numColumn = numColumn;

    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        // check if screen height is >0, which means the view has finished loading process
        Context context = view.getContext();
        int parentHeightPx = parent.getHeight() - UnitUtils.dpToPx(context, 10 * 2);
        if (parentHeightPx <= 0) {
            Log.e(TAG, "The view is still loading.");
            return;
        }
        // calculate again only if height of parent view changed
        if (verticalSpacingPx == -1 || parentHeightPx != lastParentHeightPx) {
            // get layout manager
            RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
            if (layoutManager == null) {
                Log.e(TAG, "Layout manager is null.");
                return;
            }
            // get item height
            int itemHeightPx = 0;
            if (parent.getChildCount() > 0) {
                int iconSizePx = context.getResources().getDimensionPixelSize(R.dimen.app_icon_size);
                int textSizePx = context.getResources().getDimensionPixelSize(R.dimen.app_label_size);
                itemHeightPx = iconSizePx + textSizePx + UnitUtils.dpToPx(context, 10 * 2 + 5);
            }
            if (itemHeightPx <= 0) {
                Log.e(TAG, "Illegal item height.");
                return;
            }
            // calculate remaining space in vertical direction
            int itemHeightTotalPx = numRow * itemHeightPx;
            int remainingHeightPx = parentHeightPx - itemHeightTotalPx;
            // divide it by number of gaps in vertical direction
            if (remainingHeightPx > 0) {
                int numVerticalGaps = numRow + 1;
                verticalSpacingPx = remainingHeightPx / numVerticalGaps;
            } else {
                verticalSpacingPx = 0;
            }
            lastParentHeightPx = parentHeightPx;
        }
        // calculate row index
        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION) {
            Log.e(TAG, "Illegal position.");
            return;
        }
        int rowIndex = position / numColumn;
        // set vertical spacing
        if (verticalSpacingPx > 0) {
            if (rowIndex == 0) {
                outRect.top = verticalSpacingPx;
                outRect.bottom = verticalSpacingPx / 2;
            } else if (rowIndex == numRow - 1) {
                outRect.top = verticalSpacingPx / 2;
                outRect.bottom = verticalSpacingPx;
            } else {
                outRect.top = verticalSpacingPx / 2;
                outRect.bottom = verticalSpacingPx / 2;
            }
        }
    }
}