package com.qch.sumelauncher.data.model.launcher;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qch.sumelauncher.room.entity.IconEntity;

public class IconModel extends ItemModel {
    private static final String TAG = "IconModel";
    private final ActivityModel activityModel;

    public IconModel(@NonNull Context context, IconEntity iconEntity) {
        super(
                iconEntity.getId(),
                new CellPosition(
                        iconEntity.getPageId(),
                        iconEntity.getCellX(),
                        iconEntity.getCellY(),
                        iconEntity.getSpanX(),
                        iconEntity.getSpanY()
                )
        );
        this.activityModel = new ActivityModel(context, iconEntity.getPackageName(),
                iconEntity.getActivityName());
    }

    @Override
    public Type getType() {
        return Type.ICON;
    }

    public ActivityModel getActivityModel() {
        return activityModel;
    }

    public String getPackageName() {
        return activityModel.getPackageName();
    }

    public String getActivityName() {
        return activityModel.getActivityName();
    }

    public String getKey() {
        return activityModel.getPackageName() + ":" + activityModel.getActivityName();
    }

    @Nullable
    public String getLabel() {
        return activityModel.getLabel();
    }

    public IconEntity toIconEntity() {
        return new IconEntity(
                cellPosition.getPageId(),
                cellPosition.getCellX(),
                cellPosition.getCellY(),
                cellPosition.getSpanX(),
                cellPosition.getSpanY(),
                activityModel.getPackageName(),
                activityModel.getActivityName()
        );
    }
}