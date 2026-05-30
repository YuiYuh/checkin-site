# HabitLink

HabitLink 是一个课程演示用的目标打卡与小组协作 MVP。项目包含 Vue 3 前端、Spring Boot 后端和 MySQL 数据库脚本，当前定位是本地演示、课程答辩和内测准备，不等同于完整生产级系统。

当前登录态使用服务端签名 token，不使用 JWT。密码使用 BCrypt 哈希存储，并兼容旧明文密码在登录成功后自动升级。

## 技术栈

- 前端：Vue 3、Vite、JavaScript、Vue Router、Element Plus、Axios
- 后端：Spring Boot 3、Java 17、MyBatis Plus、Maven
- 数据库：MySQL 8.x

## 功能模块

- 用户注册、登录、获取当前用户、退出登录
- 当前登录用户的数据隔离
- 创建、查看、删除个人目标
- 今日打卡、防重复打卡
- 累计打卡天数和连续打卡天数统计
- 创建小组，并自动创建一个小组共同目标
- 邀请码加入小组
- 小组成员可以在目标列表看到小组共同目标
- 小组成员对共同目标打卡后，才算完成小组任务
- 查看我的小组、小组成员、小组成员今日共同目标完成状态
- 退出小组、转让组长
- token 失效后前端自动清理登录态并跳转登录页

当前未实现：补打卡、TodoList、番茄钟、排行榜、提醒通知、文件上传、微信登录、移动端应用、完整权限系统。

## 项目结构

```text
HabitLink/
├─ backend/              # Spring Boot 后端项目
├─ frontend/             # Vue 3 + Vite 前端项目
├─ database/             # 数据库脚本
│  ├─ init.sql
│  └─ migrations/
├─ docs/                 # 项目文档
│  ├─ api.md
│  ├─ beta-test-guide.md
│  ├─ deployment.md
│  ├─ internal-test.md
│  ├─ requirement.md
│  ├─ tasks.md
│  └─ test-cases.md
├─ presentation/         # 课程展示材料
└─ README.md
```

## 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- npm
- MySQL 8.x

## 数据库初始化

新数据库初始化：

```bash
mysql -u root -p < database/init.sql
```

已有数据库按需执行迁移脚本，建议按文件名时间顺序执行：

```bash
mysql -u root -p < database/migrations/2026-05-30-add-team-goal-id.sql
mysql -u root -p < database/migrations/2026-05-30-add-team-goal-index.sql
```

`database/init.sql` 只创建表结构，不内置测试账号。

## 后端配置

后端配置文件位于：

```text
backend/src/main/resources/application.yml
backend/src/main/resources/application-dev.yml
backend/src/main/resources/application-prod.yml
```

开发环境默认使用 `dev` profile。`application-dev.yml` 默认连接本地 MySQL：

```text
jdbc:mysql://localhost:3306/habitlink
```

开发环境可通过环境变量覆盖数据库密码和 token 密钥：

```text
MYSQL_PASSWORD
HABITLINK_TOKEN_SECRET
HABITLINK_TOKEN_EXPIRE_DAYS
```

生产环境使用 `prod` profile，必须提供：

```text
MYSQL_HOST
MYSQL_PORT
MYSQL_DATABASE
MYSQL_USERNAME
MYSQL_PASSWORD
FRONTEND_ORIGIN
HABITLINK_TOKEN_SECRET
HABITLINK_TOKEN_EXPIRE_DAYS
```

`HABITLINK_TOKEN_SECRET` 生产环境必须使用足够长的随机字符串，不要提交到 Git。

## 后端启动

本地开发：

```bash
cd backend
mvn spring-boot:run
```

后端默认地址：

```text
http://localhost:8080
```

后端测试：

```bash
cd backend
mvn test
```

生产打包：

```bash
cd backend
mvn clean package
```

生产运行示例：

```bash
java -jar target/habitlink-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## 前端配置

前端环境文件位于：

```text
frontend/.env.development
frontend/.env.production
```

开发环境：

```text
VITE_API_BASE_URL=http://localhost:8080
VITE_ENABLE_DEMO_USER=true
```

生产环境：

```text
VITE_API_BASE_URL=/api
VITE_ENABLE_DEMO_USER=false
```

前端不会解析 token，只负责保存后端返回的 token，并在请求头中携带：

```text
Authorization: Bearer <token>
```

## 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端开发地址通常为：

```text
http://localhost:5173
```

前端构建：

```bash
cd frontend
npm run build
```

构建产物位于：

```text
frontend/dist/
```

## 测试账号

通过注册接口或前端页面创建演示账号：

```json
{
  "username": "student01",
  "password": "123456",
  "nickname": "小明"
}
```

```json
{
  "username": "student02",
  "password": "123456",
  "nickname": "小红"
}
```

签名 token 启用后，前端“使用默认用户”按钮不再伪造登录态；请使用测试账号密码登录。

## 演示流程

1. 初始化数据库。
2. 启动后端服务。
3. 启动前端服务。
4. 注册并登录 `student01`。
5. 创建个人目标并完成今日打卡。
6. 查看累计打卡天数和连续打卡天数。
7. 创建小组，并填写小组共同目标。
8. 复制邀请码。
9. 注册并登录 `student02`。
10. 使用邀请码加入小组。
11. 在目标列表查看小组目标。
12. 对小组目标打卡。
13. 回到小组页面查看成员今日是否完成小组目标。
14. 演示组长转让、成员退出和空小组删除。
15. 修改 localStorage 中的 token，验证登录失效后会自动跳转登录页。

## 部署准备

详细部署说明见 [docs/deployment.md](docs/deployment.md)。

当前项目可以作为课程 MVP 演示，不建议不经加固直接面向公网开放。上线前至少需要：

- 使用生产数据库账号，不使用 root。
- 通过环境变量提供生产数据库密码和 `HABITLINK_TOKEN_SECRET`。
- 生产 CORS 只允许正式前端域名。
- 使用 HTTPS。
- 配置日志、备份和基础监控。
- 根据实际需求评估是否升级为 JWT、Session 或其他成熟认证方案。

## 内测文档

- API 文档：[docs/api.md](docs/api.md)
- 部署说明：[docs/deployment.md](docs/deployment.md)
- 内测指南：[docs/beta-test-guide.md](docs/beta-test-guide.md)
- 内测说明：[docs/internal-test.md](docs/internal-test.md)
- 测试用例：[docs/test-cases.md](docs/test-cases.md)

## 后续扩展方向

- 多目标小组或独立 `team_goal` 表。
- 更完整的小组成员管理。
- 排行榜、提醒通知和数据看板。
- 移动端或微信小程序。
- 自动化测试和部署脚本。
