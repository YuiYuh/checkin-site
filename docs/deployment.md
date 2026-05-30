# HabitLink 部署说明

本文档用于 P1 部署准备和内测环境搭建。当前项目是课程 MVP，不包含 Docker、CI/CD、HTTPS 自动签发或生产监控脚本。

## 1. 部署组件

- MySQL 8.x
- HabitLink 后端：Spring Boot jar
- HabitLink 前端：Vite 构建后的静态文件
- 可选反向代理：Nginx

## 2. 数据库准备

新环境执行：

```bash
mysql -u root -p < database/init.sql
```

已有环境按需执行迁移，建议按文件名时间顺序执行：

```bash
mysql -u root -p < database/migrations/2026-05-30-add-team-goal-id.sql
mysql -u root -p < database/migrations/2026-05-30-add-team-goal-index.sql
```

生产环境建议使用独立数据库账号，不使用 root。

## 3. 后端生产配置

生产环境使用：

```text
backend/src/main/resources/application-prod.yml
```

必须提供环境变量：

```text
MYSQL_HOST
MYSQL_PORT
MYSQL_DATABASE
MYSQL_USERNAME
MYSQL_PASSWORD
FRONTEND_ORIGIN
HABITLINK_TOKEN_SECRET
```

可选环境变量：

```text
HABITLINK_TOKEN_EXPIRE_DAYS
```

`HABITLINK_TOKEN_SECRET` 必须是生产环境私密随机字符串，不能提交到 Git。

打包：

```bash
cd backend
mvn clean package
```

运行：

```bash
java -jar target/habitlink-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## 4. 前端生产配置

生产环境文件：

```text
frontend/.env.production
```

当前配置：

```text
VITE_API_BASE_URL=/api
VITE_ENABLE_DEMO_USER=false
```

构建：

```bash
cd frontend
npm install
npm run build
```

构建产物：

```text
frontend/dist/
```

## 5. Nginx 反向代理示例

以下配置仅作参考，需要替换域名、路径和证书配置：

```nginx
server {
    listen 80;
    server_name example.com;

    root /path/to/frontend/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

如果前端和后端不同域名部署，需要同步调整：

- 前端 `VITE_API_BASE_URL`
- 后端 `FRONTEND_ORIGIN`
- 后端 CORS 配置

## 6. 上线前检查

- 数据库已初始化或迁移完成。
- 生产数据库账号不是 root。
- `HABITLINK_TOKEN_SECRET` 已设置。
- `FRONTEND_ORIGIN` 是正式前端域名。
- 前端构建使用 `.env.production`。
- 后端运行使用 `prod` profile。
- 浏览器能访问前端页面。
- 前端能请求后端 `/api/user/login`。
- token 失效后会跳转登录页。
- 日志、备份、监控和 HTTPS 已按实际部署环境处理。

## 7. 当前不包含

- Docker 镜像和 Compose 文件
- CI/CD
- 自动 HTTPS 证书签发
- 数据库自动备份脚本
- 多实例会话同步
- 完整生产权限系统
