package com.qch.sumelauncher.bean;

import android.content.pm.PermissionInfo;

import java.util.ArrayList;
import java.util.List;

public class PermissionSorted {
    public List<PermissionInfo> permissionDefinedList = new ArrayList<>();
    public List<PermissionInfo> permissionRequestedList = new ArrayList<>();
    public List<String> permissionNotFoundList = new ArrayList<>();
}