package com.qch.sumelauncher.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.qch.sumelauncher.room.entity.IconEntity;
import com.qch.sumelauncher.room.entity.LayoutEntity;
import com.qch.sumelauncher.room.entity.PageEntity;
import com.qch.sumelauncher.room.relation.PageWithIconEntities;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * This interface is the DAO (Data Access Object) of Launcher of Room database.
 *
 */
@Dao
public interface LauncherDao {
    // layout
    @Query("SELECT * FROM layouts")
    List<LayoutEntity> getAllLayouts();

    @Query("SELECT * FROM layouts WHERE is_default = 1")
    LayoutEntity getDefaultLayout();

    @Query("SELECT * FROM layouts WHERE row_count = :rowCount AND column_count = :columnCount")
    LayoutEntity getLayoutBySize(int rowCount, int columnCount);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLayout(LayoutEntity layoutEntity);

    // page
    @Query("SELECT * FROM pages WHERE layout_name = :layoutName ORDER BY page_rank ASC")
    LiveData<List<PageEntity>> getPagesByLayout(String layoutName);

    @Query("SELECT COUNT(*) FROM pages WHERE layout_name = :layoutName")
    int getPageCountSync(String layoutName);

    @Query("SELECT COUNT(*) FROM pages WHERE layout_name = :layoutName")
    LiveData<Integer> getPageCountLiveData(String layoutName);

    @Query("SELECT page_id FROM pages WHERE layout_name = :layoutName AND page_rank = :pageRank")
    long getPageIdByPositionSync(String layoutName, int pageRank);

    /**
     * Shift the pages whose {@code pageRank} is no less than {@code targetPageRank} right.
     * <p>This method should only be called before calling
     * {@link LauncherDao#insertPageSync(PageEntity)}.
     * <p>This method is synchronous.
     */
    @Query("UPDATE pages SET page_rank = page_rank + 1 " +
            "WHERE layout_name = :layoutName AND page_rank >= :targetPageRank")
    void shiftPagesRightSync(String layoutName, int targetPageRank);

    /**
     * Insert a page into the database. Replace the existing page when running into conflicts.
     * <p>This method should only be called after calling
     * {@link LauncherDao#shiftPagesRightSync(String, int)} after the database is initialized,
     * because it will probably replace an existing page and cause data loss.
     * <p>This method is synchronous.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPageSync(PageEntity pageEntity);

    /**
     * Shift the pages whose {@code pageRank} is bigger than {@code targetPageRank} left.
     * <p>This method should only be called after calling
     * {@link LauncherDao#deletePageSync(String, int)}.
     * <p>This method is synchronous.
     */
    @Query("UPDATE pages SET page_rank = page_rank - 1 " +
            "WHERE layout_name = :layoutName AND page_rank > :targetPageRank")
    void shiftPagesLeftSync(String layoutName, int targetPageRank);

    /**
     * Delete a page from the database.
     * <p>This method should only be called after calling
     * {@link LauncherDao#shiftPagesLeftSync(String, int)} after the database is initialized,
     * because it will not change the ranks of pages after deleting. This will cause errors when
     * inserting icons or pages the next time.
     * <p>This method is synchronous.
     */
    @Query("DELETE FROM pages WHERE layout_name = :layoutName AND page_rank = :pageRank")
    void deletePageSync(String layoutName, int pageRank);

    // icon

    /**
     * Get the list of IconEntity in the layout with the given {@code layoutName}.
     */
    @Query("SELECT icons.* FROM icons " +
            "INNER JOIN pages ON icons.page_id = pages.page_id " +
            "WHERE pages.layout_name = :layoutName")
    LiveData<List<IconEntity>> getIconListInLayout(String layoutName);

    /**
     * Get the list of PageWithIconEntities in the layout with the given {@code layoutName}. The
     * list can be used to generate layout view.
     */
    @Query("SELECT * FROM pages WHERE layout_name = :layoutName ORDER BY page_rank ASC")
    LiveData<List<PageWithIconEntities>> getPagedIconListInLayout(String layoutName);

    @Query("SELECT * FROM icons WHERE page_id = :pageId")
    LiveData<List<IconEntity>> getIconListOnPage(long pageId);

