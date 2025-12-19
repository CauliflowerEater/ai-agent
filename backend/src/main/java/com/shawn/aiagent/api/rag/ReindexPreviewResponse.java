package com.shawn.aiagent.api.rag;

import lombok.Data;

import java.io.Serializable;

/**
 * 重新索引预览响应DTO
 */
@Data
public class ReindexPreviewResponse implements Serializable {

    /**
     * chunk数量
     */
    private int chunkCount;

    /**
     * 表名
     */
    private String tableName;

    /**
     * embedding模型名
     */
    private String embeddingModelName;

    /**
     * embedding维度
     */
    private int embeddingDim;

    private static final long serialVersionUID = 1L;
}

