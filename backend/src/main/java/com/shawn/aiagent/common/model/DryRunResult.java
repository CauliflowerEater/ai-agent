package com.shawn.aiagent.common.model;

import lombok.Data;

import java.io.Serializable;

/**
 * dryRun 预览结果
 */
@Data
public class DryRunResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * chunk 数量
     */
    private int chunkCount;
    
    /**
     * 预计 collection 名
     */
    private String collectionName;
    
    /**
     * 当前配置的 embedding 模型名
     */
    private String embeddingModelName;
    
    /**
     * embedding 维度
     */
    private int embeddingDim;
}

