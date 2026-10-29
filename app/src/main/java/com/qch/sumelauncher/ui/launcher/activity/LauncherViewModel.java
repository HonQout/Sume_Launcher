package com.qch.sumelauncher.ui.launcher.activity;

import android.app.Application;
import android.content.pm.LauncherApps;
import android.os.UserHandle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.qch.sumelauncher.application.MyApplication;
import com.qch.sumelauncher.compat.ListCompat;
import com.qch.sumelauncher.data.model.launcher.ActivityModel;
import com.qch.sumelauncher.compat.CollectionCompat;
import com.qch.sumelauncher.data.model.launcher.GridSize;
import com.qch.sumelauncher.data.model.launcher.IconModel;
import com.qch.sumelauncher.data.model.launcher.PageWithIconModels;
import com.qch.sumelauncher.data.repository.AppRepository;
import com.qch.sumelauncher.multithread.AppExecutors;
import com.qch.sumelauncher.room.entity.IconEntity;
import com.qch.sumelauncher.room.relation.PageWithIconEntities;
import com.qch.sumelauncher.room.repository.LauncherRepository;
import com.qch.sumelauncher.utils.ApplicationUtils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LauncherViewModel extends AndroidViewModel {
    private static final String TAG = "LauncherViewModel";

    public enum LauncherState {
        LAUNCHER,
        APPS
    }

    public enum AppListOp {
        INIT,
        ADD,
        REMOVE,
        REPLACE,
        DISABLE,
        ENABLE
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
    private final MutableLiveData<Integer> mDrawerColumnCount = new MutableLiveData<>();
    private LauncherState launcherState = LauncherState.LAUNCHER;
    private int currentPageIndex;

    // persistence
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    // repository
    private LauncherRepository launcherRepository;
    private AppRepository appRepository;

    public LauncherViewModel(@NonNull Application application) {
        super(application);
        launcherRepository = new LauncherRepository(getApplication());
        initLauncherApps();
        initLauncherLayout();
        initDisposable();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        appRepository.unregister();
        compositeDisposable.clear();
    }

    public void onLocaleChange() {
        updateActivityModelList(AppListOp.INIT, null);
        refreshPagedIconModelList();
    }

    private void initLauncherApps() {
        appRepository = new AppRepository(
                getApplication(),
                new LauncherApps.Callback() {
                    @Override
                    public void onPackageAdded(String packageName, UserHandle user) {
                        updateActivityModelList(AppListOp.ADD, packageName);
                    }

                    @Override
                    public void onPackageChanged(String packageName, UserHandle user) {
                        updateActivityModelList(AppListOp.REPLACE, packageName);
                    }

                    @Override
                    public void onPackageRemoved(String packageName, UserHandle user) {
                        updateActivityModelList(AppListOp.REMOVE, packageName);
                    }

                    @Override
                    public void onPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing) {

                    }

                    @Override
                    public void onPackagesUnavailable(String[] packageNames, UserHandle user, boolean replacing) {

                    }

                    @Override
                    public void onPackagesSuspended(String[] packageNames, UserHandle user) {
                        for (String packageName : packageNames) {
                            updateActivityModelList(AppListOp.DISABLE, packageName);
                        }
                    }

                    @Override
                    public void onPackagesUnsuspended(String[] packageNames, UserHandle user) {
                        for (String packageName : packageNames) {
                            updateActivityModelList(AppListOp.ENABLE, packageName);
                        }
                    }
                }
        );
        appRepository.register();
    }

    private void initDisposable() {
        // grid_size
        Disposable disposable = MyApplication.getPreferenceDataStoreImpl()
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
        AppExecutors.getInstance().cpu().execute(() -> {
                    List<ActivityModel> list;
                    if (mActivityModelList.getValue() == null || op == AppListOp.INIT) {
                        // initialize
                        list = new ArrayList<>(appRepository.getLaunchableActivityModels(getApplication(), null));
                        sortActivityModelList(list);
                    } else {
                        list = new ArrayList<>(mActivityModelList.getValue());
                    }
                    if (packageName != null) {
                        // Update ActivityModelList
                        if (op == AppListOp.REMOVE || op == AppListOp.REPLACE || op == AppListOp.DISABLE) {
                            try {
                                CollectionCompat.removeIf(
                                        list,
                                        item -> Objects.equals(packageName, item.getPackageName())
                                );
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to remove activity models of " + packageName);
                            }
                        }
                        if (op == AppListOp.ADD || op == AppListOp.REPLACE || op == AppListOp.ENABLE) {
                            try {
                                list.addAll(appRepository.getLaunchableActivityModels(getApplication(), packageName));
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to add activity models of " + packageName);
                            }
                            sortActivityModelList(list);
                        }
                        // Update Database
                        if (op == AppListOp.REMOVE || op == AppListOp.DISABLE) {
                            deleteIconsByPackageAsync(packageName);
                        }
                        if (op == AppListOp.REPLACE) {
                            deleteInvalidIconsByPackageAsync(packageName);
                        }
                    } else if (op != AppListOp.INIT) {
                        Log.e(TAG, "Package name is null. Specified operation has not been executed.");
                    }
                    mActivityModelList.postValue(list);
                }
        );
    }

    private void initLauncherLayout() {
        LiveData<List<PageWithIconEntities>> rawPagedIconEntityList
                = Transformations.switchMap(mGridSize, gridSize ->
                gridSize == null ? new MutableLiveData<>(Collections.emptyList()) :
                        launcherRepository.getPagedIconListInLayout(gridSize.toString())
        );
        pagedIconModelList.addSource(rawPagedIconEntityList, rawList -> {
            if (rawList == null) {
                pagedIconEntityList.postValue(new ArrayList<>());
                return;
            }
            AppExecutors.getInstance().cpu().execute(() -> {
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
                    launcherRepository.deleteIconListSync(invalidIcons);
                }
            });
        });
        pageCount = Transformations.switchMap(mGridSize, gridSize ->
                launcherRepository.getPageCount(gridSize.toString()));
    }

    private void refreshPagedIconModelList() {
        AppExecutors.getInstance().cpu().execute(() -> {
            List<PageWithIconEntities> pageWithIconEntitiesList = pagedIconEntityList.getValue();
            if (ListCompat.isNullOrEmpty(pageWithIconEntitiesList)) {
                return;
            }
            List<PageWithIconModels> pageWithIconModelsList = new ArrayList<>();
            for (PageWithIconEntities pageWithIconEntities : pageWithIconEntitiesList) {
                List<IconModel> iconModelList = new ArrayList<>();
                if (pageWithIconEntities.list != null) {
                    for (IconEntity iconEntity : pageWithIconEntities.list) {
                        if (ApplicationUtils.hasActivity(getApplication(),
                                iconEntity.getPackageName(), iconEntity.getActivityName())) {
                            iconModelList.add(new IconModel(getApplication(), iconEntity));
                        }
                    }
                }
                pageWithIconModelsList.add(new PageWithIconModels(pageWithIconEntities.page, iconModelList));
            }
            pagedIconModelList.postValue(pageWithIconModelsList);
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

    public void setDrawerColumnCount(int newValue) {
        mDrawerColumnCount.postValue(newValue);
    }

    public LiveData<Integer> getDrawerColumnCount() {
        return mDrawerColumnCount;
    }

    public LiveData<List<ActivityModel>> getActivityModelList() {
        return mActivityModelList;
    }

    public LiveData<List<PageWithIconModels>> getPagedIconModelList() {
        return pagedIconModelList;
    }

    public Completable insertIconAsync(@NonNull IconEntity iconEntity) {
        return launcherRepository.insertIconAsync(iconEntity)
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

        return launcherRepository.insertIconAsync(layoutName, pageRank, cellX, cellY, packageName, activityName)
                .subscribeOn(Schedulers.io());
    }

    public void deleteIconAsync(@NonNull IconEntity iconEntity) {
        launcherRepository.deleteIconAsync(iconEntity)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void deleteIconsAsync(@NonNull IconEntity[] iconEntities) {
        launcherRepository.deleteIconsAsync(iconEntities)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void deleteIconByPositionAsync(@NonNull String layoutName, int pageRank, int cellX, int cellY) {
        launcherRepository.deleteIconByPositionAsync(layoutName, pageRank, cellX, cellY)
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

    public void deleteIconsByPackageAsync(@NonNull String packageName) {
        launcherRepository.deleteIconsByPackageAsync(packageName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void deleteInvalidIconsByPackageAsync(@NonNull String packageName) {
        launcherRepository.deleteInvalidIconsByPackageAsync(packageName)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    public void insertPage(String layoutName, InsertPagePosition position) {
        if (getCurrentPageIndex() < 0) {
            return;
        }
        switch (position) {
            case BEFORE: {
                launcherRepository.insertPageAt(layoutName, getCurrentPageIndex())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe();
                break;
            }
            case AFTER: {
                launcherRepository.insertPageAt(layoutName, getCurrentPageIndex() + 1)
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
        launcherRepository.deletePageAt(layoutName, pageRank)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }
}