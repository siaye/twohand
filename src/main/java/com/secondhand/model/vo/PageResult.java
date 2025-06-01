package com.secondhand.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private List<T> list;
    private long total;
    private int page;
    private int pageSize;
    private int pages;

    public void setList(List<T> list) {
        this.list = list;
    }

    public void setTotal(long total) {
        this.total = total;
        // 计算总页数
        this.pages = (int) Math.ceil((double) total / pageSize);
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        // 重新计算总页数
        if (total > 0) {
            this.pages = (int) Math.ceil((double) total / pageSize);
        }
    }
} 