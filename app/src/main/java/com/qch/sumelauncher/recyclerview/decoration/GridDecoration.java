package com.qch.sumelauncher.recyclerview.decoration;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GridDecoration extends RecyclerView.ItemDecoration {
    private int spanCount;
    private int spacing;

    public GridDecoration(int spanCount, int spacing) {
        this.spanCount = spanCount;
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int column = position % spanCount;
        outRect.left = column == 0 ? spacing * 2 : spacing;
        outRect.right = column == spanCount - 1 ? spacing * 2 : spacing;
        outRect.top = position >= spanCount ? 2 * spacing : 0;
    }

    public void setSpanCount(int spanCount) {
        this.spanCount = spanCount;
    }

    public void setSpacing(int spacing) {
        this.spacing = spacing;
    }
}
