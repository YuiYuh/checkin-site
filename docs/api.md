# HabitLink API 文档

## 统一说明

- 接口前缀：`/api`
- 请求和响应默认使用 JSON。
- 后端统一返回 `code`、`message`、`data`。
- 除注册、登录外，其他业务接口都需要登录 token。

请求头：

```text
Authorization: Bearer <token>
```

token 格式由服务端生成，当前为 `userId.expireAt.signature`。前端只保存和携带 token，不解析 token。

## 统一响应

成功：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

失败：

```json
{
  "code": 500,
  "message": "请先登录",
  "data": null
}
```

常见鉴权错误：

- `请先登录`
- `登录状态无效`
- `登录已过期，请重新登录`
- `无权限访问该目标`

前端遇到前三类登录状态错误或 HTTP 401 时，会清除本地登录态并跳转登录页。

## 当前接口清单

- 用户：注册、登录、获取当前用户
- 目标：创建、列表、详情、删除、统计
- 打卡：今日打卡、目标打卡记录、今日是否打卡
- 小组：创建、邀请码加入、我的小组、成员列表、成员今日状态、退出小组、转让组长

## 用户模块

### 注册

- 方法：`POST`
- 路径：`/api/user/register`
- 说明：创建新用户，密码使用 BCrypt 哈希存储。

请求：

```json
{
  "username": "student01",
  "password": "123456",
  "nickname": "小明"
}
```

响应 data：

```json
{
  "id": 1,
  "username": "student01",
  "nickname": "小明"
}
```

### 登录

- 方法：`POST`
- 路径：`/api/user/login`
- 说明：使用用户名和密码登录。旧明文密码在登录成功后会自动升级为 BCrypt 哈希。

请求：

```json
{
  "username": "student01",
  "password": "123456"
}
```

响应 data：

```json
{
  "token": "userId.expireAt.signature",
  "user": {
    "id": 1,
    "username": "student01",
    "nickname": "小明"
  }
}
```

### 获取当前用户

- 方法：`GET`
- 路径：`/api/user/me`
- 说明：根据 Authorization 获取当前登录用户。

## 目标模块

### 创建个人目标

- 方法：`POST`
- 路径：`/api/goals`

请求：

```json
{
  "title": "每天背单词",
  "description": "每天完成 30 个单词复习",
  "startDate": "2026-05-30",
  "endDate": "2026-06-30"
}
```

### 查看目标列表

- 方法：`GET`
- 路径：`/api/goals`
- 说明：返回当前用户创建的个人目标，以及当前用户加入的小组绑定目标。

个人目标示例：

```json
{
  "id": 1,
  "userId": 1,
  "title": "每天背单词",
  "description": "每天完成 30 个单词复习",
  "startDate": "2026-05-30",
  "endDate": "2026-06-30",
  "status": 1,
  "teamId": null,
  "teamName": null,
  "goalType": "PERSONAL"
}
```

小组目标示例：

```json
{
  "id": 10,
  "userId": 1,
  "title": "每天背 20 个单词",
  "description": "小组共同目标",
  "startDate": "2026-05-30",
  "endDate": "2026-06-30",
  "status": 1,
  "teamId": 3,
  "teamName": "四级打卡小组",
  "goalType": "TEAM"
}
```

只有被 `team.goal_id` 引用的目标才是小组目标。

### 查看目标详情

- 方法：`GET`
- 路径：`/api/goals/{goalId}`
- 说明：只能查看自己有权限访问的目标。

### 删除目标

- 方法：`DELETE`
- 路径：`/api/goals/{goalId}`
- 说明：只能删除当前用户创建且没有被 `team.goal_id` 引用的个人目标。小组目标不能在目标页直接删除。

### 目标统计

- 方法：`GET`
- 路径：`/api/goals/{goalId}/stats`
- 说明：统计当前用户在该目标上的累计打卡天数和连续打卡天数。

响应 data：

```json
{
  "goalId": 1,
  "totalDays": 12,
  "continuousDays": 5
}
```

## 打卡模块

### 今日打卡

- 方法：`POST`
- 路径：`/api/checkins`
- 说明：当前用户对有权限访问的目标完成今日打卡。

请求：

```json
{
  "goalId": 1,
  "content": "今天完成 30 个单词"
}
```

规则：

- 同一用户、同一目标、同一天只能打卡一次。
- 用户不能给无权访问的目标打卡。
- 小组目标按 `team.goal_id` 判断。

### 查看目标打卡记录

- 方法：`GET`
- 路径：`/api/checkins/goal/{goalId}`
- 说明：只返回当前用户在该目标上的打卡记录。

### 今日是否打卡

- 方法：`GET`
- 路径：`/api/checkins/today/{goalId}`
- 说明：判断当前用户今天是否已对该目标打卡。

响应 data：

```json
true
```

## 小组模块

### 创建小组

- 方法：`POST`
- 路径：`/api/teams`
- 说明：创建小组时自动创建一个小组共同目标，并写入 `team.goal_id`。

请求：

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

- 方法：`POST`
- 路径：`/api/teams/join`

请求：

```json
{
  "inviteCode": "AB12CD"
}
```

加入小组只创建成员关系，不复制目标。加入后用户能在目标列表看到该小组目标。

### 查看我的小组

- 方法：`GET`
- 路径：`/api/teams/my`
- 说明：返回当前用户加入的小组，并包含绑定目标信息。

### 查看小组成员

- 方法：`GET`
- 路径：`/api/teams/{teamId}/members`

响应项：

```json
{
  "userId": 1,
  "username": "student01",
  "nickname": "小明",
  "role": "OWNER"
}
```

### 小组成员今日状态

- 方法：`GET`
- 路径：`/api/teams/{teamId}/checkins/today`
- 说明：只判断成员是否完成该小组绑定目标，不判断任意打卡。

判断条件：

```text
checkin_record.user_id = member.user_id
checkin_record.goal_id = team.goal_id
checkin_record.checkin_date = 今天
```

响应项：

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

- 方法：`POST`
- 路径：`/api/teams/{teamId}/leave`

规则：

- 普通成员退出：删除自己的 `team_member`，返回 `退出小组成功`
- 组长且小组成员数大于 1：不允许直接退出，返回 `组长不能直接退出，请先转让组长`
- 组长且最后一人退出：删除小组成员、小组、绑定目标和相关打卡记录，返回 `已退出并删除空小组`

### 转让组长

- 方法：`POST`
- 路径：`/api/teams/{teamId}/transfer-owner`

请求：

```json
{
  "newOwnerUserId": 2
}
```

规则：

- 只有当前小组 OWNER 可以转让组长。
- 新组长必须是当前小组成员。
- 不能转让给自己。
- 成功后返回 `组长转让成功`。
