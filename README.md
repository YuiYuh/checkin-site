# HabitLink

HabitLink 是一个课程演示用的目标打卡与小组协作 MVP。项目包含 Vue 3 前端、Spring Boot 后端和 MySQL 初始化脚本，当前定位是本地演示与部署准备，不代表已经完成生产级上线能力。

当前登录态是 MVP 简化实现，token 格式为 `user-{id}`，不包含 JWT、密码加密、复杂权限和完整线上安全体系。

## 技术栈

- 前端：Vue 3、Vite、JavaScript、Vue Router、Element Plus、Axios
- 后端：Spring Boot 3、MyBatis Plus、Maven
- 数据库：MySQL

## 功能模块

### 用户

- 用户注册
- 用户登录
- 获取当前登录用户
- 前端保存登录态和退出登录
- 前端路由保护

### 目标与打卡

- 创建个人目标
- 查看目标列表
- 删除个人目标
- 今日打卡
- 防止同一目标当天重复打卡
- 查看目标今日是否已打卡
- 查看累计打卡天数和连续打卡天数
- 目标和打卡数据按当前登录用户隔离

### 小组

- 创建小组
- 创建小组时自动创建一个小组共同目标
- 小组绑定一个共同目标，字段为 `team.goal_id`
- 通过邀请码加入小组
- 加入小组后，成员可以在目标列表看到该小组共同目标
- 小组成员对该共同目标打卡，才算完成小组任务
- 查看我的小组
- 查看小组成员
- 查看成员今日是否完成小组目标
- 退出小组
- 组长转让
- 组长作为最后一名成员退出时删除空小组

## 项目结构

```text
HabitLink/
├── backend/              # Spring Boot 后端项目
├── frontend/             # Vue 3 + Vite 前端项目
├── database/             # 数据库脚本
│   ├── init.sql
│   └── migrations/
├── docs/                 # 项目文档
│   ├── api.md
│   ├── requirement.md
│   └── tasks.md
├── presentation/         # 课程展示材料
└── README.md
```

## 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- npm
- MySQL 8.x

## 数据库初始化

初始化新数据库：

```bash
mysql -u root -p < database/init.sql
```

如果是已有数据库，需要补充小组绑定目标字段：

```bash
mysql -u root -p < database/migrations/2026-05-30-add-team-goal-id.sql
```

后端数据库连接配置位于：

```text
backend/src/main/resources/application-dev.yml
backend/src/main/resources/application-prod.yml
```

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

`database/init.sql` 只创建表结构，不内置测试账号。

可以通过前端注册页面或接口创建演示账号，例如：

```json
{
  "username": "student01",
  "password": "123456",
  "nickname": "小明"
}
```

再创建第二个账号用于小组演示：

```json
{
  "username": "student02",
  "password": "123456",
  "nickname": "小红"
}
```

开发环境可以使用前端“使用默认用户”按钮。该按钮会写入 `token=user-1` 和默认用户信息，使用前需要数据库中存在 `id=1` 的用户。

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
13. 回到小组页面，查看成员今日是否完成小组目标。
14. 演示组长转让、成员退出和空小组删除。

## 生产环境注意事项

当前项目还不是生产级系统。部署前至少需要处理：

- 使用真实认证方案替换 MVP token，例如 JWT 或 Session。
- 对密码进行加密存储。
- 使用生产数据库账号，不使用 root。
- 生产数据库密码通过环境变量提供，不写入 Git。
- 前端 API 地址通过环境变量配置。
- CORS 只允许正式前端域名。
- 配置 HTTPS、日志、监控和备份。

## 后续扩展方向

- 多目标小组或独立 `team_goal` 表。
- 更完整的小组成员管理。
- 排行榜、提醒通知和数据看板。
- 移动端或微信小程序。
- 自动化测试和部署脚本。
