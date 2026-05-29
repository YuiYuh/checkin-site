package com.habitlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.habitlink.dto.GoalCreateRequest;
import com.habitlink.entity.Goal;
import com.habitlink.mapper.GoalMapper;
import com.habitlink.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private static final Long CURRENT_USER_ID = 1L;
    private static final Integer DEFAULT_STATUS = 1;

    private final GoalMapper goalMapper;

    @Override
    public Goal createGoal(GoalCreateRequest request) {
        validateCreateRequest(request);

        Goal goal = new Goal();
        goal.setUserId(CURRENT_USER_ID);
        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());
        goal.setStartDate(request.getStartDate());
        goal.setEndDate(request.getEndDate());
        goal.setStatus(DEFAULT_STATUS);
        goalMapper.insert(goal);

        return goal;
    }

    @Override
    public List<Goal> listGoals() {
        return goalMapper.selectList(new LambdaQueryWrapper<Goal>()
                .eq(Goal::getUserId, CURRENT_USER_ID)
                .orderByDesc(Goal::getCreatedAt));
    }

    @Override
    public Goal getGoal(Long goalId) {
        if (goalId == null) {
            throw new IllegalArgumentException("目标ID不能为空");
        }

        Goal goal = goalMapper.selectById(goalId);
        if (goal == null) {
            throw new IllegalArgumentException("目标不存在");
        }
        return goal;
    }

    @Override
    public void deleteGoal(Long goalId) {
        if (goalId == null) {
            throw new IllegalArgumentException("目标ID不能为空");
        }
        goalMapper.deleteById(goalId);
    }

    private void validateCreateRequest(GoalCreateRequest request) {
        if (request == null || !StringUtils.hasText(request.getTitle()) || request.getStartDate() == null) {
            throw new IllegalArgumentException("目标标题和开始日期不能为空");
        }
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
    }
}
