package com.qch.sumelauncher.topbar.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.MaterialColors;
import com.qch.sumelauncher.R;
import com.qch.sumelauncher.databinding.TopBarBinding;

public class TopBarView extends LinearLayoutCompat {
    private static final String TAG = "TopBarView";
    private TopBarBinding binding;

    public enum ViewTag {
        RINGER_MODE,
        AIRPLANE_MODE,
        WIFI,
        BLUETOOTH,
        BATTERY_PCT,
        BATTERY
    }

    public enum ConflictStrategy {
        KEEP_EXISTING,
        REPLACE_EXISTING,
        IGNORE_EXISTING
    }

    public static class IconExtra {
        @DrawableRes
        int res;

        public IconExtra(@DrawableRes int res) {
            this.res = res;
        }
    }

    public static class BatteryPctExtra {
        int level;

        public BatteryPctExtra(int level) {
            this.level = level;
        }
    }

    public static class BatteryExtra {
        int level;
        boolean isCharging;

        public BatteryExtra(int level, boolean isCharging) {
            this.level = level;
            this.isCharging = isCharging;
        }
    }

    public TopBarView(@NonNull Context context) {
        this(context, null, 0);
    }

    public TopBarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopBarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        binding = TopBarBinding.inflate(LayoutInflater.from(context), this, true);
        addChildView(context, ViewTag.BATTERY_PCT, new BatteryPctExtra(100), ConflictStrategy.KEEP_EXISTING);
        addChildView(context, ViewTag.BATTERY, new BatteryExtra(100, false), ConflictStrategy.KEEP_EXISTING);
    }

    private AppCompatImageView buildIcon(@NonNull Context context,
                                         ViewTag iconTag,
                                         @DrawableRes int res) {
        AppCompatImageView imageView = new AppCompatImageView(context);
        imageView.setTag(iconTag);
        imageView.setImageResource(res);
        imageView.setColorFilter(MaterialColors.getColor(
                imageView, com.google.android.material.R.attr.colorOnSurface)
        );
        imageView.setPadding(0, 0, 0, 0);
        LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(
                context.getResources().getDimensionPixelSize(R.dimen.top_bar_icon_size),
                context.getResources().getDimensionPixelSize(R.dimen.top_bar_icon_size)
        );
        layoutParams.setMargins(1, 1, 1, 1);
        imageView.setLayoutParams(layoutParams);
        return imageView;
    }

    public void setTimeText(String timeText) {
        AppCompatTextView textView = binding.topBarLeftPart.findViewById(R.id.top_bar_tv_time);
        if (textView == null) {
            Log.e(TAG, "Cannot find TextView of time.");
            return;
        }
        textView.setText(timeText);
    }

    public void setDateText(String dateText) {
        AppCompatTextView textView = binding.topBarLeftPart.findViewById(R.id.top_bar_tv_date);
        if (textView == null) {
            Log.e(TAG, "Cannot find TextView of date.");
            return;
        }
        textView.setText(dateText);
    }

    /**
     * Add a child view to the area of status views.
     *
     * @return {@code True} if and only if the view is successfully added, {@code False} otherwise.
     */
    public boolean addChildView(@NonNull Context context,
                                ViewTag viewTag,
                                Object extra,
                                ConflictStrategy strategy) {
        LinearLayoutCompat container = (LinearLayoutCompat) binding.topBarRightPart;
        View view = container.findViewWithTag(viewTag);
        if (view != null) {
            Log.e(TAG, "View " + viewTag.name() + " already exists.");
            switch (strategy) {
                case KEEP_EXISTING: {
                    return false;
                }
                case REPLACE_EXISTING: {
                    container.removeView(view);
                }
            }
        }
        switch (viewTag) {
            case RINGER_MODE:
            case AIRPLANE_MODE:
            case WIFI:
            case BLUETOOTH: {
                if (!(extra instanceof IconExtra)) {
                    Log.e(TAG, "Failed to add view " + viewTag.name() + ". Illegal argument: extra.");
                    return false;
                }
                IconExtra iconExtra = (IconExtra) extra;
                int res = iconExtra.res;

                AppCompatImageView imageView = buildIcon(context, viewTag, res);
                LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(
                        context.getResources().getDimensionPixelSize(R.dimen.top_bar_icon_size),
                        context.getResources().getDimensionPixelSize(R.dimen.top_bar_icon_size)
                );
                layoutParams.setMargins(2, 2, 2, 2);
                imageView.setLayoutParams(layoutParams);

                addChildViewInner(container, imageView, viewTag);
                return true;
            }
            case BATTERY_PCT: {
                if (!(extra instanceof BatteryPctExtra)) {
                    Log.e(TAG, "Failed to add view " + viewTag.name() + ". Illegal argument: extra.");
                    return false;
                }
                BatteryPctExtra batteryPctExtra = (BatteryPctExtra) extra;
                int level = batteryPctExtra.level;

                TextView textView = new TextView(context);
                // Set width and height
                LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                layoutParams.setMargins(2, 2, 2, 2);
                textView.setLayoutParams(layoutParams);
                // Set text size
                float textSizePx = getResources().getDimensionPixelSize(R.dimen.top_bar_text_size);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx);
                // Set text style
                textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                // Set content
                textView.setText(String.format(ContextCompat.getString(context, R.string.battery_percentage_format), level));
                // Set tag
                textView.setTag(ViewTag.BATTERY_PCT);

                // Find index to insert
                addChildViewInner(container, textView, viewTag);
                return true;
            }
            case BATTERY: {
                if (!(extra instanceof BatteryExtra)) {
                    Log.e(TAG, "Failed to add view " + viewTag.name() + ". Illegal argument: extra.");
                    return false;
                }
                BatteryExtra batteryExtra = (BatteryExtra) extra;
                int level = batteryExtra.level;
                boolean isCharging = batteryExtra.isCharging;

                BatteryView batteryView = new BatteryView(context);
                LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(
                        context.getResources().getDimensionPixelSize(R.dimen.top_bar_battery_width),
                        context.getResources().getDimensionPixelSize(R.dimen.top_bar_battery_height)
                );
                layoutParams.setMargins(2, 2, 2, 2);
                batteryView.setLayoutParams(layoutParams);
                batteryView.setLevel(level);
                batteryView.setCharging(isCharging);
                batteryView.setTag(ViewTag.BATTERY);

                addChildViewInner(container, batteryView, viewTag);
                return true;
            }
        }
        return false;
    }

    private void addChildViewInner(ViewGroup container, View child, ViewTag viewTag) {
        int insertIndex = -1;
        for (int i = 0; i < container.getChildCount(); i++) {
            View existingChild = container.getChildAt(i);
            Object existingTag = existingChild.getTag();

            if (existingTag instanceof ViewTag) {
                int existingIndex = ((ViewTag) existingTag).ordinal();
                if (existingIndex > viewTag.ordinal()) {
                    insertIndex = i;
                    break;
                }
            }
        }
        container.addView(child, insertIndex);
        Log.d(TAG, "Added view " + viewTag.name() + " at " + insertIndex + ".");
    }

    public boolean modifyChildView(ViewTag viewTag, Object extra) {
        switch (viewTag) {
            case RINGER_MODE:
            case AIRPLANE_MODE:
            case WIFI:
            case BLUETOOTH: {
                if (!(extra instanceof IconExtra)) {
                    Log.e(TAG, "Failed to add view " + viewTag.name() + ". Illegal argument: extra.");
                    return false;
                }
                IconExtra iconExtra = (IconExtra) extra;
                int res = iconExtra.res;

                View view = binding.topBarRightPart.findViewWithTag(viewTag);
                if (!(view instanceof AppCompatImageView)) {
                    Log.e(TAG, "Cannot find view " + viewTag.name() + ".");
                    return false;
                }
                AppCompatImageView imageView = (AppCompatImageView) view;
                imageView.setImageResource(res);
                return true;
            }
            case BATTERY_PCT:
            case BATTERY: {
                return false;
            }
        }
        return false;
    }

    public boolean modifyOrAddChildView(Context context, ViewTag viewTag, Object extra) {
        switch (viewTag) {
            case RINGER_MODE:
            case AIRPLANE_MODE:
            case WIFI:
            case BLUETOOTH: {
                if (!(extra instanceof IconExtra)) {
                    Log.e(TAG, "Failed to add view " + viewTag.name() + ". Illegal argument: extra.");
                    return false;
                }
                IconExtra iconExtra = (IconExtra) extra;
                int res = iconExtra.res;

                View view = binding.topBarRightPart.findViewWithTag(viewTag);
                if (view instanceof AppCompatImageView) {
                    AppCompatImageView imageView = (AppCompatImageView) view;
                    imageView.setImageResource(res);
                    return true;
                } else {
                    return addChildView(context, viewTag, extra, ConflictStrategy.REPLACE_EXISTING);
                }
            }
            case BATTERY_PCT:
            case BATTERY: {
                return false;
            }
        }
        return false;
    }

    public boolean removeChildView(ViewTag viewTag) {
        View view = binding.topBarRightPart.findViewWithTag(viewTag);
        if (view == null) {
            Log.e(TAG, "View " + viewTag.name() + " doesn't exist.");
            return false;
        }
        binding.topBarRightPart.removeView(view);
        Log.d(TAG, "Removed view " + viewTag.name() + ".");
        return true;
    }

    public void setBatteryLevel(@NonNull Context context, int level) {
        LinearLayoutCompat container = binding.topBarRightPart;
        View view1 = container.findViewWithTag(ViewTag.BATTERY_PCT);
        if (view1 instanceof TextView) {
            TextView textView = (TextView) view1;
            textView.setText(
                    String.format(
                            ContextCompat.getString(context, R.string.battery_percentage_format),
                            level
                    ));
        }

        View view2 = binding.topBarRightPart.findViewWithTag(ViewTag.BATTERY);
        if (view2 instanceof BatteryView) {
            BatteryView batteryView = (BatteryView) view2;
            batteryView.setLevel(level);
        }
    }

    public void setBatteryCharging(boolean isCharging) {
        View view = binding.topBarRightPart.findViewWithTag(ViewTag.BATTERY);
        if (view instanceof BatteryView) {
            BatteryView batteryView = (BatteryView) view;
            batteryView.setCharging(isCharging);
        }
    }
}
