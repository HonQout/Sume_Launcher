package com.qch.sumelauncher.ui.settings.root;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.qch.sumelauncher.application.MyApplication;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class SettingsViewModel extends AndroidViewModel {
    private static final String TAG = "SettingsViewModel";
    // data
    private final MutableLiveData<Boolean> mDisplayStatusBar = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mDisplayTopBar = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mForceToApplyBgColor = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mAnimation = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mSwitchPageViaScrolling = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mSwitchPageViaVolumeKeys = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mAskForPermFineLocation = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mDisplayDate = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mDisplayRingerMode = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mDisplayAirplaneMode = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mDisplayWifi = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mDisplayBluetooth = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mDisplayBatteryPct = new MutableLiveData<>();

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
        compositeDisposable.add(MyApplication.getPreferenceDataStoreImpl()
                .getBooleanFlowable("display_status_bar", true)
                .subscribe(
                        mDisplayStatusBar::postValue,
                        throwable -> Log.e(TAG, "Cannot get value of key display_status_bar.", throwable)
                )
        );
        // display_top_bar
        compositeDisposable.add(MyApplication.getPreferenceDataStoreImpl()
                .getBooleanFlowable("display_top_bar", true)
                .subscribe(
                        mDisplayTopBar::postValue,
                        throwable -> Log.e(TAG, "Cannot get value of key display_top_bar.", throwable)
                )
        );
        // force_to_apply_bg_color
        compositeDisposable.add(MyApplication.getPreferenceDataStoreImpl()
                .getBooleanFlowable("force_to_apply_bg_color", false)
                .subscribe(
                        mForceToApplyBgColor::postValue,
                        throwable -> Log.e(TAG, "Cannot get value of key force_to_apply_bg_color.", throwable)
                )
        );
        // animation
        compositeDisposable.add(MyApplication.getPreferenceDataStoreImpl()
                .getBooleanFlowable("animation", true)
                .subscribe(
                        mAnimation::postValue,
                        throwable -> Log.e(TAG, "Cannot get value of key animation.", throwable)
                )
        );
        // switch_page_via_scrolling
        compositeDisposable.add(MyApplication.getPreferenceDataStoreImpl()
                .getBooleanFlowable("switch_page_via_scrolling", true)
                .subscribe(
                        mSwitchPageViaScrolling::postValue,
                        throwable -> Log.e(TAG, "Cannot get value of key switch_page_via_scrolling.", throwable)
                )
        );
        // switch_page_via_volume_keys
        compositeDisposable.add(MyApplication.getPreferenceDataStoreImpl()
                .getBooleanFlowable("switch_page_via_volume_keys", true)
                .subscribe(
                        mSwitchPageViaVolumeKeys::postValue,
                        throwable -> Log.e(TAG, "Cannot get value of key switch_page_via_volume_keys.", throwable)
                )
        );
        // ask_for_perm_fine_location
        compositeDisposable.add(MyApplication.getPreferenceDataStoreImpl()
                .getBooleanFlowable("ask_for_perm_fine_location", true)
                .subscribe(
                        mAskForPermFineLocation::postValue,
                        throwable -> Log.e(TAG, "Cannot get value of key ask_for_perm_fine_location.", throwable)
                )
        );
        // Top bar icon - Date
        compositeDisposable.add(MyApplication.getPreferenceDataStoreImpl()
                .getBooleanFlowable("date", true)
                .subscribe(
                        mDisplayDate::postValue,
                        throwable -> Log.e(TAG, "Cannot get value of key date.", throwable)
                )
        );
        // Top bar icon - Ringer mode
        compositeDisposable.add(MyApplication.getPreferenceDataStoreImpl()
                .getBooleanFlowable("ringer_mode", true)
                .subscribe(
                        mDisplayRingerMode::postValue,
                        throwable -> Log.e(TAG, "Cannot get value of key ringer_mode.", throwable)
                )
        );
        // Top bar icon - Airplane mode
        compositeDisposable.add(MyApplication.getPreferenceDataStoreImpl()
                .getBooleanFlowable("airplane_mode", true)
                .subscribe(
                        mDisplayAirplaneMode::postValue,
                        throwable -> Log.e(TAG, "Cannot get value of key airplane_mode.", throwable)
                )
        );
        // Top bar icon - Wi-Fi
        compositeDisposable.add(MyApplication.getPreferenceDataStoreImpl()
                .getBooleanFlowable("wifi", true)
                .subscribe(
                        mDisplayWifi::postValue,
                        throwable -> Log.e(TAG, "Cannot get value of key wifi.", throwable)
                )
        );
        // Top bar icon - Bluetooth
        compositeDisposable.add(MyApplication.getPreferenceDataStoreImpl()
                .getBooleanFlowable("bluetooth", true)
                .subscribe(
                        mDisplayBluetooth::postValue,
                        throwable -> Log.e(TAG, "Cannot get value of key bluetooth.", throwable)
                )
        );
        // Top bar icon - Battery percentage
        compositeDisposable.add(MyApplication.getPreferenceDataStoreImpl()
                .getBooleanFlowable("battery_percentage", true)
                .subscribe(
                        mDisplayBatteryPct::postValue,
                        throwable -> Log.e(TAG, "Cannot get value of key battery_percentage.", throwable)
                )
        );
    }

    public LiveData<Boolean> getDisplayStatusBar() {
        return mDisplayStatusBar;
    }

    public LiveData<Boolean> getDisplayTopBar() {
        return mDisplayTopBar;
    }

    public LiveData<Boolean> getForceToApplyBgColor() {
        return mForceToApplyBgColor;
    }

    public boolean getAnimationValue() {
        return mAnimation.getValue() == null || mAnimation.getValue();
    }

    public LiveData<Boolean> getSwitchPageViaScrolling() {
        return mSwitchPageViaScrolling;
    }

    public boolean getSwitchPageViaVolumeKeysValue() {
        return mSwitchPageViaVolumeKeys.getValue() == null || mSwitchPageViaVolumeKeys.getValue();
    }

    public boolean getAskForPermFineLocationValue() {
        return mAskForPermFineLocation.getValue() == null || mAskForPermFineLocation.getValue();
    }

    public LiveData<Boolean> getDisplayDate() {
        return mDisplayDate;
    }

    public LiveData<Boolean> getDisplayRingerMode() {
        return mDisplayRingerMode;
    }

    public LiveData<Boolean> getDisplayAirplaneMode() {
        return mDisplayAirplaneMode;
    }

    public LiveData<Boolean> getDisplayWifi() {
        return mDisplayWifi;
    }

    public LiveData<Boolean> getDisplayBluetooth() {
        return mDisplayBluetooth;
    }

    public LiveData<Boolean> getDisplayBatteryPct() {
        return mDisplayBatteryPct;
    }
}