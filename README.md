# 工厂生产管理系统 (Factory Monitor Screen)

一个面向中小型工厂的生产管理系统，包含 PC 端管理后台、生产监控大屏和移动端小程序，实现订单管理、派工报工、质量检验、工资核算等核心功能。

## 系统架构

```
factory-monitor-screen/
├── backend/          # 后端服务 (Spring Boot)
├── frontend/         # PC端前端 (Vue 3)
└── miniprogram/      # 微信小程序
```

## 技术栈

### 后端 (Backend)
| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | JDK 版本 |
| Spring Boot | 3.2.0 | 基础框架 |
| Spring Data JPA | - | ORM 框架 |
| Spring Security | - | 安全认证 |
| JWT | 0.12.3 | Token 认证 |
| MySQL | 8.0+ | 数据库 |
| WebSocket | - | 实时数据推送 |
| Quartz | - | 定时任务 |
| ZXing | 3.5.2 | 二维码生成 |
| Apache POI | 5.2.5 | Excel 导出 |

### 前端 (Frontend)
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.0 | 前端框架 |
| TypeScript | 5.3.3 | 类型支持 |
| Vite | 5.0.10 | 构建工具 |
| Element Plus | 2.4.4 | UI 组件库 |
| ECharts | 5.4.3 | 图表库 |
| Pinia | 2.1.7 | 状态管理 |
| Vue Router | 4.2.5 | 路由管理 |
| Axios | 1.6.2 | HTTP 客户端 |

### 小程序 (Miniprogram)
- 微信原生小程序开发
- 支持扫码报工、任务查看、质检提交等

## 功能模块

### PC 端管理后台

| 模块 | 功能描述 |
|------|----------|
| 监控大屏 | 实时生产数据展示、设备状态监控、产量趋势图表 |
| 订单管理 | 销售订单创建、派工单生成、订单状态跟踪 |
| 生产管理 | 工序模板配置、派工分配、生产进度监控 |
| 质量管理 | 工序质检、成品质检、不良品处理 |
| 库存管理 | 物料管理、BOM 配置、库存预警 |
| 工资管理 | 计件工资、工时统计、工资报表 |
| 基础数据 | 产品管理、工序模板、物料管理 |
| 系统管理 | 用户管理、班组管理、角色权限 |
| 报表统计 | 生产报表、质量报表、工资报表 |

### 移动端小程序

| 页面 | 功能描述 |
|------|----------|
| 登录 | 账号密码登录 |
| 任务列表 | 查看待办/进行中/已完成任务 |
| 扫码报工 | 扫描工序二维码进行报工 |
| 质检提交 | 质检员进行质量检验 |
| 个人中心 | 个人信息、工资查询 |

## 数据库设计

系统包含以下数据表：

**系统表**
- `sys_user` - 用户表
- `sys_team` - 班组表

**MES 核心表**
- `mes_product` - 产品表
- `mes_process_template` - 工序模板表
- `mes_process_step` - 工序步骤表
- `mes_sales_order` - 销售订单表
- `mes_work_order` - 派工单表
- `mes_work_order_process` - 派工单工序表
- `mes_work_report` - 报工记录表
- `mes_quality_inspection` - 质检记录表
- `mes_material` - 物料表
- `mes_material_bom` - 物料 BOM 表
- `mes_wage_record` - 工资记录表

**监控统计表**
- `production_record` - 生产记录表
- `alarm_record` - 报警记录表
- `device_status_log` - 设备状态日志表
- `daily_statistics` - 每日统计表
- `monthly_statistics` - 月度统计表

## 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+
- 微信开发者工具 (小程序开发)

## 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd factory-monitor-screen
```

### 2. 数据库配置

```bash
# 创建数据库并执行建表脚本
mysql -u root -p < backend/src/main/resources/schema.sql
```

或者让 JPA 自动创建表（application.yml 中 `ddl-auto: update`）。

### 3. 后端启动

```bash
cd backend

# 修改数据库配置
# 编辑 src/main/resources/application.yml
# 修改 spring.datasource.username 和 password

# 编译打包
mvn clean package -DskipTests

# 运行
java -jar target/monitor-screen-1.0.0.jar

# 或开发模式运行
mvn spring-boot:run
```

后端服务默认运行在 `http://localhost:8080`

### 4. 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 开发模式运行
npm run dev

# 生产构建
npm run build
```

前端开发服务默认运行在 `http://localhost:5173`

### 5. 小程序启动

1. 打开微信开发者工具
2. 导入 `miniprogram` 目录
3. 配置 AppID（或使用测试号）
4. 修改 `utils/config.js` 中的后端 API 地址

## 配置说明

### 后端配置 (application.yml)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/factory_monitor
    username: root
    password: your_password

