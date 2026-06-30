package com.qch.sumelauncher.recyclerview.decoration;

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

    public void setMarginLeft(int marginLeft) {
        this.marginLeft = marginLeft;
    }

    public void setMarginRight(int marginRight) {
        this.marginRight = marginRight;
    }

    public void setMarginTop(int marginTop) {
        this.marginTop = marginTop;
    }

    public void setMarginBottom(int marginBottom) {
        this.marginBottom = marginBottom;
    }

    public void setMarginHorizontal(int marginHorizontal) {
        this.marginLeft = marginHorizontal;
        this.marginRight = marginHorizontal;
    }

    public void setMarginVertical(int marginVertical) {
        this.marginTop = marginVertical;
        this.marginBottom = marginVertical;
    }

    public void setMarginAll(int marginAll) {
        this.marginLeft = marginAll;
        this.marginRight = marginAll;
        this.marginTop = marginAll;
        this.marginBottom = marginAll;
    }
}
