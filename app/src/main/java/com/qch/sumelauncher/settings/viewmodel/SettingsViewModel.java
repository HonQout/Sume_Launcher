package com.qch.sumelauncher.settings.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.qch.sumelauncher.application.MyApplication;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class SettingsViewModel extends AndroidViewModel {
    private static final String TAG = "SettingsViewModel";
    // data
    private final MutableLiveData<Boolean> mDisplayStatusBar = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mDisplayTopBar = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mAnimation = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mScrollToSwitchPage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mVolumeKeySwitchPage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mAskForPermFineLocation = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mDisplayRingerMode = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mDisplayAirplaneMode = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mDisplayWlan = new MutableLiveData<>();
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
        Disposable disposable1 = MyApplication.getPreferenceDataStore()
                .getBooleanFlowable("display_status_bar", true)
                .subscribe(
                        mDisplayStatusBar::postValue,
                        throwable -> Log.e(TAG, "Cannot get value of key display_status_bar.", throwable)
                );
        compositeDisposable.add(disposable1);
        // display_top_bar
        Disposable disposable2 = MyApplication.getPreferenceDataStore()
                .getBooleanFlowable("display_top_bar", true)
                .subscribe(
                        mDisplayTopBar::postValue,
                        throwable -> Log.e(TAG, "Cannot get value of key display_top_bar.", throwable)
                );
        compositeDisposable.add(disposable2);
        // animation
        Disposable disposable3 = MyApplication.getPreferenceDataStore()
                .getBooleanFlowable("animation", true)
                .subscribe(
                        mAnimation::postValue,
                        throwable -> Log.e(TAG, "Cannot get value of key animation.", throwable)
                );
        compositeDisposable.add(disposable3);
        // scroll_switch_page
        Disposable disposable4 = MyApplication.getPreferenceDataStore()
                .getBooleanFlowable("scroll_switch_page", true)
                .subscribe(
                        mScrollToSwitchPage::postValue,
                        throwable -> Log.e(TAG, "Cannot get value of key scroll_switch_page.", throwable)
                );
        compositeDisposable.add(disposable4);
        // volume_key_switch_page
        Disposable disposable5 = MyApplication.getPreferenceDataStore()
                .getBooleanFlowable("volume_key_switch_page", true)
                .subscribe(
                        mVolumeKeySwitchPage::postValue,
                        throwable -> Log.e(TAG, "Cannot get value of key volume_key_switch_page.", throwable)
                );
        compositeDisposable.add(disposable5);
        // ask_for_perm_fine_location
        Disposable disposable6 = MyApplication.getPreferenceDataStore()
                .getBooleanFlowable("ask_for_perm_fine_location", true)
                .subscribe(
                        mAskForPermFineLocation::postValue,
                        throwable -> Log.e(TAG, "Cannot get value of key ask_for_perm_fine_location.", throwable)
                );
        compositeDisposable.add(disposable6);
        Disposable disposable7 = MyApplication.getPreferenceDataStore()
                .getBooleanFlowable("ringer_mode", true)
                .subscribe(
                        mDisplayRingerMode::postValue,
                        throwable -> Log.e(TAG, "Cannot get value of key ringer_mode.", throwable)
                );
        compositeDisposable.add(disposable7);
        Disposable disposable8 = MyApplication.getPreferenceDataStore()
                .getBooleanFlowable("airplane_mode", true)
                .subscribe(
                        mDisplayAirplaneMode::postValue,
                        throwable -> Log.e(TAG, "Cannot get value of key airplane_mode.", throwable)
                );
        compositeDisposable.add(disposable8);
        Disposable disposable9 = MyApplication.getPreferenceDataStore()
                .getBooleanFlowable("wlan", true)
                .subscribe(
                        mDisplayWlan::postValue,
                        throwable -> Log.e(TAG, "Cannot get value of key wlan.", throwable)
                );
        compositeDisposable.add(disposable9);
        Disposable disposable10 = MyApplication.getPreferenceDataStore()
                .getBooleanFlowable("bluetooth", true)
                .subscribe(
                        mDisplayBluetooth::postValue,
                        throwable -> Log.e(TAG, "Cannot get value of key bluetooth.", throwable)
                );
        compositeDisposable.add(disposable10);
        Disposable disposable11 = MyApplication.getPreferenceDataStore()
                .getBooleanFlowable("battery_percentage", true)
                .subscribe(
                        mDisplayBatteryPct::postValue,
                        throwable -> Log.e(TAG, "Cannot get value of key battery_percentage.", throwable)
                );
        compositeDisposable.add(disposable11);
    }

    public LiveData<Boolean> getDisplayStatusBar() {
        return mDisplayStatusBar;
    }

    public LiveData<Boolean> getDisplayTopBar() {
        return mDisplayTopBar;
    }

    public boolean getAnimationValue() {
        return mAnimation.getValue() == null || mAnimation.getValue();
    }

    public LiveData<Boolean> getScrollToSwitchPage() {
        return mScrollToSwitchPage;
    }

    public boolean getVolumeKeySwitchPageValue() {
        return mVolumeKeySwitchPage.getValue() == null || mVolumeKeySwitchPage.getValue();
    }

    public boolean getAskForPermFineLocationValue() {
        return mAskForPermFineLocation.getValue() == null || mAskForPermFineLocation.getValue();
    }

    public LiveData<Boolean> getDisplayRingerMode() {
        return mDisplayRingerMode;
    }

    public boolean getDisplayRingerModeValue() {
        return mDisplayRingerMode.getValue() == null || mDisplayRingerMode.getValue();
    }

    public LiveData<Boolean> getDisplayAirplaneMode() {
        return mDisplayAirplaneMode;
    }

    public boolean getDisplayAirplaneModeValue() {
        return mDisplayAirplaneMode.getValue() != null && mDisplayAirplaneMode.getValue();
    }

    public LiveData<Boolean> getDisplayWlan() {
        return mDisplayWlan;
    }

    public boolean getDisplayWlanValue() {
        return mDisplayWlan.getValue() != null && mDisplayWlan.getValue();
    }

    public LiveData<Boolean> getDisplayBluetooth() {
        return mDisplayBluetooth;
    }

    public boolean getDisplayBluetoothValue() {
        return mDisplayBluetooth.getValue() != null && mDisplayBluetooth.getValue();
    }

    public LiveData<Boolean> getDisplayBatteryPct() {
        return mDisplayBatteryPct;
    }

    public boolean getDisplayBatteryPctValue() {
        return mDisplayBatteryPct.getValue() != null && mDisplayBatteryPct.getValue();
    }
}