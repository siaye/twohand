package com.secondhand.common.response;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> list;
    private long total;
    private int page;
    private int pageSize;
} 