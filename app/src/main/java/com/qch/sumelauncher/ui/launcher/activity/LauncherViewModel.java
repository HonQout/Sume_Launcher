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
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.qch.sumelauncher.application.MyApplication;
import com.qch.sumelauncher.data.model.launcher.ActivityModel;
import com.qch.sumelauncher.compat.CollectionCompat;
import com.qch.sumelauncher.data.model.launcher.GridSize;
import com.qch.sumelauncher.data.model.launcher.IconModel;
import com.qch.sumelauncher.data.model.launcher.PageWithIconModels;
import com.qch.sumelauncher.room.entity.IconEntity;
import com.qch.sumelauncher.room.relation.PageWithIconEntities;
import com.qch.sumelauncher.room.repository.LauncherRepository;
import com.qch.sumelauncher.utils.ApplicationUtils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LauncherViewModel extends AndroidViewModel {
    private static final String TAG = "LauncherViewModel";

    public enum LauncherState {
        LAUNCHER, APPS
    }

    public enum AppListOp {
        INIT, ADD, REMOVE, REPLACE
    }

    public enum InsertPagePosition {
        BEFORE, AFTER
    }

    // data
    private final MutableLiveData<GridSize> mGridSize = new MutableLiveData<>(GridSize.DEFAULT_VALUE);
    private LiveData<Integer> pageCount;
    private final MediatorLiveData<List<PageWithIconEntities>> pagedIconEntityList = new MediatorLiveData<>();
    private final MediatorLiveData<List<PageWithIconModels>> pagedIconModelList = new MediatorLiveData<>();
    private final MutableLiveData<List<ActivityModel>> mActivityModelList = new MutableLiveData<>();
    private LauncherState launcherState = LauncherState.LAUNCHER;
    private int currentPageIndex;

    // multi-thread
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final AtomicBoolean isUpdatingList = new AtomicBoolean(false);
    private final Object updateListLock = new Object();

    // persistence
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private LauncherRepository repository;

    // broadcast receiver
    private BroadcastReceiver localeBroadcastReceiver = null;
    private BroadcastReceiver packageBroadcastReceiver = null;


    public LauncherViewModel(@NonNull Application application) {
        super(application);
        repository = new LauncherRepository(getApplication());

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
        LiveData<List<PageWithIconEntities>> rawPagedIconEntityList
                = Transformations.switchMap(mGridSize, gridSize ->
                gridSize == null ? new MutableLiveData<>(Collections.emptyList()) :
                        repository.getPagedIconListInLayout(gridSize.toString())
        );
        pagedIconModelList.addSource(rawPagedIconEntityList, rawList -> {
            if (rawList == null) {
                pagedIconEntityList.postValue(new ArrayList<>());
                return;
            }
            executorService.execute(() -> {
                List<PageWithIconEntities> pageWithIconEntitiesList = new ArrayList<>();
                List<PageWithIconModels> pageWithIconModelsList = new ArrayList<>();
                List<IconEntity> invalidIcons = new ArrayList<>();
                for (PageWithIconEntities pageWithIconEntities : rawList) {
                    List<IconEntity> validIconEntityList = new ArrayList<>();
                    List<IconModel> validIconModelList = new ArrayList<>();
                    if (pageWithIconEntities.list != null) {
                        for (IconEntity iconEntity : pageWithIconEntities.list) {
                            if (ApplicationUtils.hasActivity(getApplication(),
                                    iconEntity.getPackageName(), iconEntity.getActivityName())) {
                                validIconEntityList.add(iconEntity);
                                validIconModelList.add(new IconModel(getApplication(), iconEntity));
                            } else {
                                invalidIcons.add(iconEntity);
                            }
                        }
                    }
                    PageWithIconEntities cleanedPageWithIconEntities = new PageWithIconEntities();
                    cleanedPageWithIconEntities.page = pageWithIconEntities.page;
                    cleanedPageWithIconEntities.list = validIconEntityList;
                    pageWithIconEntitiesList.add(cleanedPageWithIconEntities);
                    pageWithIconModelsList.add(new PageWithIconModels(pageWithIconEntities.page, validIconModelList));
                }
                pagedIconEntityList.postValue(pageWithIconEntitiesList);
                pagedIconModelList.postValue(pageWithIconModelsList);
                if (!invalidIcons.isEmpty()) {
                    repository.deleteIconListSync(invalidIcons);
                }
            });
        });
        pageCount = Transformations.switchMap(mGridSize, gridSize ->
                repository.getPageCount(gridSize.toString()));
    }

    private void initDisposable() {
        // grid_size
        Disposable disposable = MyApplication.getPreferenceDataStore()
                .getStringFlowable("grid_size", GridSize.DEFAULT_VALUE.toString())
                .subscribe(string -> {
                            GridSize gridSize = GridSize.parse(string, GridSize.DEFAULT_VALUE);
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
                    deleteIconsByPackage(packageName);
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

    public String getGridSizeString() {
        return mGridSize.getValue() == null ? null : mGridSize.getValue().toString();
    }

    public LiveData<Integer> getPageCount() {
        return pageCount;
    }

    public int getPageCountValue() {
        if (pageCount != null) {
            Integer value = pageCount.getValue();
            if (value != null) {
                return value;
            }
        }
        return 1;
    }

    public void setCurrentPageIndex(int newValue) {
        if (newValue >= 0 && newValue < getPageCountValue()) {
            currentPageIndex = newValue;
        }
    }

    public int getCurrentPageIndex() {
        return currentPageIndex;
    }

    public LiveData<List<ActivityModel>> getActivityModelList() {
        return mActivityModelList;
    }

    public LiveData<List<PageWithIconModels>> getPagedIconModelList() {
        return pagedIconModelList;
    }

    public Completable insertIconAsync(@NonNull IconEntity iconEntity) {
        return repository.insertIconAsync(iconEntity)
                .subscribeOn(Schedulers.io());
    }

    public Completable insertIconAsync(int cellX, int cellY, String packageName, String activityName) {
        String layoutName = getGridSizeString();
        if (layoutName == null) {
            throw new IllegalArgumentException("Layout name is null.");
        }

        int pageRank = getCurrentPageIndex();
        if (pageRank < 0) {
            throw new IllegalArgumentException("Current page rank is null.");
        }

        return repository.insertIconAsync(layoutName, pageRank, cellX, cellY, packageName, activityName)
                .subscribeOn(Schedulers.io());
    }

    public void deleteIconAsync(@NonNull IconEntity iconEntity) {
        repository.deleteIcon(iconEntity)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void deleteIconsAsync(@NonNull IconEntity[] iconEntities) {
        repository.deleteIcons(iconEntities)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void deleteIconByPositionAsync(@NonNull IconEntity iconEntity) {
        String layoutName = getGridSizeString();
        if (layoutName == null) {
            return;
        }

        int pageRank = getCurrentPageIndex();
        if (pageRank < 0) {
            return;
        }

        deleteIconByPositionAsync(layoutName, pageRank, iconEntity.getCellX(), iconEntity.getCellY());
    }

    public void deleteIconByPositionAsync(@NonNull String layoutName, int pageRank, int cellX, int cellY) {
        repository.deleteIconByPositionAsync(layoutName, pageRank, cellX, cellY)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void deleteIconsByPackage(@NonNull String packageName) {
        repository.deleteIconsByPackage(packageName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void insertPage(String layoutName, InsertPagePosition position) {
        if (getCurrentPageIndex() < 0) {
            return;
        }
        switch (position) {
            case BEFORE: {
                repository.insertPageAt(layoutName, getCurrentPageIndex())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe();
                break;
            }
            case AFTER: {
                repository.insertPageAt(layoutName, getCurrentPageIndex() + 1)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe();
                break;
            }
            default: {
                throw new IllegalArgumentException("Argument position is illegal.");
            }
        }
    }

    public void insertPage(InsertPagePosition position) {
        String layoutName = getGridSizeString();
        insertPage(layoutName, position);
    }

    public void deletePage() {
        String layoutName = getGridSizeString();
        if (layoutName == null) {
            return;
        }

        int pageRank = getCurrentPageIndex();
        if (pageRank < 0) {
            return;
        }

        deletePage(layoutName, pageRank);
    }

    public void deletePage(String layoutName, int pageRank) {
        repository.deletePageAt(layoutName, pageRank)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }
}