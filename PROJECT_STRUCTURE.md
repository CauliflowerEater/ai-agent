# AI Agent 项目结构说明

## 项目概述

项目已成功改造为 Maven 多模块结构，包含后端和前端两个子模块。

## 目录结构

```
ai-agent/
├── pom.xml                    # 父POM文件，管理所有子模块
├── backend/                   # 后端子模块
│   ├── pom.xml               # 后端模块配置
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/         # Java源代码
│   │   │   └── resources/    # 配置文件
│   │   └── test/             # 测试代码
│   ├── mvnw
│   └── mvnw.cmd
└── frontend/                  # 前端子模块
    ├── pom.xml               # 前端模块配置(集成Maven构建)
    ├── package.json          # npm依赖配置
    ├── vite.config.js        # Vite构建配置
    ├── index.html            # HTML入口
    ├── src/
    │   ├── main.jsx          # React入口
    │   ├── App.jsx           # 主组件
    │   ├── App.css           # 样式文件
    │   └── index.css
    └── public/               # 静态资源
```

## 技术栈

### 后端 (Backend)
- **框架**: Spring Boot 3.4.4
- **Java版本**: 21
- **构建工具**: Maven
- **主要依赖**:
  - Spring AI Alibaba
  - DashScope SDK
  - LangChain4j
  - Hutool
  - Knife4j (API文档)

### 前端 (Frontend)
- **框架**: React 18
- **构建工具**: Vite 5
- **包管理**: npm
- **主要依赖**:
  - React
  - React Router DOM
  - Axios

## 构建和运行

### 整体构建
```bash
# 在项目根目录执行
mvn clean install
```

### 后端开发

**方式1: 使用Maven**
```bash
cd backend
mvn spring-boot:run
```

**方式2: 使用Maven Wrapper**
```bash
cd backend
./mvnw spring-boot:run
```

后端服务默认运行在: `http://localhost:8080`

### 前端开发

**开发模式(热更新)**
```bash
cd frontend
npm install    # 首次运行需要安装依赖
npm run dev
```

前端开发服务器运行在: `http://localhost:3000`

**生产构建**
```bash
cd frontend
npm run build
```

构建产物输出到 `frontend/dist/` 目录

### 通过Maven构建前端

前端已集成 `frontend-maven-plugin`，可通过Maven自动管理Node.js和npm:

```bash
cd frontend
mvn clean install
```

这将自动:
1. 安装Node.js和npm (版本: Node v20.11.0, npm 10.2.4)
2. 执行 `npm install`
3. 执行 `npm run build`

## API代理配置

前端开发服务器已配置代理，所有 `/api` 开头的请求会自动转发到后端服务:

```javascript
// vite.config.js
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
    rewrite: (path) => path.replace(/^\/api/, '')
  }
}
```

## 开发建议

1. **并行开发**: 可以同时启动后端服务和前端开发服务器进行联调
2. **端口配置**: 
   - 后端: 8080 (可在 `backend/src/main/resources/application.properties` 修改)
   - 前端: 3000 (可在 `frontend/vite.config.js` 修改)
3. **热更新**: 前端支持热更新，代码修改后自动刷新
4. **API测试**: 访问 `http://localhost:8080/doc.html` 查看后端API文档(Knife4j)

## 部署

### 开发环境
1. 启动后端: `cd backend && mvn spring-boot:run`
2. 启动前端: `cd frontend && npm run dev`

### 生产环境
1. 构建后端: `cd backend && mvn clean package`
2. 构建前端: `cd frontend && npm run build`
3. 将前端构建产物部署到Web服务器或集成到后端静态资源目录

## 注意事项

- 首次构建前端时，Maven会下载Node.js和npm，可能需要较长时间
- 前端的 `node_modules/` 和 `dist/` 目录已添加到 `.gitignore`
- 建议使用IDE的多模块项目功能同时管理前后端代码
