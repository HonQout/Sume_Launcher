package com.qch.sumelauncher.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "pages",
        indices = {
                @Index(value = {"layout_name", "page_rank"}),
                @Index(value = {"layout_name"})
        },
        foreignKeys = @ForeignKey(
                entity = LayoutEntity.class,
                parentColumns = "name",
                childColumns = "layout_name",
                onDelete = ForeignKey.CASCADE,
                onUpdate = ForeignKey.CASCADE
        )
)
public class PageEntity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "page_id")
    private long pageId;

    @ColumnInfo(name = "layout_name")
    private String layoutName;

    @ColumnInfo(name = "page_rank")
    private int pageRank;

    public PageEntity(String layoutName, int pageRank) {
        this.layoutName = layoutName;
        this.pageRank = pageRank;
    }

    public void setPageId(long pageId) {
        this.pageId = pageId;
    }

    public long getPageId() {
        return pageId;
    }

    public void setLayoutName(String layoutName) {
        this.layoutName = layoutName;
    }

    public String getLayoutName() {
        return layoutName;
    }

    public void setPageRank(int pageRank) {
        this.pageRank = pageRank;
    }

    public int getPageRank() {
        return pageRank;
    }
}