package com.qch.sumelauncher.room.relation;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.qch.sumelauncher.room.entity.IconEntity;
import com.qch.sumelauncher.room.entity.PageEntity;

import java.util.List;

public class PageWithIconEntities {
    @Embedded
    public PageEntity page;

    @Relation(
            parentColumn = "page_id",
            entityColumn = "page_id"
    )
    public List<IconEntity> list;
}