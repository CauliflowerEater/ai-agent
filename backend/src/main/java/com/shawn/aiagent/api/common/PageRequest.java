package com.shawn.aiagent.api.common;

import lombok.Data;

/**
 * 通用分页请求DTO
 */
@Data
public class PageRequest {

    /**
     * 当前页号（从1开始）
     */
    private int current = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认降序）
     * 可选值：ascend（升序）、descend（降序）
     */
    private String sortOrder = "descend";
}

