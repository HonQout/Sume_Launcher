package com.qch.sumelauncher.topbar.ui;

import android.content.Context;
import android.util.Log;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.color.MaterialColors;
import com.qch.sumelauncher.R;

public class TopBarManager {
    public enum TopBarIcons {
        RINGER_MODE, AIRPLANE_MODE, WIFI, BLUETOOTH
    }

    public static AppCompatImageView buildIcon(
            @NonNull Context context,
            TopBarIcons iconTag,
            @DrawableRes int res) {
        AppCompatImageView imageView = new AppCompatImageView(context);
        imageView.setTag(iconTag);
        imageView.setImageResource(res);
        imageView.setColorFilter(
                MaterialColors.getColor(imageView, com.google.android.material.R.attr.colorOnSurface)
        );
        imageView.setPadding(0, 0, 0, 0);
        LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(
                context.getResources().getDimensionPixelSize(R.dimen.top_bar_icon_size),
                context.getResources().getDimensionPixelSize(R.dimen.top_bar_icon_size)
        );
        layoutParams.setMargins(2, 0, 2, 0);
        imageView.setLayoutParams(layoutParams);
        return imageView;
    }

    public static boolean addIcon(@NonNull Context context,
                                  LinearLayoutCompat rightPart,
                                  TopBarIcons iconTag,
                                  @DrawableRes int res,
                                  String logTag) {
        View view = rightPart.findViewWithTag(iconTag);
        if (view != null) {
            Log.e(logTag, "Icon " + iconTag.name() + " already exists.");
            return false;
        }
        AppCompatImageView imageView = buildIcon(context, iconTag, res);
        rightPart.addView(imageView, 0);
        Log.d(logTag, "Added icon " + iconTag.name() + ".");
        return true;
    }

    public static boolean hasView(LinearLayoutCompat rightPart, TopBarIcons iconTag) {
        View view = rightPart.findViewWithTag(iconTag);
        return view != null;
    }

    public static boolean hasIcon(LinearLayoutCompat rightPart, TopBarIcons iconTag) {
        View view = rightPart.findViewWithTag(iconTag);
        return view instanceof AppCompatImageView;
    }

    public static boolean modifyIcon(LinearLayoutCompat rightPart,
                                     TopBarIcons iconTag,
                                     @DrawableRes int res,
                                     String logTag) {
        View view = rightPart.findViewWithTag(iconTag);
        if (!(view instanceof AppCompatImageView)) {
            Log.e(logTag, "Cannot find icon " + iconTag.name() + ".");
            return false;
        }
        AppCompatImageView imageView = (AppCompatImageView) view;
        imageView.setImageResource(res);
        return true;
    }

    public static void replaceIcon(@NonNull Context context,
                                   LinearLayoutCompat rightPart,
                                   TopBarIcons iconTag,
                                   @DrawableRes int res,
                                   String logTag) {
        View view = rightPart.findViewWithTag(iconTag);
        if (view != null) {
            rightPart.removeView(view);
        }
        AppCompatImageView imageView = buildIcon(context, iconTag, res);
        rightPart.addView(imageView, 0);
        Log.d(logTag, "Replaced icon " + iconTag.name() + ".");
    }

    public static boolean removeIcon(LinearLayoutCompat rightPart,
                                     TopBarIcons iconTag,
                                     String logTag) {
        View view = rightPart.findViewWithTag(iconTag);
        if (view == null) {
            Log.e(logTag, "Icon " + iconTag.name() + " doesn't exist.");
            return false;
        }
        rightPart.removeView(view);
        Log.d(logTag, "Removed icon " + iconTag.name() + ".");
        return true;
    }
}