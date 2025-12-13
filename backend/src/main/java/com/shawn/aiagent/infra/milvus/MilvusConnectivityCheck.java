package com.shawn.aiagent.infra.milvus;

import io.milvus.client.MilvusClient;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MilvusConnectivityCheck {

  // 移除自定义的 milvusClient Bean，直接使用 Spring AI 自动配置的
  // @Bean
  // public MilvusClient milvusClient(...) { ... }

  @Bean
  public CommandLineRunner milvusPing(MilvusClient client,
                                      @Value("${spring.ai.vectorstore.milvus.client.host}") String host,
                                      @Value("${spring.ai.vectorstore.milvus.client.port}") int port) {
    return args -> {
      try {
        // v2.x SDK: 通过检查连接状态来验证连通性
        var healthResp = client.checkHealth();
        if (healthResp.getStatus() == 0) {
          log.info("✅ Connected to Milvus at {}:{}", host, port);
        } else {
          log.warn("⚠️ Milvus health check failed: {}", healthResp.getMessage());
        }
      } catch (Exception e) {
        log.error("❌ Failed to connect to Milvus at {}:{}; {}", host, port, e.getMessage(), e);
        // 你想让启动直接失败也可以：throw e;
      }
    };
  }
}