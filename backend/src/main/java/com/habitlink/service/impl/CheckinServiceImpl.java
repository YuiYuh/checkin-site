package com.habitlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.habitlink.dto.CheckinRequest;
import com.habitlink.entity.CheckinRecord;
import com.habitlink.entity.Goal;
import com.habitlink.entity.Team;
import com.habitlink.entity.TeamMember;
import com.habitlink.mapper.CheckinMapper;
import com.habitlink.mapper.GoalMapper;
import com.habitlink.mapper.TeamMapper;
import com.habitlink.mapper.TeamMemberMapper;
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
    private final TeamMapper teamMapper;
    private final TeamMemberMapper teamMemberMapper;

    @Override
    public CheckinRecord checkinToday(CheckinRequest request, Long currentUserId) {
        if (request == null || request.getGoalId() == null) {
            throw new IllegalArgumentException("目标ID不能为空");
        }

        Long goalId = request.getGoalId();
        LocalDate today = LocalDate.now();
        checkGoalAccessible(goalId, currentUserId);

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
        checkGoalAccessible(goalId, currentUserId);

        return checkinMapper.selectList(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getUserId, currentUserId)
                .eq(CheckinRecord::getGoalId, goalId)
                .orderByDesc(CheckinRecord::getCheckinDate)
                .orderByDesc(CheckinRecord::getCreatedAt));
    }

    @Override
    public Boolean hasCheckedToday(Long goalId, Long currentUserId) {
        validateGoalId(goalId);
        checkGoalAccessible(goalId, currentUserId);
        return existsCheckin(goalId, LocalDate.now(), currentUserId);
    }

    @Override
    public void cancelTodayCheckin(Long goalId, Long currentUserId) {
        validateGoalId(goalId);
        checkGoalAccessible(goalId, currentUserId);

        LocalDate today = LocalDate.now();
        if (!existsCheckin(goalId, today, currentUserId)) {
            throw new IllegalArgumentException("今日尚未打卡");
        }

        checkinMapper.delete(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getUserId, currentUserId)
                .eq(CheckinRecord::getGoalId, goalId)
                .eq(CheckinRecord::getCheckinDate, today));
    }

    private void validateGoalId(Long goalId) {
        if (goalId == null) {
            throw new IllegalArgumentException("目标ID不能为空");
        }
    }

    private void checkGoalAccessible(Long goalId, Long currentUserId) {
        Goal goal = goalMapper.selectById(goalId);
        if (goal == null) {
            throw new IllegalArgumentException("目标不存在");
        }
        if (currentUserId.equals(goal.getUserId()) || canAccessTeamGoal(goalId, currentUserId)) {
            return;
        }
        throw new IllegalArgumentException("无权限访问该目标");
    }

    private boolean canAccessTeamGoal(Long goalId, Long currentUserId) {
        List<Team> teams = teamMapper.selectList(new LambdaQueryWrapper<Team>()
                .select(Team::getId)
                .eq(Team::getGoalId, goalId));
        if (teams.isEmpty()) {
            return false;
        }

        List<Long> teamIds = teams.stream()
                .map(Team::getId)
                .toList();
        Long count = teamMemberMapper.selectCount(new LambdaQueryWrapper<TeamMember>()
                .in(TeamMember::getTeamId, teamIds)
                .eq(TeamMember::getUserId, currentUserId));
        return count != null && count > 0;
    }

    private boolean existsCheckin(Long goalId, LocalDate checkinDate, Long currentUserId) {
        Long count = checkinMapper.selectCount(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getUserId, currentUserId)
                .eq(CheckinRecord::getGoalId, goalId)
                .eq(CheckinRecord::getCheckinDate, checkinDate));
        return count != null && count > 0;
    }
}
