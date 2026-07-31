package com.qch.sumelauncher.recyclerview.decoration;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class VerticalGridDecoration extends RecyclerView.ItemDecoration {
    private int spanCount;
    private int spaceHorizontal;
    private int spaceVertical;

    public VerticalGridDecoration(int spanCount, int spaceHorizontal, int spaceVertical) {
        this.spanCount = spanCount;
        this.spaceHorizontal = spaceHorizontal;
        this.spaceVertical = spaceVertical;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int column = position % spanCount;
        int row = position - column * spanCount;
        outRect.left = column == 0 ? 0 : spaceHorizontal / 2;
        outRect.right = column == spanCount - 1 ? 0 : spaceHorizontal / 2;
        outRect.top = row == 0 ? 0 : spaceVertical;
    }

    public void setSpanCount(int spanCount) {
        this.spanCount = spanCount;
    }

    public int getSpanCount() {
        return spanCount;
    }

    public void setSpaceHorizontal(int spaceHorizontal) {
        this.spaceHorizontal = spaceHorizontal;
    }

    public int getSpaceHorizontal() {
        return spaceHorizontal;
    }

    public void setSpaceVertical(int spaceVertical) {
        this.spaceVertical = spaceVertical;
    }

    public int getSpaceVertical() {
        return spaceVertical;
    }
}