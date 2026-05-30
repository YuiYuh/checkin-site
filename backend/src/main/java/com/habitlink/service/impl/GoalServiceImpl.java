package com.habitlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.habitlink.dto.GoalCreateRequest;
import com.habitlink.dto.GoalListResponse;
import com.habitlink.dto.GoalStatsResponse;
import com.habitlink.entity.CheckinRecord;
import com.habitlink.entity.Goal;
import com.habitlink.entity.Team;
import com.habitlink.entity.TeamMember;
import com.habitlink.mapper.CheckinMapper;
import com.habitlink.mapper.GoalMapper;
import com.habitlink.mapper.TeamMapper;
import com.habitlink.mapper.TeamMemberMapper;
import com.habitlink.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private static final Integer DEFAULT_STATUS = 1;
    private static final String PERSONAL_GOAL = "PERSONAL";
    private static final String TEAM_GOAL = "TEAM";

    private final GoalMapper goalMapper;
    private final CheckinMapper checkinMapper;
    private final TeamMapper teamMapper;
    private final TeamMemberMapper teamMemberMapper;

    @Override
    public Goal createGoal(GoalCreateRequest request, Long currentUserId) {
        validateCreateRequest(request);

        Goal goal = new Goal();
        goal.setUserId(currentUserId);
        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());
        goal.setStartDate(request.getStartDate());
        goal.setEndDate(request.getEndDate());
        goal.setStatus(DEFAULT_STATUS);
        goalMapper.insert(goal);

        return goal;
    }

    @Override
    public List<GoalListResponse> listGoals(Long currentUserId) {
        Map<Long, GoalListResponse> merged = new LinkedHashMap<>();

        List<Goal> ownGoals = goalMapper.selectList(new LambdaQueryWrapper<Goal>()
                .eq(Goal::getUserId, currentUserId)
                .orderByDesc(Goal::getCreatedAt));

        Set<Long> teamGoalIds = selectTeamGoalIdsByGoals(ownGoals);
        ownGoals.stream()
                .filter(goal -> !teamGoalIds.contains(goal.getId()))
                .forEach(goal -> merged.put(goal.getId(), toGoalListResponse(goal, null, null, PERSONAL_GOAL)));

        List<Team> joinedTeams = listJoinedTeams(currentUserId);
        Map<Long, Team> joinedTeamByGoalId = joinedTeams.stream()
                .filter(team -> team.getGoalId() != null)
                .collect(Collectors.toMap(Team::getGoalId, Function.identity(), (left, right) -> left));
        if (joinedTeamByGoalId.isEmpty()) {
            return new ArrayList<>(merged.values());
        }

        List<Goal> joinedTeamGoals = goalMapper.selectList(new LambdaQueryWrapper<Goal>()
                .in(Goal::getId, joinedTeamByGoalId.keySet()));
        for (Goal goal : joinedTeamGoals) {
            Team team = joinedTeamByGoalId.get(goal.getId());
            merged.put(goal.getId(), toGoalListResponse(goal, team.getId(), team.getName(), TEAM_GOAL));
        }

        return new ArrayList<>(merged.values());
    }

    @Override
    public Goal getGoal(Long goalId, Long currentUserId) {
        validateGoalId(goalId);
        return getAccessibleGoal(goalId, currentUserId);
    }

    @Override
    public GoalStatsResponse getGoalStats(Long goalId, Long currentUserId) {
        validateGoalId(goalId);
        getAccessibleGoal(goalId, currentUserId);

        Long totalDays = checkinMapper.selectCount(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getUserId, currentUserId)
                .eq(CheckinRecord::getGoalId, goalId));

        List<CheckinRecord> records = checkinMapper.selectList(new LambdaQueryWrapper<CheckinRecord>()
                .select(CheckinRecord::getCheckinDate)
                .eq(CheckinRecord::getUserId, currentUserId)
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
    public void deleteGoal(Long goalId, Long currentUserId) {
        validateGoalId(goalId);
        Goal goal = goalMapper.selectById(goalId);
        if (goal == null || !currentUserId.equals(goal.getUserId())) {
            throw new IllegalArgumentException("目标不存在");
        }
        if (isTeamGoal(goalId)) {
            throw new IllegalArgumentException("小组目标不能直接删除");
        }

        checkinMapper.delete(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getGoalId, goalId));
        goalMapper.deleteById(goalId);
    }

    private GoalListResponse toGoalListResponse(Goal goal, Long teamId, String teamName, String goalType) {
        return new GoalListResponse(
                goal.getId(),
                goal.getUserId(),
                goal.getTitle(),
                goal.getDescription(),
                goal.getStartDate(),
                goal.getEndDate(),
                goal.getStatus(),
                teamId,
                teamName,
                goalType
        );
    }

    private List<Team> listJoinedTeams(Long currentUserId) {
        List<TeamMember> memberships = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getUserId, currentUserId));
        if (memberships.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> teamIds = memberships.stream()
                .map(TeamMember::getTeamId)
                .distinct()
                .toList();
        return teamMapper.selectList(new LambdaQueryWrapper<Team>()
                .in(Team::getId, teamIds));
    }

    private Set<Long> selectTeamGoalIdsByGoals(List<Goal> goals) {
        if (goals.isEmpty()) {
            return Collections.emptySet();
        }

        List<Long> goalIds = goals.stream()
                .map(Goal::getId)
                .toList();
        List<Team> teams = teamMapper.selectList(new LambdaQueryWrapper<Team>()
                .select(Team::getGoalId)
                .in(Team::getGoalId, goalIds));

        return teams.stream()
                .map(Team::getGoalId)
                .filter(goalId -> goalId != null)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private boolean isTeamGoal(Long goalId) {
        Long count = teamMapper.selectCount(new LambdaQueryWrapper<Team>()
                .eq(Team::getGoalId, goalId));
        return count != null && count > 0;
    }

    private void validateGoalId(Long goalId) {
        if (goalId == null) {
            throw new IllegalArgumentException("目标ID不能为空");
        }
    }

    private Goal getAccessibleGoal(Long goalId, Long currentUserId) {
        Goal goal = goalMapper.selectById(goalId);
        if (goal == null) {
            throw new IllegalArgumentException("目标不存在");
        }
        if (currentUserId.equals(goal.getUserId()) && !isTeamGoal(goalId)) {
            return goal;
        }
        if (canAccessTeamGoal(goalId, currentUserId)) {
            return goal;
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
