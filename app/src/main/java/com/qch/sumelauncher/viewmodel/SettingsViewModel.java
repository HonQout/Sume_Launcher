package com.qch.sumelauncher.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.qch.sumelauncher.MyApplication;
import com.qch.sumelauncher.R;
import com.qch.sumelauncher.activity.PermissionActivity;
import com.qch.sumelauncher.utils.IntentUtils;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class SettingsViewModel extends AndroidViewModel {
    private static final String TAG = "SettingsViewModel";
    // data
    private final MutableLiveData<Boolean> mDisplayStatusBar = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mAnimation = new MutableLiveData<>();
    // persistence
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        initDisposable();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }

    private void initDisposable() {
        // display_status_bar
        Disposable disposable1 = MyApplication.getPreferenceDataStore()
                .getBooleanFlowable("display_status_bar", true)
                .subscribe(
                        mDisplayStatusBar::postValue,
                        error -> Log.e(TAG, "Failed to get value of key display_status_bar")
                );
        compositeDisposable.add(disposable1);
        // animation
        Disposable disposable2 = MyApplication.getPreferenceDataStore()
                .getBooleanFlowable("animation", true)
                .subscribe(
                        mAnimation::postValue,
                        throwable -> Log.e(TAG, "Failed to get value of key animation", throwable)
                );
        compositeDisposable.add(disposable2);
    }

    public LiveData<Boolean> getDisplayStatusBar() {
        return mDisplayStatusBar;
    }

    public boolean getAnimationValue() {
        return mAnimation.getValue() == null || mAnimation.getValue();
    }

    public void startManageDefaultAppsSettings(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (!getAnimationValue()) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            }
            activity.startActivity(intent);
        } else {
            Toast.makeText(activity, R.string.cannot_find_activity, Toast.LENGTH_SHORT).show();
        }
    }

    public void startPermissionActivity(@NonNull Activity activity) {
        Intent intent = new Intent(activity, PermissionActivity.class);
        if (!getAnimationValue()) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        }
        activity.startActivity(intent);
    }

    public void openGithubPage(@NonNull Activity activity) {
        int flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        if (getAnimationValue()) {
            flags |= Intent.FLAG_ACTIVITY_NO_ANIMATION;
        }
        IntentUtils.handleLaunchIntentResult(
                activity,
                IntentUtils.openNetAddress(
                        activity,
                        ContextCompat.getString(activity, R.string.github_address),
                        flags)
        );
    }
}