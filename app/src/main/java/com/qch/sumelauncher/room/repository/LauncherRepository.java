package com.qch.sumelauncher.room.repository;

import android.content.ComponentName;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.qch.sumelauncher.room.dao.LauncherDao;
import com.qch.sumelauncher.room.database.LauncherDatabase;
import com.qch.sumelauncher.room.entity.IconEntity;
import com.qch.sumelauncher.room.entity.LayoutEntity;
import com.qch.sumelauncher.room.entity.PageEntity;
import com.qch.sumelauncher.room.relation.PageWithIconEntities;
import com.qch.sumelauncher.utils.ApplicationUtils;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LauncherRepository {
    private static final String TAG = "LauncherRepository";
    private final LauncherDao dao;
    private final Context appContext;

    public LauncherRepository(Context context) {
        LauncherDatabase db = LauncherDatabase.getInstance(context);
        this.dao = db.launcherIconDao();
        this.appContext = context.getApplicationContext();
    }

    // layout

    /**
     * Get all layouts in database.
     */
    public List<LayoutEntity> getAllLayouts() {
        return dao.getAllLayouts();
    }

    /**
     * Get the default layout in database.
     */
    public LayoutEntity getDefaultLayout() {
        return dao.getDefaultLayout();
    }

    /**
     * Get the layout with the given size (columns * rows).
     */
    public LayoutEntity getLayoutBySize(int columnCount, int rowCount) {
        return dao.getLayoutBySize(columnCount, rowCount);
    }

    /**
     * Insert the layout described by the given LayoutEntity.
     */
    public void insertLayout(@NonNull LayoutEntity layoutEntity) {
        dao.insertLayout(layoutEntity);
    }

    // page

    /**
     * Get pages by layout name.
     */
    public LiveData<List<PageEntity>> getPagesByLayout(@NonNull String layoutName) {
        return dao.getPagesByLayout(layoutName);
    }

    /**
     * Get number of pages in the layout with the given layoutName.
     */
    public LiveData<Integer> getPageCount(@NonNull String layoutName) {
        return dao.getPageCountLiveData(layoutName);
    }

    /**
     * Get pageId of the page with the given position.
     */
    public long getPageIdByPosition(@NonNull String layoutName, int pageRank) {
        return dao.getPageIdByPositionSync(layoutName, pageRank);
    }

    /**
     * Insert the page described by the given PageEntity.
     */
    public Completable insertPage(@NonNull PageEntity pageEntity) {
        return dao.insertPageAsync(pageEntity);
    }

    /**
     * Insert a page at {@code targetPageRank} in {@code layoutName}. This method is synchronous.
     */
    public Completable insertPageAt(@NonNull String layoutName, int targetPageRank) {
        return Completable
                .fromCallable(() -> {
                    return dao.shiftAndInsertPageSync(layoutName, targetPageRank);
                })
                .subscribeOn(Schedulers.io());
    }

    public Single<Long> insertPageAndAddIcon(String layoutName, int targetPageRank) {
        return Single
                .fromCallable(() -> {
                    ComponentName componentName
                            = ApplicationUtils.getLauncherActivity(appContext, appContext.getPackageName());
                    if (componentName == null) {
                        return -1L;
                    }
                    return dao.shiftAndInsertPageSync(layoutName, targetPageRank);
                })
                .subscribeOn(Schedulers.io());
    }

    public Completable deletePageAt(String layoutName, int pageRank) {
        return Completable
                .fromCallable(() -> {
                    int pageCount = dao.getPageCountSync(layoutName);
                    if (pageCount > 1) {
                        dao.deleteAndShiftPageSync(layoutName, pageRank);
                        return true;
                    } else {
                        return false;
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    /**
     * Shift all pages whose {@code pageRank} is no less than {@code targetPageRank} right. This
     * method is synchronous.
     */
    public void shiftPageRight(@NonNull String layoutName, int targetPageRank) {
        dao.shiftPageRightSync(layoutName, targetPageRank);
    }

    // icons
    public LiveData<List<IconEntity>> getIconListInLayout(@NonNull String layoutName) {
        return dao.getIconListInLayout(layoutName);
    }

    public LiveData<List<PageWithIconEntities>> getPagedIconListInLayout(@NonNull String layoutName) {
        return dao.getPagedIconListInLayout(layoutName);
    }

    public LiveData<List<IconEntity>> getIconListOnPage(long pageId) {
        return dao.getIconListOnPage(pageId);
    }

    public LiveData<List<IconEntity>> getIconListByPagePosition(@NonNull String layoutName, int pageRank) {
        return dao.getIconListByPagePosition(layoutName, pageRank);
    }

    public Single<Integer> getIconCountByPageRank(@NonNull String layoutName, int pageRank) {
        return dao.getIconCountByPageRank(layoutName, pageRank);
    }

    public LiveData<List<IconEntity>> getIconsOnScreen(@NonNull String layoutName, int pageRank) {
        return dao.getIconListByPagePosition(layoutName, pageRank);
    }

    public boolean isCellOccupied(@NonNull String layoutName, int pageRank, int cellX, int cellY) {
        return dao.isCellOccupied(layoutName, pageRank, cellX, cellY);
    }

    public boolean[][] getOccupiedCells(LayoutEntity layoutEntity, int pageRank) {
        String layoutName = layoutEntity.getName();
        int rows = layoutEntity.getRowCount();
        int columns = layoutEntity.getColumnCount();
        boolean[][] occupied = new boolean[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                occupied[i][j] = false;
            }
        }
        List<IconEntity> iconEntityList = getIconListByPagePosition(layoutName, pageRank).getValue();
        if (iconEntityList != null) {
            for (IconEntity iconEntity : iconEntityList) {
                for (int y = 0; y < iconEntity.getSpanY(); y++) {
                    for (int x = 0; x < iconEntity.getSpanX(); x++) {
                        int targetY = iconEntity.getCellY();
                        int targetX = iconEntity.getCellX();
                        if (targetY < rows && targetX < columns) {
                            occupied[targetY][targetX] = true;
                        }
                    }
                }
            }
        }
        return occupied;
    }

    public Completable insertIconAsync(IconEntity iconEntity) {
        return dao.insertIconAsync(iconEntity)
                .subscribeOn(Schedulers.io());
    }

    public Completable insertIconAsync(String layoutName, int pageRank, int cellX, int cellY,
                                       String packageName, String activityName) {
        return Completable
                .fromCallable(() -> {
                    long pageId = dao.getPageIdByPositionSync(layoutName, pageRank);
                    IconEntity iconEntity = new IconEntity(pageId, cellX, cellY, 1, 1,
                            packageName, activityName);
                    dao.insertIconSync(iconEntity);
                    return true;
                })
                .subscribeOn(Schedulers.io());
    }

    public Completable insertIconArray(IconEntity[] iconEntityArray) {
        return dao.insertIconArrayAsync(iconEntityArray);
    }

    public Completable insertIconListAsync(List<IconEntity> iconEntityList) {
        return dao.insertIconListAsync(iconEntityList)
                .subscribeOn(Schedulers.io());
    }

    public void insertIconListSync(List<IconEntity> iconEntityList) {
        dao.insertIconListSync(iconEntityList);
    }

    /**
     * Delete the icon specified by the given {@code iconEntity}. This method is asynchronous.
     */
    public Completable deleteIcon(IconEntity iconEntity) {
        return dao.deleteIconAsync(iconEntity)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Delete the icons specified by the given {@code iconEntities}. This method is asynchronous.
     */
    public Completable deleteIcons(IconEntity[] iconEntities) {
        return dao.deleteIconsAsync(iconEntities)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Delete the icons specified by the given list of {@code iconEntities}. This method is
     * synchronous.
     */
    public void deleteIconListSync(List<IconEntity> iconEntityList) {
        dao.deleteIconListSync(iconEntityList);
    }

    /**
     * Delete the icon specified by the given {@code layoutName}, {@code pageRank}, {@code cellX}
     * and {@code cellY}. This method is asynchronous.
     */
    public Completable deleteIconByPositionAsync(String layoutName, int pageRank, int cellX, int cellY) {
        return dao.deleteIconByPosition(layoutName, pageRank, cellX, cellY)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Delete icons on the page specified by the given {@code pageRank} and {@code layoutName}.
     * This method is asynchronous.
     */
    public Completable deleteIconsOnPage(String layoutName, int pageRank) {
        return dao.deleteIconsOnPage(layoutName, pageRank)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Delete icons of package with the given {@code packageName}. This method is asynchronous.
     */
    public Completable deleteIconsByPackage(String packageName) {
        return dao.deleteIconsByPackage(packageName)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Delete icons of activity with the given {@code packageName} and {@code activityName}. This
     * method is asynchronous.
     */
    public Completable deleteIconsByActivity(String packageName, String activityName) {
        return dao.deleteIconsByActivity(packageName, activityName)
                .subscribeOn(Schedulers.io());
    }
}