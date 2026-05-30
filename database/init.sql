CREATE DATABASE IF NOT EXISTS habitlink
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE habitlink;

DROP TABLE IF EXISTS team_member;
DROP TABLE IF EXISTS checkin_record;
DROP TABLE IF EXISTS goal;
DROP TABLE IF EXISTS team;
DROP TABLE IF EXISTS user;

CREATE TABLE user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  username VARCHAR(50) NOT NULL COMMENT '用户名',
  password VARCHAR(255) NOT NULL COMMENT '密码，后端存储加密后的值',
  nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  avatar VARCHAR(255) DEFAULT NULL COMMENT '头像地址，预留给微信小程序或用户资料',
  openid VARCHAR(100) DEFAULT NULL COMMENT '微信openid，预留给微信小程序登录',
  email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  phone VARCHAR(30) DEFAULT NULL COMMENT '手机号',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常，0禁用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_user_username (username),
  UNIQUE KEY uk_user_openid (openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE goal (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '目标ID',
  user_id BIGINT NOT NULL COMMENT '创建用户ID',
  title VARCHAR(100) NOT NULL COMMENT '目标标题',
  description VARCHAR(500) DEFAULT NULL COMMENT '目标描述',
  start_date DATE NOT NULL COMMENT '开始日期',
  end_date DATE DEFAULT NULL COMMENT '结束日期',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1进行中，0停止，2完成',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY idx_goal_user_id (user_id),
  CONSTRAINT fk_goal_user FOREIGN KEY (user_id) REFERENCES user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打卡目标表';

CREATE TABLE checkin_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '打卡记录ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  goal_id BIGINT NOT NULL COMMENT '目标ID',
  checkin_date DATE NOT NULL COMMENT '打卡日期',
  content VARCHAR(500) DEFAULT NULL COMMENT '打卡备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_checkin_user_goal_date (user_id, goal_id, checkin_date),
  KEY idx_checkin_goal_id (goal_id),
  KEY idx_checkin_user_date (user_id, checkin_date),
  CONSTRAINT fk_checkin_user FOREIGN KEY (user_id) REFERENCES user (id),
  CONSTRAINT fk_checkin_goal FOREIGN KEY (goal_id) REFERENCES goal (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打卡记录表';

CREATE TABLE team (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '小组ID',
  creator_id BIGINT NOT NULL COMMENT '创建者用户ID',
  name VARCHAR(100) NOT NULL COMMENT '小组名称',
  description VARCHAR(500) DEFAULT NULL COMMENT '小组描述',
  invite_code VARCHAR(20) NOT NULL COMMENT '邀请码',
  goal_id BIGINT NULL COMMENT '小组绑定目标ID',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常，0解散',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_team_invite_code (invite_code),
  KEY idx_team_creator_id (creator_id),
  CONSTRAINT fk_team_creator FOREIGN KEY (creator_id) REFERENCES user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小组表';

CREATE TABLE team_member (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '小组成员ID',
  team_id BIGINT NOT NULL COMMENT '小组ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role VARCHAR(20) NOT NULL DEFAULT 'member' COMMENT '角色：owner/member',
  joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  UNIQUE KEY uk_team_user (team_id, user_id),
  KEY idx_team_member_user_id (user_id),
  CONSTRAINT fk_team_member_team FOREIGN KEY (team_id) REFERENCES team (id),
  CONSTRAINT fk_team_member_user FOREIGN KEY (user_id) REFERENCES user (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='小组成员表';
