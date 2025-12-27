# 工厂设备监控大屏

基于信捷云平台接口的工厂设备实时监控大屏系统。

## 项目结构

```
factory-monitor-screen/
├── backend/                 # Java后端 (Spring Boot)
│   ├── src/main/java/
│   │   └── com/factory/monitor/
│   │       ├── config/      # 配置类
│   │       ├── controller/  # REST控制器
│   │       ├── model/       # 数据模型
│   │       ├── service/     # 业务服务
│   │       ├── task/        # 定时任务
│   │       └── websocket/   # WebSocket处理
│   └── src/main/resources/
│       └── application.yml  # 配置文件
│
└── frontend/                # Vue3前端
    ├── src/
    │   ├── components/      # Vue组件
    │   ├── composables/     # 组合式函数
    │   ├── types/           # TypeScript类型
    │   └── assets/          # 静态资源
    └── package.json
```

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2
- WebSocket
- FastJSON2

### 前端
- Vue 3 + TypeScript
- ECharts 5
- Vite 5
- Sass

## 快速开始

### 1. 配置后端

编辑 `backend/src/main/resources/application.yml`：

```yaml
xinje:
  cloud:
    base-url: https://cloud.xinje.net
    username: 你的用户名    # 替换为实际用户名
    password: 你的密码      # 替换为实际密码
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

或使用 IDE 直接运行 `MonitorApplication.java`

后端将在 http://localhost:8080 启动

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端将在 http://localhost:5173 启动

### 4. 访问大屏

打开浏览器访问: http://localhost:5173

## 功能特性

### 实时监控
- WebSocket 实时数据推送
- 3秒自动刷新
- 断线自动重连

### 数据展示
- 设备总数/在线/离线/报警统计
- 今日产量 vs 计划产量
- 生产完成率
- 设备效率 OEE
- 7天生产趋势图
- 设备状态饼图
- 实时数据点展示
- 报警信息列表

### 大屏特性
- 深色科技风格
- 渐变动效
- 自适应布局
- 实时时钟

## API 接口

### REST API

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/dashboard` | GET | 获取大屏完整数据 |
| `/api/projects` | GET | 获取项目列表 |
| `/api/devices?itemId=xxx` | GET | 获取设备列表 |
| `/api/data?itemId=xxx` | GET | 获取实时数据 |
| `/api/health` | GET | 健康检查 |

### WebSocket

- 连接地址: `ws://localhost:8080/ws/monitor`
- 发送 `refresh` 可主动刷新数据

## 自定义配置

### 轮询间隔

在 `application.yml` 中调整：

```yaml
polling:
  interval: 3000  # 毫秒
```

### 数据映射

如果你的设备数据点名称与默认不同，可以在 `DashboardService.java` 中修改：

```java
// 修改这些关键词来匹配你的数据点名称
int todayProduction = extractProductionCount(allDataPoints, "production", "count", "产量");
int planProduction = extractPlanCount(allDataPoints, "plan", "计划");
```

## 部署

### 打包后端

```bash
cd backend
mvn clean package
java -jar target/monitor-screen-1.0.0.jar
```

### 打包前端

```bash
cd frontend
npm run build
# 产物在 dist/ 目录
```

### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
    }

    # WebSocket代理
    location /ws {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

## 扩展功能

可根据需要添加：

1. **历史数据查询** - 添加时序数据库存储
2. **报警通知** - 集成邮件/短信/企业微信
3. **设备控制** - 使用 SetValue 接口
4. **多工厂支持** - 支持切换不同项目
5. **权限管理** - 添加用户登录验证

## 常见问题

### Q: 连接失败
A: 检查信捷云平台账号密码是否正确，网络是否通畅

### Q: 数据不更新
A: 检查 WebSocket 连接状态，查看浏览器控制台日志

### Q: 生产数据为0
A: 需要在信捷云平台配置加工数据点，并确保数据点名称包含识别关键词
