package com.qch.sumelauncher.viewmodel;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.qch.sumelauncher.application.MyApplication;
import com.qch.sumelauncher.bean.ActivityBean;
import com.qch.sumelauncher.room.entity.IconEntity;
import com.qch.sumelauncher.room.repository.LauncherIconRepository;
import com.qch.sumelauncher.utils.ApplicationUtils;
import com.qch.sumelauncher.utils.CollectionUtils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class LauncherViewModel extends AndroidViewModel {
    private static final String TAG = "LauncherViewModel";

    // constant
    private static final int defNumRow = 5;
    private static final int defNumColumn = 5;

    public enum LauncherState {
        NORMAL, SETTINGS, SEARCH
    }

    public enum AppListOp {
        INIT, ADD, REMOVE, REPLACE
    }

    // data
    private LiveData<Integer> numScreen;
    private LiveData<Map<Integer, List<IconEntity>>> launcherIconMap;
    private final MutableLiveData<LauncherState> mLauncherState = new MutableLiveData<>(LauncherState.NORMAL);
    private final MutableLiveData<Boolean> mDisplayStatusBar = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mDisplayTopBar = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mAnimation = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mScrollToSwitchPage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mVolumeKeySwitchPage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mAskForPermFineLocation = new MutableLiveData<>();
    private final MutableLiveData<String> mGridSize = new MutableLiveData<>("5,5");
    private final MutableLiveData<Integer> mNumRow = new MutableLiveData<>();
    private final MutableLiveData<Integer> mNumColumn = new MutableLiveData<>();
    private final MutableLiveData<Integer> mCurrentScreen = new MutableLiveData<>();
    private final MutableLiveData<List<ActivityBean>> mActivityBeanList = new MutableLiveData<>();

    // multi-thread
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final AtomicBoolean isUpdatingList = new AtomicBoolean(false);
    private final Object updateListLock = new Object();
    private final AtomicBoolean isUpdatingLauncherIcon = new AtomicBoolean(false);
    private final Object updateLauncherIconLock = new Object();
    private final AtomicBoolean isEditingMap = new AtomicBoolean(false);
    private final Object editMapLock = new Object();

    // persistence
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private LauncherIconRepository repository;

    // broadcast receiver
    private BroadcastReceiver localeBroadcastReceiver = null;
    private BroadcastReceiver packageBroadcastReceiver = null;


    public LauncherViewModel(@NonNull Application application) {
        super(application);
        registerLocaleBR();
        registerPackageBR();
        initLauncherLayout();
        initDisposable();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        unregisterLocaleBR();
        unregisterPackageBR();
        executorService.shutdown();
        compositeDisposable.clear();
    }


    private void registerLocaleBR() {
        if (localeBroadcastReceiver != null) {
            return;
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_LOCALE_CHANGED);

        localeBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateActivityBeanList(AppListOp.INIT, null);
            }
        };

        ContextCompat.registerReceiver(getApplication(), localeBroadcastReceiver, intentFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void unregisterLocaleBR() {
        if (localeBroadcastReceiver != null) {
            getApplication().unregisterReceiver(localeBroadcastReceiver);
            localeBroadcastReceiver = null;
        }
    }

    private void registerPackageBR() {
        if (packageBroadcastReceiver != null) {
            return;
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");

        packageBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    Log.e(TAG, "Received intent is null.");
                    return;
                }
                String action = intent.getAction();
                if (action == null) {
                    Log.e(TAG, "Action of intent is null.");
                    return;
                }
                Log.i(TAG, "Received intent action: " + action);
                switch (action) {
                    case Intent.ACTION_PACKAGE_ADDED: {
                        boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
                        Log.i(TAG, "Extra: replacing = " + replacing);
                        if (!replacing) {
                            String packageName;
                            Uri data = intent.getData();
                            if (data != null) {
                                packageName = data.getSchemeSpecificPart();
                                if (packageName != null) {
                                    updateActivityBeanList(AppListOp.ADD, packageName);
                                }
                            }
                        }
                        break;
                    }

                    case Intent.ACTION_PACKAGE_REMOVED: {
                        boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
                        boolean dataRemoved = intent.getBooleanExtra(Intent.EXTRA_DATA_REMOVED, false);
                        Log.i(TAG, "Extra: replacing = " + replacing + ", data_removed = " + dataRemoved);
                        if (!replacing) {
                            String packageName;
                            Uri data = intent.getData();
                            if (data != null) {
                                packageName = data.getSchemeSpecificPart();
                                if (packageName != null) {
                                    updateActivityBeanList(AppListOp.REMOVE, packageName);
                                }
                            }
                        }
                        break;
                    }

                    case Intent.ACTION_PACKAGE_REPLACED: {
                        String packageName;
                        Uri data = intent.getData();
                        if (data != null) {
                            packageName = data.getSchemeSpecificPart();
                            if (packageName != null) {
                                updateActivityBeanList(AppListOp.REPLACE, packageName);
                            }
                        }
                        break;
                    }
                }
            }
        };

        ContextCompat.registerReceiver(getApplication(), packageBroadcastReceiver, intentFilter,
                ContextCompat.RECEIVER_EXPORTED);
    }

    private void unregisterPackageBR() {
        if (packageBroadcastReceiver != null) {
            getApplication().unregisterReceiver(packageBroadcastReceiver);
            packageBroadcastReceiver = null;
        }
    }

    private void initLauncherLayout() {
        repository = new LauncherIconRepository(getApplication());
        launcherIconMap = Transformations.switchMap(mGridSize, layoutName ->
                repository.getIconMapInLayout(layoutName));
        numScreen = Transformations.switchMap(mGridSize, layoutName ->
                repository.getNumScreens(layoutName));
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
        // grid_count
        Disposable disposable7 = MyApplication.getPreferenceDataStore()
                .getStringFlowable("grid_count", "5,5")
                .subscribe(gridSize -> {
                            mGridSize.postValue(gridSize);
                            String[] split = gridSize.split(",");
                            int actualNumColumn = defNumColumn;
                            int actualNumRow = defNumRow;
                            try {
                                actualNumColumn = Integer.parseInt(split[0]);
                            } catch (Exception e) {
                                Log.e(TAG, "Cannot get stored numColumn.", e);
                            }
                            try {
                                actualNumRow = Integer.parseInt(split[1]);
                            } catch (Exception e) {
                                Log.e(TAG, "Cannot get stored numRow.", e);
                            }
                            mNumColumn.postValue(actualNumColumn);
                            mNumRow.postValue(actualNumRow);
                            updateActivityBeanList(AppListOp.INIT, null);
                        },
                        throwable -> Log.e(TAG, "Cannot get value of key grid_count.", throwable)
                );
        compositeDisposable.add(disposable7);
    }

    private void sortActivityBeanList(List<ActivityBean> list) {
        Collator collator = Collator.getInstance();
        if (list != null) {
            Collections.sort(list, (o1, o2) -> {
                String label1 = o1.getLabel();
                String label2 = o2.getLabel();
                return collator.compare(label1, label2);
            });
        }
    }

    private void updateActivityBeanList(AppListOp op, @Nullable String packageName) {
        executorService.execute(() -> {
            List<ActivityBean> list;
            if (mActivityBeanList.getValue() == null || op == AppListOp.INIT) {
                // initialize
                list = new ArrayList<>(ApplicationUtils.getActivityBeanList(getApplication(), null));
                sortActivityBeanList(list);
                mCurrentScreen.postValue(1);
            } else {
                list = new ArrayList<>(mActivityBeanList.getValue());
            }
            if (packageName != null) {
                // Update ActivityBeanList
                if (op == AppListOp.REMOVE || op == AppListOp.REPLACE) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            list.removeIf(item ->
                                    Objects.equals(packageName, item.getPackageName()));
                        } else {
                            CollectionUtils.removeConditionally(list, item ->
                                    Objects.equals(packageName, item.getPackageName()));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Cannot remove activity beans of " + packageName);
                    }
                }
                if (op == AppListOp.ADD || op == AppListOp.REPLACE) {
                    try {
                        list.addAll(ApplicationUtils.getActivityBeanList(getApplication(), packageName));
                    } catch (Exception e) {
                        Log.e(TAG, "Cannot add activity beans of " + packageName);
                    }
                    sortActivityBeanList(list);
                }
                // Update Database
                if (op == AppListOp.REMOVE) {
                    repository.deleteIconsByPackage(packageName);
                }
                if (op == AppListOp.REPLACE) {
                    // TODO: Ask LauncherLayout to reload activity if package is updated
                }
            } else if (op != AppListOp.INIT) {
                Log.e(TAG, "Package name is null. Specified operation has not been executed.");
            }
            if (isUpdatingList.compareAndSet(false, true)) {
                try {
                    synchronized (updateListLock) {
                        mActivityBeanList.postValue(list);
                    }
                } finally {
                    isUpdatingList.set(false);
                }
            }
        });
    }

    public void setLauncherState(LauncherState state) {
        mLauncherState.postValue(state);
    }

    public LiveData<LauncherState> getLauncherState() {
        return mLauncherState;
    }

    public LiveData<Boolean> getDisplayStatusBar() {
        return mDisplayStatusBar;
    }

    public LiveData<Boolean> getDisplayTopBar() {
        return mDisplayTopBar;
    }

    public LiveData<Boolean> getAnimation() {
        return mAnimation;
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

    public LiveData<Integer> getNumRow() {
        return mNumRow;
    }

    public LiveData<Integer> getNumColumn() {
        return mNumColumn;
    }

    public int getNumColumnValue() {
        return mNumColumn.getValue() == null ? defNumColumn : mNumColumn.getValue();
    }

    public int getNumScreenValue() {
        if (numScreen != null) {
            Integer value = numScreen.getValue();
            if (value != null) {
                return value;
            }
        }
        return 1;
    }

    public void setCurrentPage(int newValue) {
        mCurrentScreen.postValue(newValue);
    }

    public LiveData<Integer> getCurrentScreen() {
        return mCurrentScreen;
    }

    public int getCurrentScreenValue() {
        return mCurrentScreen.getValue() == null ? 1 : mCurrentScreen.getValue();
    }

    public LiveData<List<ActivityBean>> getActivityBeanList() {
        return mActivityBeanList;
    }

    public List<ActivityBean> getActivityBeanListValue() {
        return mActivityBeanList.getValue() == null ? new ArrayList<>() : mActivityBeanList.getValue();
    }

    public LiveData<Map<Integer, List<IconEntity>>> getLauncherIconMap() {
        return launcherIconMap;
    }

    public LiveData<Integer> getNumScreen() {
        return numScreen;
    }

    public void removeIcon(@NonNull IconEntity iconEntity) {
        repository.deleteIcon(iconEntity, true);
    }

    public void removeIconsByPackageName(@NonNull String packageName) {
        repository.deleteIconsByPackage(packageName);
    }
}