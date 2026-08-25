package com.qch.sumelauncher.room.repository;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LauncherRepository {
    private static final String TAG = "LauncherRepository";
    private final LauncherDao dao;
    private final Context appContext;
    private final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

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
     * Insert a page at {@code targetPageRank} in {@code layoutName} after shifting other pages
     * whose {@code pageRank}s are no less than it right. Theoretically, this method will not run
     * into conflicts.
     * <p>This method should be used to insert pages after the database is initialized.
     * <p>This method is asynchronous.
     */
    public Completable insertPageAt(@NonNull String layoutName, int targetPageRank) {
        return Completable
                .fromAction(() -> {
                    LauncherDatabase.getInstance(appContext).runInTransaction(() -> {
                        dao.shiftPagesRightSync(layoutName, targetPageRank);
                        dao.insertPageSync(new PageEntity(layoutName, targetPageRank));
                    });
                })
                .subscribeOn(Schedulers.from(singleThreadExecutor));
    }

    /**
     * Delete a page at {@code pageRank} in {@code layoutName} and shift other pages whose
     * {@code pageRank}s are bigger than it left. Theoretically, this method will not run into
     * conflicts.
     * <p>This method should be used to delete pages after the database is initialized.
     * <p>This method is asynchronous.
     */
    public Completable deletePageAt(String layoutName, int pageRank) {
        return Completable
                .fromAction(() -> {
                    int pageCount = dao.getPageCountSync(layoutName);
                    if (pageCount > 1) {
                        dao.deletePageSync(layoutName, pageRank);
                        dao.shiftPagesLeftSync(layoutName, pageRank);
                    }
                })
                .subscribeOn(Schedulers.from(singleThreadExecutor));
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
                .subscribeOn(Schedulers.from(singleThreadExecutor));
    }

    public Completable insertIconAsync(String layoutName, int pageRank, int cellX, int cellY,
                                       String packageName, String activityName) {
        return Completable
                .fromAction(() -> {
                    long pageId = dao.getPageIdByPositionSync(layoutName, pageRank);
                    IconEntity iconEntity = new IconEntity(pageId, cellX, cellY, 1, 1,
                            packageName, activityName);
                    dao.insertIconSync(iconEntity);
                })
                .subscribeOn(Schedulers.from(singleThreadExecutor));
    }

    public Completable insertIconArray(IconEntity[] iconEntityArray) {
        return dao.insertIconArrayAsync(iconEntityArray);
    }

    public Completable insertIconListAsync(List<IconEntity> iconEntityList) {
        return dao.insertIconListAsync(iconEntityList)
                .subscribeOn(Schedulers.from(singleThreadExecutor));
    }

    public void insertIconListSync(List<IconEntity> iconEntityList) {
        dao.insertIconListSync(iconEntityList);
    }

    /**
     * Delete the icon specified by the given {@code iconEntity}.
     * <p>This method is asynchronous.
     * <p>Notice: This method is intended to delete an IconEntity. However, field {@code id} of
     * IconEntity cannot be accessed from the outside, so the object of IconEntity in parameter
     * {@code iconEntity} may not be identical with the one having the same other fields in the
     * database. This causes the operation of deleting not to be proceeded. In this case, please
     * consider use {@link LauncherRepository#deleteIconByPositionAsync(String, int, int, int)} to
     * delete them by positions.
     */
    public Completable deleteIconAsync(IconEntity iconEntity) {
        return dao.deleteIconAsync(iconEntity)
                .subscribeOn(Schedulers.from(singleThreadExecutor));
    }

    /**
     * Delete the icons specified by the given {@code iconEntities}.
     * <p>This method is asynchronous.
     * <p>Notice: This method is intended to delete an array of IconEntities. However, field
     * {@code id} of IconEntity cannot be accessed from the outside, so the objects of IconEntities
     * in parameter {@code iconEntities} may not be identical with the ones having the same other
     * fields in the database. This causes the operation of deleting not to be proceeded. In this
     * case, consider use {@link LauncherRepository#deleteIconByPositionAsync(String, int, int, int)}
     * to delete them by positions.
     */
    public Completable deleteIconsAsync(IconEntity[] iconEntities) {
        return dao.deleteIconsAsync(iconEntities)
                .subscribeOn(Schedulers.from(singleThreadExecutor));
    }

    /**
     * Delete the icons specified by the given list of {@code iconEntities}.
     * <p>This method is synchronous.
     * <p>Notice: This method is intended to delete a list of IconEntities. However, field
     * {@code id} of IconEntity cannot be accessed from the outside, so the objects of IconEntities
     * in parameter {@code iconEntityList} may not be identical with the ones having the same other
     * fields in the database. This causes the operation of deleting not to be proceeded. In this
     * case, consider use {@link LauncherRepository#deleteIconByPositionAsync(String, int, int, int)}
     * to delete them by positions.
     */
    public void deleteIconListSync(List<IconEntity> iconEntityList) {
        dao.deleteIconListSync(iconEntityList);
    }

    /**
     * Delete the icon specified by the given {@code layoutName}, {@code pageRank}, {@code cellX}
     * and {@code cellY}.
     * <p>This method is asynchronous.
     */
    public Completable deleteIconByPositionAsync(String layoutName, int pageRank, int cellX, int cellY) {
        return dao.deleteIconByPositionAsync(layoutName, pageRank, cellX, cellY)
                .subscribeOn(Schedulers.from(singleThreadExecutor));
    }

    /**
     * Delete icons on the page specified by the given {@code pageRank} and {@code layoutName}.
     * <p>This method is asynchronous.
     */
    public Completable deleteIconsOnPageAsync(String layoutName, int pageRank) {
        return dao.deleteIconsOnPage(layoutName, pageRank)
                .subscribeOn(Schedulers.from(singleThreadExecutor));
    }

    /**
     * Delete icons of package with the given {@code packageName}.
     * <p>This method is asynchronous.
     */
    public Completable deleteIconsByPackageAsync(String packageName) {
        return dao.deleteIconsByPackage(packageName)
                .subscribeOn(Schedulers.from(singleThreadExecutor));
    }

    /**
     * Delete icons of activity with the given {@code packageName} and {@code activityName}.
     * <p>This method is asynchronous.
     */
    public Completable deleteIconsByActivityAsync(String packageName, String activityName) {
        return dao.deleteIconsByActivity(packageName, activityName)
                .subscribeOn(Schedulers.io());
    }

    /**
     * Delete invalid icons of package with the given {@code packageName}.
     * <p>This method is asynchronous.
     */
    public Single<Boolean> deleteInvalidIconsByPackageAsync(String packageName) {
        return dao.getIconListByPackageName(packageName)
                .subscribeOn(Schedulers.from(singleThreadExecutor))
                .map(iconEntityList -> {
                    for (IconEntity iconEntity : iconEntityList) {
                        if (!ApplicationUtils.hasActivity(appContext, iconEntity.getPackageName(),
                                iconEntity.getActivityName())) {
                            dao.deleteIconSync(iconEntity);
                        }
                    }
                    return true;
                });
    }
}