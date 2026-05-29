package com.habitlink.service;

import com.habitlink.dto.GoalCreateRequest;
import com.habitlink.entity.Goal;

import java.util.List;

public interface GoalService {

    Goal createGoal(GoalCreateRequest request);

    List<Goal> listGoals();

    Goal getGoal(Long goalId);

    void deleteGoal(Long goalId);
}
