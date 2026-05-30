# HabitLink

HabitLink 是一个面向大学生和学习小组的轻量级打卡平台 MVP。项目以课程小组作业为基础，围绕“目标创建、每日打卡、小组监督、记录统计”完成核心闭环，同时在用户体系、数据库字段和接口设计上为后续上线、移动端适配和微信小程序扩展预留空间。

## 技术栈

- 前端：Vue3、Vite、Element Plus、Axios
- 后端：Spring Boot、MyBatis Plus
- 数据库：MySQL
- 文档：Markdown

## 功能模块

### 用户模块

- 用户注册
- 用户登录
- 获取当前用户信息
- 预留头像 `avatar` 与微信 `openid` 扩展能力

### 目标模块

- 创建打卡目标
- 查看个人目标列表
- 查看目标详情
- 统计累计打卡天数
- 统计连续打卡天数

### 打卡模块

- 今日打卡
- 防重复打卡
- 查看打卡记录

### 小组模块

- 创建小组
- 通过邀请码加入小组
- 查看小组成员
- 查看小组成员今日是否打卡

## 项目结构

```text
HabitLink/
├── frontend/        # 前端 Vue3 项目，后续实现页面与接口调用
├── backend/         # 后端 Spring Boot 项目，后续实现业务接口
├── database/        # 数据库初始化脚本
│   └── init.sql
├── docs/            # 项目文档
│   ├── api.md
│   ├── requirement.md
│   └── tasks.md
├── presentation/    # 课程答辩材料
└── README.md
```

## 运行方式占位

### 数据库

```bash
mysql -u root -p < database/init.sql
```

### 后端

```bash
cd backend
# 后续补充 Spring Boot 启动命令
```

### 前端

```bash
cd frontend
# 后续补充 Vite 启动命令
```

## 后续计划（不属于当前 MVP）

- 当前 MVP 只实现用户、目标、打卡、小组四个核心模块
- 微信小程序登录、排行榜、提醒通知和数据看板均不属于当前 MVP
- 后续可在核心功能稳定后，再评估移动端适配、云服务器部署、HTTPS 和域名接入
## Frontend MVP

```bash
cd frontend
npm install
npm run dev
```

Vite 默认运行在 `http://localhost:5173`，后端接口地址为 `http://localhost:8080`。
## 完整运行方式 / Full Run Guide

### 数据库 / Database

```bash
mysql -u root -p < database/init.sql
```

确认 `backend/src/main/resources/application.yml` 中的 MySQL 用户名、密码、数据库名和端口与本地环境一致。

### 后端 / Backend

```bash
cd backend
mvn spring-boot:run
```

后端接口地址：`http://localhost:8080`。

### 前端 / Frontend

```bash
cd frontend
npm install
npm run dev
```

Vite 通常运行在 `http://localhost:5173`。如果端口被占用，可能会使用 `http://localhost:5174`。
