package com.shawn.aiagent.api.rag;

import lombok.Data;

import java.io.Serializable;

/**
 * 重新索引请求DTO
 */
@Data
public class ReindexRequest implements Serializable {

    /**
     * 是否执行dryRun预览（默认为true）
     */
    private boolean dryRun = true;

    private static final long serialVersionUID = 1L;
}

