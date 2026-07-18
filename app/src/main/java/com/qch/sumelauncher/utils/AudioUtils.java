package com.qch.sumelauncher.utils;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AudioUtils {
    private static final String TAG = "AudioUtils";

    public enum RingerMode {
        Silent, Vibrate, Normal
    }

    @Nullable
    public static AudioManager getAudioManager(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getSystemService(AudioManager.class);
        } else {
            return (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }
    }

    public static RingerMode getRingerMode(@NonNull Context context) {
        AudioManager audioManager = getAudioManager(context);
        if (audioManager == null) {
            Log.e(TAG, "Audio manager is null.");
            return RingerMode.Normal;
        }
        int ringerMode = audioManager.getRingerMode();
        switch (ringerMode) {
            case AudioManager.RINGER_MODE_SILENT: {
                return RingerMode.Silent;
            }
            case AudioManager.RINGER_MODE_VIBRATE: {
                return RingerMode.Vibrate;
            }
            case AudioManager.RINGER_MODE_NORMAL: {
                return RingerMode.Normal;
            }
            default: {
                Log.e(TAG, "System-provided ringer mode is illegal. Return the default.");
                return RingerMode.Normal;
            }
        }
    }
}
