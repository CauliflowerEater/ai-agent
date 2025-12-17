# PostgreSQL + pgvector 向量数据库

## 概述

本项目使用 PostgreSQL + pgvector 作为向量数据库，以实现在同一系统内同时支持向量查询和 SQL 查询。

## 快速开始

### 1. 启动 PostgreSQL + pgvector

```bash
cd postgres
docker-compose up -d
```

### 2. 初始化 pgvector 扩展

连接到 PostgreSQL 并创建扩展：

```bash
docker exec -it dream-postgres psql -U dream -d dreamdb
```

在 PostgreSQL 中执行：

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

### 3. 验证连接

启动应用后，检查日志确认连接成功：

```
✅ Connected to PostgreSQL at jdbc:postgresql://localhost:5432/dreamdb
✅ pgvector extension is installed
```

## 配置说明

配置文件：`backend/src/main/resources/application.properties`

主要配置项：
- `spring.datasource.*` - PostgreSQL 数据库连接配置
- `spring.ai.vectorstore.pgvector.*` - pgvector 向量存储配置

## 数据索引

使用应用的 reindex 功能将文档数据索引到向量存储：

```bash
# 执行 dryRun 预览
curl http://localhost:8080/rag/reindex?dryRun=true

# 执行实际索引
curl http://localhost:8080/rag/reindex?dryRun=false
```

## 优势

- ✅ 统一数据源：向量和关系数据在同一数据库中
- ✅ 事务一致性：支持 ACID 事务
- ✅ SQL + 向量查询：可以在同一查询中组合使用
- ✅ 简化运维：减少一个独立服务

## 注意事项

- pgvector 适合中小规模向量数据（百万级）
- 大规模数据（千万级以上）建议评估性能需求
- HNSW 索引性能更好但占用空间更大
- IVFFLAT 索引占用空间小但查询性能略低

