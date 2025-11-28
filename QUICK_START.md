# 快速开始指南

## 环境要求

- **Java**: JDK 21
- **Maven**: 3.6+ 
- **Node.js**: 20.x (可选,Maven会自动下载)
- **npm**: 10.x (可选,Maven会自动下载)

## 一、环境配置

### 设置Java 21环境

由于系统中可能有多个Java版本,建议在运行前设置Java 21:

```bash
# 方式1: 使用提供的脚本(推荐)
source setup-env.sh

# 方式2: 手动设置
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH=$JAVA_HOME/bin:$PATH
```

验证Java版本:
```bash
java -version
# 应该显示: openjdk version "21.x.x"
```

## 二、项目构建

### 构建整个项目

```bash
# 在项目根目录
source setup-env.sh  # 首先设置Java环境
mvn clean install
```

这会构建后端和前端两个模块。

### 仅构建后端

```bash
cd backend
source ../setup-env.sh  # 设置Java环境
mvn clean install
```

### 仅构建前端

```bash
cd frontend

# 方式1: 通过Maven构建(会自动下载Node.js和npm)
mvn clean install

# 方式2: 直接使用npm
npm install
npm run build
```

## 三、运行项目

### 启动后端服务

```bash
cd backend
source ../setup-env.sh  # 设置Java环境

# 方式1: 使用Maven
mvn spring-boot:run

# 方式2: 使用Maven Wrapper
./mvnw spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

访问API文档: `http://localhost:8080/doc.html`

### 启动前端开发服务器

```bash
cd frontend

# 首次运行需要安装依赖
npm install

# 启动开发服务器(支持热更新)
npm run dev
```

前端服务将在 `http://localhost:3000` 启动

### 同时运行前后端(推荐)

**终端1 - 后端:**
```bash
cd backend
source ../setup-env.sh
mvn spring-boot:run
```

**终端2 - 前端:**
```bash
cd frontend
npm install
npm run dev
```

然后访问 `http://localhost:3000` 即可使用完整应用。

## 四、开发工作流

1. **修改后端代码**: 
   - 修改Java文件后,重启后端服务
   - 或使用Spring DevTools实现热重载

2. **修改前端代码**:
   - Vite会自动检测变化并热更新
   - 保存文件后浏览器自动刷新

3. **测试前后端联调**:
   - 前端的`/api`请求会自动代理到后端`http://localhost:8080`
   - 可以在浏览器开发者工具中查看网络请求

## 五、常见问题

### 1. Maven编译失败,提示Java版本错误

**问题**: `Fatal error compiling: java.lang.ExceptionInInitializerError`

**解决**: 
```bash
# 确保使用Java 21
source setup-env.sh
java -version  # 验证版本
mvn clean compile
```

### 2. 前端启动失败

**问题**: 缺少依赖或构建失败

**解决**:
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm run dev
```

### 3. 端口已被占用

**后端端口冲突(8080)**:
修改 `backend/src/main/resources/application.properties`:
```properties
server.port=8081
```

**前端端口冲突(3000)**:
修改 `frontend/vite.config.js`:
```javascript
server: {
  port: 3001,  // 改为其他端口
  // ...
}
```

### 4. 前端无法访问后端API

**检查事项**:
1. 后端服务是否正常启动(访问 http://localhost:8080/health)
2. 前端代理配置是否正确(查看 `frontend/vite.config.js`)
3. 查看浏览器控制台和网络请求

## 六、生产部署

### 构建生产版本

```bash
# 后端
cd backend
source ../setup-env.sh
mvn clean package
# JAR文件位于: backend/target/ai-agent-backend-0.0.1-SNAPSHOT.jar

# 前端
cd frontend
npm run build
# 构建产物位于: frontend/dist/
```

### 运行生产版本

```bash
# 后端
java -jar backend/target/ai-agent-backend-0.0.1-SNAPSHOT.jar

# 前端
# 将 frontend/dist/ 目录部署到Nginx/Apache等Web服务器
# 或集成到后端的static资源目录
```

## 七、项目结构快速参考

```
ai-agent/
├── setup-env.sh              # Java环境设置脚本
├── pom.xml                   # 父POM
├── backend/                  # 后端模块
│   ├── src/main/java/        # Java源码
│   ├── src/main/resources/   # 配置文件
│   └── pom.xml
└── frontend/                 # 前端模块
    ├── src/                  # React源码
    ├── package.json          # npm配置
    ├── vite.config.js        # Vite配置
    └── pom.xml               # Maven配置
```

## 八、下一步

- 查看 `PROJECT_STRUCTURE.md` 了解详细的项目结构
- 访问后端API文档进行接口测试
- 开始开发您的业务功能

祝开发顺利! 🚀
