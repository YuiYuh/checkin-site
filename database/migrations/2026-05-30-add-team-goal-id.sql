USE habitlink;

ALTER TABLE team
  ADD COLUMN IF NOT EXISTS goal_id BIGINT NULL COMMENT '小组绑定目标ID' AFTER invite_code;
