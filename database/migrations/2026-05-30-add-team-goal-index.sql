USE habitlink;

SET @index_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'team'
    AND index_name = 'idx_team_goal_id'
);

SET @sql := IF(
  @index_exists = 0,
  'CREATE INDEX idx_team_goal_id ON team(goal_id)',
  'SELECT ''idx_team_goal_id already exists'''
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
