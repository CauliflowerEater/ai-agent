package com.shawn.aiagent.api.rag;

import lombok.Data;

import java.io.Serializable;

/**
 * 重新索引响应DTO
 */
@Data
public class ReindexResponse implements Serializable {

    /**
     * 成功加载的文档数量
     */
    private int documentCount;

    /**
     * 响应消息
     */
    private String message;

    private static final long serialVersionUID = 1L;
}

