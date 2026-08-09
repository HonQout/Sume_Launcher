package com.qch.sumelauncher.data.model.launcher;

import com.qch.sumelauncher.room.entity.PageEntity;

import java.util.List;

public class PageWithIconModels {
    private PageEntity page;
    private List<IconModel> list;

    public PageWithIconModels(PageEntity page, List<IconModel> list) {
        this.page = page;
        this.list = list;
    }

    public void setPage(PageEntity page) {
        this.page = page;
    }

    public PageEntity getPage() {
        return page;
    }

    public void setList(List<IconModel> list) {
        this.list = list;
    }

    public List<IconModel> getList() {
        return list;
    }
}