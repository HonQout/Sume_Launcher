package com.qch.sumelauncher.ui.launcher.activity;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.qch.sumelauncher.application.MyApplication;
import com.qch.sumelauncher.data.model.launcher.ActivityModel;
import com.qch.sumelauncher.compat.CollectionCompat;
import com.qch.sumelauncher.data.model.launcher.GridSize;
import com.qch.sumelauncher.data.model.launcher.IconModel;
import com.qch.sumelauncher.room.entity.IconEntity;
import com.qch.sumelauncher.room.repository.LauncherIconRepository;
import com.qch.sumelauncher.utils.ApplicationUtils;

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

    public enum LauncherState {
        LAUNCHER, APPS
    }

    public enum AppListOp {
        INIT, ADD, REMOVE, REPLACE
    }

    // data
    private LauncherState launcherState = LauncherState.LAUNCHER;
    private LiveData<Integer> numScreen;
    private LiveData<Map<Integer, List<IconEntity>>> iconEntityMap;
    private LiveData<Map<Integer, List<IconModel>>> iconModelMap;
    private final MutableLiveData<GridSize> mGridSize
            = new MutableLiveData<>(new GridSize(GridSize.DEFAULT_NUM_ROW, GridSize.DEFAULT_NUM_COLUMN));
    private final MutableLiveData<Integer> mCurrentScreenIndex = new MutableLiveData<>();
    private final MutableLiveData<List<ActivityModel>> mActivityModelList = new MutableLiveData<>();

    // multi-thread
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final AtomicBoolean isUpdatingList = new AtomicBoolean(false);
    private final Object updateListLock = new Object();

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
                updateActivityModelList(AppListOp.INIT, null);
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
                                    updateActivityModelList(AppListOp.ADD, packageName);
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
                                    updateActivityModelList(AppListOp.REMOVE, packageName);
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
                                updateActivityModelList(AppListOp.REPLACE, packageName);
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
        iconEntityMap = Transformations.switchMap(mGridSize, gridSize ->
                repository.getIconEntityMapInLayout(gridSize.toString()));
        iconModelMap = Transformations.switchMap(mGridSize, gridSize ->
                repository.getIconModelMapInLayout(gridSize.toString()));
        numScreen = Transformations.switchMap(mGridSize, gridSize ->
                repository.getNumScreens(gridSize.toString()));
    }

    private void initDisposable() {
        // grid_size
        Disposable disposable = MyApplication.getPreferenceDataStore()
                .getStringFlowable("grid_size", "5,5")
                .subscribe(string -> {
                            GridSize gridSize = GridSize.parse(string,
                                    new GridSize(GridSize.DEFAULT_NUM_ROW, GridSize.DEFAULT_NUM_COLUMN));
                            mGridSize.postValue(gridSize);
                            updateActivityModelList(AppListOp.INIT, null);
                        },
                        throwable -> Log.e(TAG, "Cannot get value of key grid_size.", throwable)
                );
        compositeDisposable.add(disposable);
    }

    private void sortActivityModelList(List<ActivityModel> list) {
        Collator collator = Collator.getInstance();
        if (list != null) {
            Collections.sort(list, (o1, o2) -> {
                String label1 = o1.getLabel();
                String label2 = o2.getLabel();
                return collator.compare(label1, label2);
            });
        }
    }

    private void updateActivityModelList(AppListOp op, @Nullable String packageName) {
        executorService.execute(() -> {
            List<ActivityModel> list;
            if (mActivityModelList.getValue() == null || op == AppListOp.INIT) {
                // initialize
                list = new ArrayList<>(ApplicationUtils.getActivityModelList(getApplication(), null));
                sortActivityModelList(list);
            } else {
                list = new ArrayList<>(mActivityModelList.getValue());
            }
            if (packageName != null) {
                // Update ActivityModelList
                if (op == AppListOp.REMOVE || op == AppListOp.REPLACE) {
                    try {
                        CollectionCompat.removeIf(list,
                                item -> Objects.equals(packageName, item.getPackageName()));
                    } catch (Exception e) {
                        Log.e(TAG, "Cannot remove activity models of " + packageName);
                    }
                }
                if (op == AppListOp.ADD || op == AppListOp.REPLACE) {
                    try {
                        list.addAll(ApplicationUtils.getActivityModelList(getApplication(), packageName));
                    } catch (Exception e) {
                        Log.e(TAG, "Cannot add activity models of " + packageName);
                    }
                    sortActivityModelList(list);
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
                        mActivityModelList.postValue(list);
                    }
                } finally {
                    isUpdatingList.set(false);
                }
            }
        });
    }

    public void setLauncherState(LauncherState launcherState) {
        this.launcherState = launcherState;
    }

    public LauncherState getLauncherState() {
        return launcherState;
    }

    public LiveData<GridSize> getGridSize() {
        return mGridSize;
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

    public void setCurrentScreenIndex(int newValue) {
        mCurrentScreenIndex.postValue(newValue);
    }

    public LiveData<Integer> getCurrentScreenIndex() {
        return mCurrentScreenIndex;
    }

    public int getCurrentScreenIndexValue() {
        return mCurrentScreenIndex.getValue() == null ? 0 : mCurrentScreenIndex.getValue();
    }

    public LiveData<List<ActivityModel>> getActivityModelList() {
        return mActivityModelList;
    }

    public LiveData<Map<Integer, List<IconEntity>>> getIconEntityMap() {
        return iconEntityMap;
    }

    public LiveData<Map<Integer, List<IconModel>>> getIconModelMap() {
        return iconModelMap;
    }

    public LiveData<Integer> getNumScreen() {
        return numScreen;
    }

    public void insertIcon(@NonNull IconEntity iconEntity) {
        repository.insertIcon(iconEntity);
    }

    public void removeIcon(@NonNull IconEntity iconEntity) {
        repository.deleteIcon(iconEntity, true);
    }

    public void removeIconsByPackageName(@NonNull String packageName) {
        repository.deleteIconsByPackage(packageName);
    }

    public boolean deleteScreen() {
        if (mGridSize == null) {
            return false;
        }
        GridSize gridSize = mGridSize.getValue();
        if (gridSize == null) {
            return false;
        }
        String layoutName = gridSize.toString();

        int screenIndex = getCurrentScreenIndexValue();

        return deleteScreen(layoutName, screenIndex);
    }

    public boolean deleteScreen(String layoutName, int screenIndex) {
        if (getNumScreenValue() > 1) {
            repository.deleteScreen(layoutName, screenIndex);
            return true;
        } else {
            return false;
        }
    }
}