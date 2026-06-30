package com.qch.sumelauncher.room.converter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qch.sumelauncher.bean.ActivityRecord;

import java.lang.reflect.Type;
import java.util.List;

public class Converters {
    private static Gson gson = new Gson();

    @TypeConverter
    public String activityRecordListToString(List<ActivityRecord> list) {
        return gson.toJson(list);
    }

    @TypeConverter
    public List<ActivityRecord> stringToActivityRecordList(String json) {
        Type type = new TypeToken<List<ActivityRecord>>() {
        }.getType();
        return gson.fromJson(json, type);
    }
}