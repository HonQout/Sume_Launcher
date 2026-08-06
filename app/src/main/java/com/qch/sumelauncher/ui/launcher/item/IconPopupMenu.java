package com.qch.sumelauncher.ui.launcher.item;

import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.os.Build;
import android.view.View;

import androidx.appcompat.widget.PopupMenu;

import com.qch.sumelauncher.R;

import java.util.List;

public class IconPopupMenu {
    public enum CalledFrom {
        LAUNCHER,
        APP_DRAWER
    }

    public PopupMenu getPopupMenu(Context context, View anchorView, CalledFrom source) {
        PopupMenu popupMenu = new PopupMenu(context, anchorView);
        int menuRes;
        switch (source) {
            case LAUNCHER: {
                menuRes = R.menu.launcher_blank_area_menu;
                break;
            }
            case APP_DRAWER: {
                menuRes = R.menu.app_op_menu;
                break;
            }
            default: {
                throw new IllegalArgumentException("Argument source is illegal.");
            }
        }
        popupMenu.getMenuInflater().inflate(menuRes, popupMenu.getMenu());
        // Add shortcuts
        int baseIndex = popupMenu.getMenu().size();
        List<ShortcutInfo> shortcutInfoList;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
//            shortcutInfoList=
        }
        return null;
    }
}
