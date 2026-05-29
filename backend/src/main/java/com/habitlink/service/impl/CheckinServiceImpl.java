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

    private static final Long CURRENT_USER_ID = 1L;

    private final CheckinMapper checkinMapper;
    private final GoalMapper goalMapper;

    @Override
    public CheckinRecord checkinToday(CheckinRequest request) {
        if (request == null || request.getGoalId() == null) {
            throw new IllegalArgumentException("目标ID不能为空");
        }

        Long goalId = request.getGoalId();
        LocalDate today = LocalDate.now();
        checkGoalBelongsToCurrentUser(goalId);

        if (existsCheckin(goalId, today)) {
            throw new IllegalArgumentException("今日已打卡");
        }

        CheckinRecord record = new CheckinRecord();
        record.setUserId(CURRENT_USER_ID);
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
    public List<CheckinRecord> listGoalCheckins(Long goalId) {
        validateGoalId(goalId);
        checkGoalBelongsToCurrentUser(goalId);

        return checkinMapper.selectList(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getUserId, CURRENT_USER_ID)
                .eq(CheckinRecord::getGoalId, goalId)
                .orderByDesc(CheckinRecord::getCheckinDate)
                .orderByDesc(CheckinRecord::getCreatedAt));
    }

    @Override
    public Boolean hasCheckedToday(Long goalId) {
        validateGoalId(goalId);
        checkGoalBelongsToCurrentUser(goalId);
        return existsCheckin(goalId, LocalDate.now());
    }

    private void validateGoalId(Long goalId) {
        if (goalId == null) {
            throw new IllegalArgumentException("目标ID不能为空");
        }
    }

    private void checkGoalBelongsToCurrentUser(Long goalId) {
        Goal goal = goalMapper.selectOne(new LambdaQueryWrapper<Goal>()
                .eq(Goal::getId, goalId)
                .eq(Goal::getUserId, CURRENT_USER_ID));
        if (goal == null) {
            throw new IllegalArgumentException("目标不存在");
        }
    }

    private boolean existsCheckin(Long goalId, LocalDate checkinDate) {
        Long count = checkinMapper.selectCount(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getUserId, CURRENT_USER_ID)
                .eq(CheckinRecord::getGoalId, goalId)
                .eq(CheckinRecord::getCheckinDate, checkinDate));
        return count != null && count > 0;
    }
}
