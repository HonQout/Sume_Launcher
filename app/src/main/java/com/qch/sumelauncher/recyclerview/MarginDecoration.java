package com.qch.sumelauncher.recyclerview;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MarginDecoration extends RecyclerView.ItemDecoration {
    private int marginLeft;
    private int marginRight;
    private int marginTop;
    private int marginBottom;

    public MarginDecoration(int marginLeft, int marginRight, int marginTop, int marginBottom) {
        this.marginLeft = marginLeft;
        this.marginRight = marginRight;
        this.marginTop = marginTop;
        this.marginBottom = marginBottom;
    }

    public MarginDecoration(int marginHorizontal, int marginVertical) {
        this.marginLeft = marginHorizontal;
        this.marginRight = marginHorizontal;
        this.marginTop = marginVertical;
        this.marginBottom = marginVertical;
    }

    public MarginDecoration(int marginAll) {
        this.marginLeft = marginAll;
        this.marginRight = marginAll;
        this.marginTop = marginAll;
        this.marginBottom = marginAll;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.left = marginLeft;
        outRect.right = marginRight;
        outRect.top = marginTop;
        outRect.bottom = marginBottom;
    }
}
