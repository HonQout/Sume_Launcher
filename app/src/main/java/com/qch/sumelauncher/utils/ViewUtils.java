package com.qch.sumelauncher.utils;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

public class ViewUtils {
    private static final String TAG = "ViewUtils";
    public static final String POPUP_DUMMY_ANCHOR = "POPUP_DUMMY_ANCHOR";

    /**
     * Get a dummy anchor view for popup menu to use.
     */
    @Nullable
    public static View getDummyAnchorView(View targetView) {
        if (targetView == null) {
            Log.e(TAG, "Cannot get dummy anchor view. Target view is null.");
            return null;
        }
        View anchorView = null;
        if (targetView.getParent() instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) targetView.getParent();
            anchorView = parent.findViewWithTag(POPUP_DUMMY_ANCHOR);
            if (anchorView == null) {
                anchorView = new View(targetView.getContext());
                anchorView.setTag(POPUP_DUMMY_ANCHOR);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(1, 1);
                anchorView.setLayoutParams(lp);
                parent.addView(anchorView);
            }
        }
        return anchorView;
    }
}
