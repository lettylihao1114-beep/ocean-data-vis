package com.ocean.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 通用分页视图 — 替代 MyBatis-Plus Page，可安全序列化到 Redis
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVO<T> {

    private List<T> records;
    private long total;
    private long size;
    private long current;
    private long pages;

    /**
     * 空页
     */
    public static <T> PageVO<T> empty(long pageSize) {
        PageVO<T> vo = new PageVO<>();
        vo.setRecords(Collections.emptyList());
        vo.setTotal(0);
        vo.setSize(pageSize);
        vo.setCurrent(1);
        vo.setPages(0);
        return vo;
    }

    /**
     * 从 MyBatis-Plus Page 转换为 PageVO
     */
    public static <T> PageVO<T> from(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> mpPage) {
        PageVO<T> vo = new PageVO<>();
        vo.setRecords(mpPage.getRecords());
        vo.setTotal(mpPage.getTotal());
        vo.setSize(mpPage.getSize());
        vo.setCurrent(mpPage.getCurrent());
        vo.setPages(mpPage.getPages());
        return vo;
    }
}
