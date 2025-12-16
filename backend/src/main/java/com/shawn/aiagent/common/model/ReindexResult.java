package com.shawn.aiagent.common.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 重新索引结果
 */
@Data
public class ReindexResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成功加载的文档数量
     */
    private int documentCount;
    
    /**
     * 响应消息
     */
    private String message;
}

