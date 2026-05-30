package com.habitlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.habitlink.dto.CheckinRequest;
import com.habitlink.entity.CheckinRecord;
import com.habitlink.entity.Goal;
import com.habitlink.mapper.CheckinMapper;
import com.habitlink.mapper.GoalMapper;
import com.habitlink.service.CheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckinServiceImpl implements CheckinService {

    private final CheckinMapper checkinMapper;
    private final GoalMapper goalMapper;

    @Override
    public CheckinRecord checkinToday(CheckinRequest request, Long currentUserId) {
        if (request == null || request.getGoalId() == null) {
            throw new IllegalArgumentException("目标ID不能为空");
        }

        Long goalId = request.getGoalId();
        LocalDate today = LocalDate.now();
        checkGoalBelongsToCurrentUser(goalId, currentUserId);

        if (existsCheckin(goalId, today, currentUserId)) {
            throw new IllegalArgumentException("今日已打卡");
        }

        CheckinRecord record = new CheckinRecord();
        record.setUserId(currentUserId);
        record.setGoalId(goalId);
        record.setCheckinDate(today);
        record.setContent(request.getContent());

        try {
            checkinMapper.insert(record);
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("今日已打卡");
        }

        return record;
    }

    @Override
    public List<CheckinRecord> listGoalCheckins(Long goalId, Long currentUserId) {
        validateGoalId(goalId);
        checkGoalBelongsToCurrentUser(goalId, currentUserId);

        return checkinMapper.selectList(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getUserId, currentUserId)
                .eq(CheckinRecord::getGoalId, goalId)
                .orderByDesc(CheckinRecord::getCheckinDate)
                .orderByDesc(CheckinRecord::getCreatedAt));
    }

    @Override
    public Boolean hasCheckedToday(Long goalId, Long currentUserId) {
        validateGoalId(goalId);
        checkGoalBelongsToCurrentUser(goalId, currentUserId);
        return existsCheckin(goalId, LocalDate.now(), currentUserId);
    }

    private void validateGoalId(Long goalId) {
        if (goalId == null) {
            throw new IllegalArgumentException("目标ID不能为空");
        }
    }

    private void checkGoalBelongsToCurrentUser(Long goalId, Long currentUserId) {
        Goal goal = goalMapper.selectOne(new LambdaQueryWrapper<Goal>()
                .eq(Goal::getId, goalId)
                .eq(Goal::getUserId, currentUserId));
        if (goal == null) {
            throw new IllegalArgumentException("目标不存在");
        }
    }

    private boolean existsCheckin(Long goalId, LocalDate checkinDate, Long currentUserId) {
        Long count = checkinMapper.selectCount(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getUserId, currentUserId)
                .eq(CheckinRecord::getGoalId, goalId)
                .eq(CheckinRecord::getCheckinDate, checkinDate));
        return count != null && count > 0;
    }
}
