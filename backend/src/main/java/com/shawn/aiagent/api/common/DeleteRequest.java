package com.shawn.aiagent.api.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用删除请求DTO
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * 要删除的实体ID
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}