    @Query("SELECT icons.* FROM icons " +
            "INNER JOIN pages ON icons.page_id = pages.page_id " +
            "WHERE pages.layout_name = :layoutName AND pages.page_rank = :pageRank")
    LiveData<List<IconEntity>> getIconListByPagePosition(String layoutName, int pageRank);

    @Query("SELECT COUNT(icons.id) FROM icons " +
            "INNER JOIN pages ON icons.page_id = pages.page_id " +
            "WHERE pages.layout_name = :layoutName AND pages.page_rank = :pageRank")
    Single<Integer> getIconCountByPageRank(String layoutName, int pageRank);

    @Query("SELECT icons.* FROM icons " +
            "WHERE icons.package_name = :packageName")
    Single<List<IconEntity>> getIconListByPackageName(String packageName);

    @Query("SELECT EXISTS(" +
            "SELECT 1 FROM icons " +
            "INNER JOIN pages ON icons.page_id = pages.page_id " +
            "WHERE pages.layout_name = :layoutName " +
            "AND pages.page_rank = :pageRank " +
            "AND icons.cell_x = :cellX " +
            "AND icons.cell_y = :cellY)")
    boolean isCellOccupied(String layoutName, int pageRank, int cellX, int cellY);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertIconAsync(IconEntity iconEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIconSync(IconEntity iconEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertIconArrayAsync(IconEntity... launcherIconEntities);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIconArraySync(IconEntity... launcherIconEntities);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertIconListAsync(List<IconEntity> iconEntityList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertIconListSync(List<IconEntity> iconEntityList);

    /**
     * Delete an icon from the database.
     * <br>
     * This method may not take effect because the given {@code iconEntity} may have a different
     * address in the memory with the one in the database.
     * <br>
     * This method is asynchronous.
     */
    @Delete
    Completable deleteIconAsync(IconEntity iconEntity);

    /**
     * Delete an icon from the database.
     * <br>
     * This method may not take effect because the given {@code iconEntity} may have a different
     * address in the memory with the one in the database.
     * <br>
     * This method is synchronous.
     */
    @Delete
    void deleteIconSync(IconEntity iconEntity);

    /**
     * Delete an array of icons from the database.
     * <br>
     * This method is asynchronous.
     */
    @Delete
    Completable deleteIconsAsync(IconEntity... iconEntities);

    /**
     * Delete an array of icons from the database.
     * <br>
     * This method is synchronous.
     */
    @Delete
    void deleteIconsSync(IconEntity... iconEntities);

    /**
     * Delete a list of icons from the database.
     * <p>This method is asynchronous.
     */
    @Delete
    Completable deleteIconListAsync(List<IconEntity> iconEntityList);

    /**
     * Delete a list of icons from the database.
     * <p>This method is synchronous.
     */
    @Delete
    void deleteIconListSync(List<IconEntity> iconEntityList);

    /**
     * Delete the icon specified by the given {@code layoutName}, {@code pageRank}, {@code cellX}
     * and {@code cellY}.
     * <p>This method is asynchronous.
     */
    @Query("DELETE FROM icons WHERE id IN (" +
            "SELECT icons.id FROM icons " +
            "INNER JOIN pages ON icons.page_id = pages.page_id " +
            "WHERE pages.layout_name = :layoutName " +
            "AND pages.page_rank = :pageRank " +
            "AND icons.cell_x = :cellX " +
            "AND icons.cell_y = :cellY)")
    Completable deleteIconByPositionAsync(String layoutName, int pageRank, int cellX, int cellY);

    /**
     * Delete icons on the page specified by the given {@code pageRank} and {@code layoutName}.
     * This method is asynchronous.
     */
    @Query("DELETE FROM icons WHERE page_id IN (" +
            "SELECT page_id FROM pages " +
            "WHERE layout_name = :layoutName " +
            "AND page_rank = :pageRank)")
    Completable deleteIconsOnPage(String layoutName, int pageRank);

    /**
     * Delete icons of package with the given {@code packageName}. This method is asynchronous.
     */
    @Query("DELETE FROM icons WHERE package_name = :packageName")
    Completable deleteIconsByPackage(String packageName);

    /**
     * Delete icons of activity with the given {@code packageName} and {@code activityName}. This
     * method is asynchronous.
     */
    @Query("DELETE FROM icons WHERE package_name = :packageName AND activity_name = :activityName")
    Completable deleteIconsByActivity(String packageName, String activityName);
}