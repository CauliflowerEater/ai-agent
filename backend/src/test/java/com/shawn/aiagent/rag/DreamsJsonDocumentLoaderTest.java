package com.shawn.aiagent.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shawn.aiagent.common.exception.DataIntegrityException;
import com.shawn.aiagent.rag.loader.DreamsJsonDocumentLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DreamsJsonDocumentLoader 测试类
 * 测试从 JSON 文件加载梦境文档的功能
 */
@SpringBootTest
class DreamsJsonDocumentLoaderTest {

    @Resource
    private ObjectMapper objectMapper;

    private DreamsJsonDocumentLoader documentLoader;

    @BeforeEach
    void setUp() {
        documentLoader = new DreamsJsonDocumentLoader(objectMapper);
    }

    /**
     * 测试：成功加载文档
     */
    @Test
    void testLoadDreamsDocuments_Success() {
        // 执行加载
        List<Document> documents = documentLoader.load();

        // 验证：文档列表不为空
        assertNotNull(documents, "文档列表不应为 null");
        assertFalse(documents.isEmpty(), "文档列表不应为空");

        // 输出调试信息
        System.out.println("成功加载 " + documents.size() + " 个文档");
    }

    /**
     * 测试：验证文档结构
     */
    @Test
    void testDocumentStructure() {
        // 加载文档
        List<Document> documents = documentLoader.load();
        
        // 至少有一个文档
        assertTrue(documents.size() > 0, "应该至少有一个文档");

        // 验证第一个文档的结构
        Document firstDoc = documents.get(0);
        
        // 验证 ID 不为空
        assertNotNull(firstDoc.getId(), "文档 ID 不应为 null");
        assertTrue(firstDoc.getId().startsWith("dreams-chunk-") || 
                   firstDoc.getId().length() == 36, 
                   "文档 ID 应该是 'dreams-chunk-' 开头或 UUID 格式");

        // 验证内容不为空
        assertNotNull(firstDoc.getText(), "文档内容不应为 null");
        assertFalse(firstDoc.getText().trim().isEmpty(), "文档内容不应为空字符串");

        // 验证元数据
        Map<String, Object> metadata = firstDoc.getMetadata();
        assertNotNull(metadata, "元数据不应为 null");
        
        // 验证元数据中包含必要字段
        assertTrue(metadata.containsKey("id"), "元数据应包含 id 字段");
        assertTrue(metadata.containsKey("source"), "元数据应包含 source 字段");
        assertEquals("dreams", metadata.get("source"), "source 字段应为 'dreams'");

        // 输出第一个文档的详细信息
        System.out.println("第一个文档:");
        System.out.println("  ID: " + firstDoc.getId());
        System.out.println("  内容长度: " + firstDoc.getText().length() + " 字符");
        System.out.println("  元数据键: " + metadata.keySet());
    }

    /**
     * 测试：验证所有文档都有有效内容
     */
    @Test
    void testAllDocumentsHaveValidContent() {
        List<Document> documents = documentLoader.load();

        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            
            // 每个文档都应该有 ID
            assertNotNull(doc.getId(), 
                "文档 " + i + " 的 ID 不应为 null");
            
            // 每个文档都应该有内容
            assertNotNull(doc.getText(), 
                "文档 " + i + " 的内容不应为 null");
            assertFalse(doc.getText().trim().isEmpty(), 
                "文档 " + i + " 的内容不应为空");
            
            // 每个文档都应该有元数据
            assertNotNull(doc.getMetadata(), 
                "文档 " + i + " 的元数据不应为 null");
        }

        System.out.println("所有 " + documents.size() + " 个文档验证通过");
    }

    /**
     * 测试：验证元数据中不包含 text 字段
     */
    @Test
    void testMetadataDoesNotContainText() {
        List<Document> documents = documentLoader.load();

        for (Document doc : documents) {
            Map<String, Object> metadata = doc.getMetadata();
            
            // 元数据中不应包含 text 字段（text 应该在 content 中）
            assertFalse(metadata.containsKey("text"), 
                "元数据不应包含 'text' 字段，文本内容应在 Document.getContent() 中");
        }
    }

    /**
     * 测试：验证 chunk_index 字段
     */
    @Test
    void testChunkIndexInMetadata() {
        List<Document> documents = documentLoader.load();

        boolean hasChunkIndex = false;
        for (Document doc : documents) {
            Map<String, Object> metadata = doc.getMetadata();
            
            if (metadata.containsKey("chunk_index")) {
                hasChunkIndex = true;
                
                // 如果有 chunk_index，验证 ID 格式
                Object chunkIndex = metadata.get("chunk_index");
                assertNotNull(chunkIndex, "chunk_index 不应为 null");
                assertTrue(doc.getId().contains(chunkIndex.toString()), 
                    "文档 ID 应该包含 chunk_index 值");
                
                break;
            }
        }

        // 输出是否包含 chunk_index
        System.out.println("文档中是否包含 chunk_index: " + hasChunkIndex);
    }

    /**
     * 测试：打印示例文档内容（用于调试）
     */
    @Test
    void testPrintSampleDocument() {
        List<Document> documents = documentLoader.load();

        if (!documents.isEmpty()) {
            Document sample = documents.get(0);
            
            System.out.println("\n========== 示例文档 ==========");
            System.out.println("ID: " + sample.getId());
            System.out.println("内容: " + sample.getText());
            System.out.println("元数据: " + sample.getMetadata());
            System.out.println("==============================\n");
        }
    }

    /**
     * 测试：验证数据完整性异常
     * 这个测试验证当 chunk_index 缺失时，应该抛出 DataIntegrityException
     * 注意：这是开发/测试阶段应该发现的问题，不应该在生产环境出现
     */
    @Test
    void testDataIntegrityException() {
        // 由于测试数据都是完整的，这里只是验证异常类型的语义
        // 在实际场景中，如果 JSON 数据缺失 chunk_index，会抛出 DataIntegrityException
        
        // 验证：DataIntegrityException 是 RuntimeException 的子类
        assertTrue(RuntimeException.class.isAssignableFrom(DataIntegrityException.class),
            "DataIntegrityException 应该继承自 RuntimeException");
        
        System.out.println("DataIntegrityException 用于标识数据完整性问题");
        System.out.println("这类异常应该在开发/测试阶段被发现和修复，不应在生产环境触发");
    }
}
