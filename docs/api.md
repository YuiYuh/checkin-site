# HabitLink API 文档

## 统一说明

- 接口前缀：`/api`
- 请求和响应默认使用 JSON。
- 登录后后端返回签名 token，前端在需要登录的接口中携带：

```text
Authorization: Bearer <token>
```

token 由服务端签名并校验，前端不需要解析。

## 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

业务失败时 `code` 为非 200，`message` 返回具体错误，例如 `请先登录`、`无权限访问该目标`。

## 用户模块

### 用户注册

- URL：`POST /api/user/register`
- 说明：创建新用户，密码使用 BCrypt 哈希存储。

```json
{
  "username": "student01",
  "password": "123456",
  "nickname": "小明"
}
```

### 用户登录

- URL：`POST /api/user/login`
- 说明：使用用户名和密码登录。旧的明文密码在登录成功后会自动升级为 BCrypt 哈希。

```json
{
  "username": "student01",
  "password": "123456"
}
```

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "userId.expireAt.signature",
    "user": {
      "id": 1,
      "username": "student01",
      "nickname": "小明"
    }
  }
}
```

### 当前用户

- URL：`GET /api/user/me`
- 说明：根据 Authorization 获取当前登录用户。

## 目标模块

### 创建个人目标

- URL：`POST /api/goals`

```json
{
  "title": "每天背单词",
  "description": "每天完成 30 个单词复习",
  "startDate": "2026-05-30",
  "endDate": "2026-06-30"
}
```

### 查看目标列表

- URL：`GET /api/goals`
- 说明：返回当前用户创建的个人目标，以及当前用户加入的小组绑定目标。

小组目标会包含：

```json
{
  "id": 10,
  "title": "每天背 20 个单词",
  "description": "小组共同目标",
  "startDate": "2026-05-30",
  "endDate": "2026-06-30",
  "teamId": 3,
  "teamName": "四级打卡小组",
  "goalType": "TEAM"
}
```

个人目标的 `goalType` 为 `PERSONAL`，`teamId` 和 `teamName` 为空。

### 查看目标详情

- URL：`GET /api/goals/{goalId}`
- 说明：只能查看自己有权限访问的目标。

### 删除目标

- URL：`DELETE /api/goals/{goalId}`
- 说明：只能删除当前用户创建的个人目标。小组目标不能直接删除。

### 目标统计

- URL：`GET /api/goals/{goalId}/stats`
- 说明：统计当前用户在该目标上的累计打卡天数和连续打卡天数。

```json
{
  "goalId": 1,
  "totalDays": 12,
  "continuousDays": 5
}
```

## 打卡模块

### 今日打卡

- URL：`POST /api/checkins`
- 说明：当前用户对有权限访问的目标完成今日打卡。

```json
{
  "goalId": 1,
  "content": "今天完成 30 个单词"
}
```

### 查看目标打卡记录

- URL：`GET /api/checkins/goal/{goalId}`
- 说明：只返回当前用户在该目标上的打卡记录。

### 今日是否打卡

- URL：`GET /api/checkins/today/{goalId}`
- 说明：判断当前用户今天是否已对该目标打卡。

## 小组模块

### 创建小组

- URL：`POST /api/teams`
- 说明：创建小组时自动创建一个小组共同目标，并写入 `team.goal_id`。

```json
{
  "name": "四级打卡小组",
  "description": "一起完成每日英语学习",
  "goalTitle": "每天背 20 个单词",
  "goalDescription": "小组共同目标",
  "startDate": "2026-05-30",
  "endDate": "2026-06-30"
}
```

字段默认值：

- `goalTitle` 为空时使用 `name`
- `goalDescription` 为空时使用 `description`
- `startDate` 为空时使用当天日期
- `endDate` 可以为空

### 邀请码加入小组

- URL：`POST /api/teams/join`

```json
{
  "inviteCode": "AB12CD"
}
```

加入小组只创建成员关系，不复制目标。加入后用户能在目标列表看到该小组目标。

### 查看我的小组

- URL：`GET /api/teams/my`
- 说明：返回当前用户加入的小组，并包含绑定目标信息。

### 查看小组成员

- URL：`GET /api/teams/{teamId}/members`

### 小组成员今日状态

- URL：`GET /api/teams/{teamId}/checkins/today`
- 说明：只判断成员是否完成该小组绑定目标，不判断任意打卡。

判断条件：

```text
checkin_record.user_id = member.user_id
checkin_record.goal_id = team.goal_id
checkin_record.checkin_date = 今天
```

响应项包含：

```json
{
  "userId": 1,
  "nickname": "小明",
  "checkedToday": true,
  "goalId": 10,
  "goalTitle": "每天背 20 个单词"
}
```

### 退出小组

- URL：`POST /api/teams/{teamId}/leave`

规则：

- 普通成员退出：删除自己的 `team_member`，返回 `退出小组成功`
- 组长且小组成员数大于 1：不允许直接退出，返回 `组长不能直接退出，请先转让组长`
- 组长且最后一人退出：删除小组成员、小组、绑定目标和相关打卡记录，返回 `已退出并删除空小组`

### 转让组长

- URL：`POST /api/teams/{teamId}/transfer-owner`

```json
{
  "newOwnerUserId": 2
}
```

规则：

- 只有当前小组 OWNER 可以转让组长
- 新组长必须是当前小组成员
- 不能转让给自己
- 成功后返回 `组长转让成功`