# 信捷云平台配置（设备数据对接）
xinje:
  cloud:
    base-url: https://cloud.xinje.net
    username: your_username
    password: your_password
    production-data-point-names: HD200
    plan-data-point-names: 生产计划数

# 数据轮询间隔
polling:
  interval: 3000

# CORS 跨域配置
cors:
  allowed-origins: http://localhost:5173,http://localhost:3000
```

### 前端配置 (vite.config.ts)

```typescript
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
```

## 项目结构

### 后端结构

```
backend/src/main/java/com/factory/monitor/
├── MonitorApplication.java    # 启动类
├── config/                    # 配置类
│   ├── CorsConfig.java       # 跨域配置
│   ├── SecurityConfig.java   # 安全配置
│   └── WebSocketConfig.java  # WebSocket 配置
├── controller/                # 控制器
│   ├── AuthController.java   # 认证接口
│   ├── DashboardController.java
│   ├── OrderController.java
│   └── ...
├── entity/                    # 实体类
├── repository/                # 数据访问层
├── service/                   # 业务逻辑层
├── dto/                       # 数据传输对象
├── security/                  # 安全相关
├── task/                      # 定时任务
└── websocket/                 # WebSocket 处理
```

### 前端结构

```
frontend/src/
├── api/            # API 接口定义
├── assets/         # 静态资源
├── components/     # 公共组件
├── composables/    # 组合式函数
├── router/         # 路由配置
├── stores/         # Pinia 状态管理
├── types/          # TypeScript 类型定义
├── views/          # 页面组件
│   ├── Dashboard.vue      # 监控大屏
│   ├── Login.vue          # 登录页
│   ├── order/             # 订单管理
│   ├── production/        # 生产管理
│   ├── quality/           # 质量管理
│   ├── inventory/         # 库存管理
│   ├── wage/              # 工资管理
│   ├── basedata/          # 基础数据
│   ├── system/            # 系统管理
│   └── report/            # 报表统计
├── App.vue
└── main.ts
```

### 小程序结构

```
miniprogram/
├── pages/
│   ├── login/       # 登录页
│   ├── tasks/       # 任务列表
│   ├── operator/    # 操作工报工
│   ├── inspector/   # 质检员质检
│   └── profile/     # 个人中心
├── components/      # 公共组件
├── utils/           # 工具函数
├── app.js
├── app.json
└── app.wxss
```

## 生产部署

### Docker 部署

```dockerfile
# 后端 Dockerfile
FROM openjdk:17-jdk-slim
COPY target/monitor-screen-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```yaml
# docker-compose.yml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: factory_monitor
    volumes:
      - mysql_data:/var/lib/mysql
    ports:
      - "3306:3306"

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    depends_on:
      - mysql
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/factory_monitor

  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql_data:
```

### Nginx 配置

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # WebSocket 代理
    location /ws {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

## API 接口

### 认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/logout | 用户登出 |
| GET | /api/auth/info | 获取当前用户信息 |

### 业务接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/dashboard | 获取大屏数据 |
| GET | /api/products | 产品列表 |
| GET | /api/orders | 订单列表 |
| POST | /api/orders | 创建订单 |
| GET | /api/work-orders | 派工单列表 |
| POST | /api/work-reports | 提交报工 |
| GET | /api/statistics/daily | 每日统计 |
| GET | /api/statistics/monthly | 月度统计 |

## 默认账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | admin123 | 系统管理员 |
| 厂长 | manager | 123456 | 生产管理 |
| 班组长 | leader | 123456 | 班组管理 |
| 操作工 | operator | 123456 | 报工操作 |
| 质检员 | inspector | 123456 | 质量检验 |

## 开发指南

### 添加新功能模块

1. **后端**
   - 在 `entity/` 创建实体类
   - 在 `repository/` 创建 Repository 接口
   - 在 `service/` 实现业务逻辑
   - 在 `controller/` 暴露 API 接口

2. **前端**
   - 在 `api/` 添加接口定义
   - 在 `views/` 创建页面组件
   - 在 `router/` 配置路由

### 代码规范

- 后端遵循阿里巴巴 Java 开发规范
- 前端遵循 Vue 3 组合式 API 风格
- 使用 ESLint + Prettier 进行代码格式化

## 常见问题

**Q: 启动报错数据库连接失败？**
A: 检查 MySQL 服务是否启动，确认 application.yml 中的数据库配置正确。

**Q: 前端 API 请求 404？**
A: 检查后端服务是否启动，确认 vite.config.ts 中的代理配置正确。

**Q: 小程序登录失败？**
A: 检查 utils/config.js 中的 API 地址配置，确保能访问到后端服务。

## License

MIT License

## 联系方式

如有问题或建议，请提交 Issue。
