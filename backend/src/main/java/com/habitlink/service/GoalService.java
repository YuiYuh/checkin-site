package com.habitlink.service;

import com.habitlink.dto.GoalCreateRequest;
import com.habitlink.dto.GoalListResponse;
import com.habitlink.dto.GoalStatsResponse;
import com.habitlink.entity.Goal;

import java.util.List;

public interface GoalService {

    Goal createGoal(GoalCreateRequest request, Long currentUserId);

    List<GoalListResponse> listGoals(Long currentUserId);

    Goal getGoal(Long goalId, Long currentUserId);

    GoalStatsResponse getGoalStats(Long goalId, Long currentUserId);

    void deleteGoal(Long goalId, Long currentUserId);
}
