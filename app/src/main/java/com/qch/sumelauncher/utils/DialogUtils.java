package com.qch.sumelauncher.utils;

import android.view.Window;

import androidx.appcompat.app.AlertDialog;

public class DialogUtils {
    public static void show(AlertDialog.Builder builder, boolean animation) {
        AlertDialog dialog = builder.create();
        Window dialogWindow = dialog.getWindow();
        if (dialogWindow != null && !animation) {
            dialogWindow.getAttributes().windowAnimations = 0;
        }
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }
}