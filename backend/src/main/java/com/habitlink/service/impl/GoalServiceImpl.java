package com.habitlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.habitlink.dto.GoalCreateRequest;
import com.habitlink.dto.GoalStatsResponse;
import com.habitlink.entity.CheckinRecord;
import com.habitlink.entity.Goal;
import com.habitlink.mapper.CheckinMapper;
import com.habitlink.mapper.GoalMapper;
import com.habitlink.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private static final Long CURRENT_USER_ID = 1L;
    private static final Integer DEFAULT_STATUS = 1;

    private final GoalMapper goalMapper;
    private final CheckinMapper checkinMapper;

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
        validateGoalId(goalId);

        Goal goal = goalMapper.selectById(goalId);
        if (goal == null) {
            throw new IllegalArgumentException("目标不存在");
        }
        return goal;
    }

    @Override
    public GoalStatsResponse getGoalStats(Long goalId) {
        validateGoalId(goalId);
        checkGoalAccessible(goalId);

        Long totalDays = checkinMapper.selectCount(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getUserId, CURRENT_USER_ID)
                .eq(CheckinRecord::getGoalId, goalId));

        List<CheckinRecord> records = checkinMapper.selectList(new LambdaQueryWrapper<CheckinRecord>()
                .select(CheckinRecord::getCheckinDate)
                .eq(CheckinRecord::getUserId, CURRENT_USER_ID)
                .eq(CheckinRecord::getGoalId, goalId));

        Set<LocalDate> checkinDates = records.stream()
                .map(CheckinRecord::getCheckinDate)
                .collect(Collectors.toCollection(HashSet::new));

        return new GoalStatsResponse(
                goalId,
                totalDays == null ? 0L : totalDays,
                calculateContinuousDays(checkinDates)
        );
    }

    @Override
    public void deleteGoal(Long goalId) {
        validateGoalId(goalId);
        goalMapper.deleteById(goalId);
    }

    private void validateGoalId(Long goalId) {
        if (goalId == null) {
            throw new IllegalArgumentException("目标ID不能为空");
        }
    }

    private void checkGoalAccessible(Long goalId) {
        Goal goal = goalMapper.selectById(goalId);
        if (goal == null) {
            throw new IllegalArgumentException("目标不存在");
        }
        if (!CURRENT_USER_ID.equals(goal.getUserId())) {
            throw new IllegalArgumentException("无权限访问该目标");
        }
    }

    private int calculateContinuousDays(Set<LocalDate> checkinDates) {
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        if (checkinDates.contains(today)) {
            startDate = today;
        } else if (checkinDates.contains(today.minusDays(1))) {
            startDate = today.minusDays(1);
        } else {
            return 0;
        }

        int continuousDays = 0;
        LocalDate currentDate = startDate;
        while (checkinDates.contains(currentDate)) {
            continuousDays++;
            currentDate = currentDate.minusDays(1);
        }
        return continuousDays;
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
