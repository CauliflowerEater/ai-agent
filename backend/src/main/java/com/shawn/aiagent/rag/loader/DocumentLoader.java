package com.shawn.aiagent.rag.loader;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 文档加载器接口
 * 定义统一的文档加载方法
 */
public interface DocumentLoader {
    
    /**
     * 加载文档列表
     * 
     * @return 文档列表
     */
    List<Document> load();
}

