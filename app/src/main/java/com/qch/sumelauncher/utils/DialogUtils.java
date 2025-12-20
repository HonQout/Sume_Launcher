package com.qch.sumelauncher.utils;

import android.view.Window;

import androidx.appcompat.app.AlertDialog;

public class DialogUtils {
    public static void show(AlertDialog.Builder builder, boolean animation) {
        if (animation) {
            builder.show();
        } else {
            removeAnimAndShow(builder);
        }
    }

    public static void removeAnimAndShow(AlertDialog.Builder builder) {
        AlertDialog dialog = builder.create();
        Window dialogWindow = dialog.getWindow();
        if (dialogWindow != null) {
            dialogWindow.getAttributes().windowAnimations = 0;
        }
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }
}