package com.shawn.aiagent.infra.postgres;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Slf4j
@Configuration
public class PostgresConnectivityCheck {

    @Bean
    public CommandLineRunner postgresPing(DataSource dataSource,
                                         @Value("${spring.datasource.url}") String url) {
        return args -> {
            try (Connection connection = dataSource.getConnection()) {
                // 检查 PostgreSQL 连接
                boolean isValid = connection.isValid(5);
                if (isValid) {
                    log.info("✅ Connected to PostgreSQL at {}", url);
                    
                    // 检查 pgvector 扩展是否已安装
                    try (Statement stmt = connection.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT * FROM pg_extension WHERE extname = 'vector'")) {
                        if (rs.next()) {
                            log.info("✅ pgvector extension is installed");
                        } else {
                            log.warn("⚠️ pgvector extension is not installed. Please run: CREATE EXTENSION IF NOT EXISTS vector;");
                        }
                    }
                } else {
                    log.warn("⚠️ PostgreSQL connection validation failed");
                }
            } catch (Exception e) {
                log.error("❌ Failed to connect to PostgreSQL at {}; {}", url, e.getMessage(), e);
                // 你可以选择让启动直接失败：throw e;
            }
        };
    }
}

