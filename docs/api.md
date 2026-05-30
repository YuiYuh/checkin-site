# HabitLink API 文档

## 统一说明

- 接口前缀：`/api`
- 数据格式：请求与响应默认使用 JSON
- 登录态：MVP 阶段使用 Token，客户端在需要登录的接口中通过请求头传递

```text
Authorization: Bearer <token>
```

## 统一响应格式

所有接口统一返回 `code`、`message`、`data`。

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

## 用户模块

### 用户注册

- URL：`POST /api/user/register`
- 说明：创建新用户

请求参数：

```json
{
  "username": "student01",
  "password": "123456",
  "nickname": "小明"
}
```

响应数据：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "student01",
    "nickname": "小明"
  }
}
```

### 用户登录

- URL：`POST /api/user/login`
- 说明：使用用户名和密码登录

请求参数：

```json
{
  "username": "student01",
  "password": "123456"
}
```

响应数据：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "mock-token",
    "user": {
      "id": 1,
      "username": "student01",
      "nickname": "小明"
    }
  }
}
```

### 获取当前用户信息

- URL：`GET /api/user/me`
- 说明：获取当前登录用户的基本信息

响应数据：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "student01",
    "nickname": "小明"
  }
}
```

## 目标模块

> Day 3 仅实现目标基础接口：创建、列表、详情、删除。MVP 阶段当前用户临时固定为 `userId=1`。

### 创建目标

- URL：`POST /api/goals`
- 说明：创建个人打卡目标

请求参数：

```json
{
  "title": "每天背单词",
  "description": "每天完成 30 个单词复习",
  "startDate": "2026-05-29",
  "endDate": "2026-06-29"
}
```

响应数据：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "每天背单词",
    "description": "每天完成 30 个单词复习",
    "startDate": "2026-05-29",
    "endDate": "2026-06-29"
  }
}
```

### 查看目标列表

- URL：`GET /api/goals`
- 说明：查看当前用户创建的目标列表

响应数据：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "title": "每天背单词",
      "description": "每天完成 30 个单词复习",
      "startDate": "2026-05-29",
      "endDate": "2026-06-29"
    }
  ]
}
```

### 查看目标详情

- URL：`GET /api/goals/{goalId}`
- 说明：查看单个目标详情

响应数据：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "每天背单词",
    "description": "每天完成 30 个单词复习",
    "startDate": "2026-05-29",
    "endDate": "2026-06-29"
  }
}
```

### 删除目标

- URL：`DELETE /api/goals/{goalId}`
- 说明：根据目标 ID 删除目标

响应数据：
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 查看目标统计

- URL：`GET /api/goals/{goalId}/stats`
- 说明：统计累计打卡天数和连续打卡天数。MVP 阶段当前用户临时固定为 `userId=1`。

响应数据：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "goalId": 1,
    "totalDays": 12,
    "continuousDays": 5
  }
}
```

## 打卡模块

### 今日打卡

- URL：`POST /api/checkins`
- 说明：为指定目标完成今日打卡

请求参数：

```json
{
  "goalId": 1,
  "content": "今天完成 30 个单词"
}
```

响应数据：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "goalId": 1,
    "checkinDate": "2026-05-29",
    "content": "今天完成 30 个单词"
  }
}
```

### 查看目标打卡记录

- URL：`GET /api/checkins/goal/{goalId}`
- 说明：查看指定目标的打卡记录

响应数据：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "goalId": 1,
      "checkinDate": "2026-05-29",
      "content": "今天完成 30 个单词"
    }
  ]
}
```

### 查看目标今日打卡状态

- URL：`GET /api/checkins/today/{goalId}`
- 说明：查看指定目标今天是否已打卡

响应数据：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "goalId": 1,
    "checkedToday": true,
    "checkin": {
      "id": 1,
      "checkinDate": "2026-05-29",
      "content": "今天完成 30 个单词"
    }
  }
}
```

## 小组模块

### 创建小组

- URL：`POST /api/teams`
- 说明：创建学习小组，并生成邀请码

请求参数：

```json
{
  "name": "软工学习小组",
  "description": "一起完成课程学习和每日打卡"
}
```

响应数据：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "软工学习小组",
    "description": "一起完成课程学习和每日打卡",
    "inviteCode": "AB12CD"
  }
}
```

### 邀请码加入小组

- URL：`POST /api/teams/join`
- 说明：通过邀请码加入小组

请求参数：

```json
{
  "inviteCode": "AB12CD"
}
```

响应数据：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "creatorId": 1,
    "name": "软工学习小组",
    "description": "一起完成课程学习和每日打卡",
    "inviteCode": "AB12CD",
    "status": 1
  }
}
```

### 查看我的小组

- URL：`GET /api/teams/my`
- 说明：查看当前用户加入或创建的小组

响应数据：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "软工学习小组",
      "description": "一起完成课程学习和每日打卡",
      "inviteCode": "AB12CD"
    }
  ]
}
```

### 查看小组成员

- URL：`GET /api/teams/{teamId}/members`
- 说明：查看小组成员列表

响应数据：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "userId": 1,
      "username": "student01",
      "nickname": "小明",
      "role": "OWNER"
    }
  ]
}
```

### 查看小组成员今日打卡状态

- URL：`GET /api/teams/{teamId}/checkins/today`
- 说明：查看小组成员今天是否完成打卡。MVP 阶段判断成员今天是否有任意 `checkin_record`。

响应数据：

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "userId": 1,
      "nickname": "小明",
      "checkedToday": true
    },
    {
      "userId": 2,
      "nickname": "小红",
      "checkedToday": false
    }
  ]
}
```
## Day 9 小组管理接口补充

### 退出小组

- URL: `POST /api/teams/{teamId}/leave`
- 说明: MVP 阶段当前用户固定为 `userId=1`。

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": "退出小组成功"
}
```

其他成功信息：

- 普通成员退出：`退出小组成功`
- 组长且最后一人退出：`已退出并删除空小组`

失败信息：

- `小组不存在`
- `未加入该小组`
- `组长不能直接退出，请先转让组长`

### 转让组长

- URL: `POST /api/teams/{teamId}/transfer-owner`
- 说明: 只有当前小组 OWNER 可以转让组长。MVP 阶段当前用户固定为 `userId=1`。

请求体：

```json
{
  "newOwnerUserId": 2
}
```

成功响应：

```json
{
  "code": 200,
  "message": "success",
  "data": "组长转让成功"
}
```

失败信息：

- `小组不存在`
- `未加入该小组`
- `只有组长可以转让组长`
- `新组长必须是小组成员`
- `不能转让给自己`
