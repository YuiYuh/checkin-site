package com.habitlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.habitlink.dto.TeamCheckinTodayResponse;
import com.habitlink.dto.TeamCreateRequest;
import com.habitlink.dto.TeamJoinRequest;
import com.habitlink.dto.TeamListResponse;
import com.habitlink.dto.TeamMemberResponse;
import com.habitlink.dto.TeamTransferOwnerRequest;
import com.habitlink.entity.CheckinRecord;
import com.habitlink.entity.Goal;
import com.habitlink.entity.Team;
import com.habitlink.entity.TeamMember;
import com.habitlink.entity.User;
import com.habitlink.mapper.CheckinMapper;
import com.habitlink.mapper.GoalMapper;
import com.habitlink.mapper.TeamMapper;
import com.habitlink.mapper.TeamMemberMapper;
import com.habitlink.mapper.UserMapper;
import com.habitlink.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private static final Integer DEFAULT_STATUS = 1;
    private static final String OWNER_ROLE = "OWNER";
    private static final String MEMBER_ROLE = "MEMBER";
    private static final int INVITE_CODE_LENGTH = 6;
    private static final String INVITE_CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TeamMapper teamMapper;
    private final TeamMemberMapper teamMemberMapper;
    private final UserMapper userMapper;
    private final CheckinMapper checkinMapper;
    private final GoalMapper goalMapper;

    @Override
    @Transactional
    public TeamListResponse createTeam(TeamCreateRequest request, Long currentUserId) {
        if (request == null || !StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("小组名称不能为空");
        }

        Goal goal = new Goal();
        goal.setUserId(currentUserId);
        goal.setTitle(StringUtils.hasText(request.getGoalTitle()) ? request.getGoalTitle() : request.getName());
        goal.setDescription(StringUtils.hasText(request.getGoalDescription()) ? request.getGoalDescription() : request.getDescription());
        goal.setStartDate(request.getStartDate() == null ? LocalDate.now() : request.getStartDate());
        goal.setEndDate(request.getEndDate());
        goal.setStatus(DEFAULT_STATUS);
        goalMapper.insert(goal);

        Team team = new Team();
        team.setCreatorId(currentUserId);
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setInviteCode(generateUniqueInviteCode());
        team.setGoalId(goal.getId());
        team.setStatus(DEFAULT_STATUS);
        teamMapper.insert(team);

        TeamMember owner = new TeamMember();
        owner.setTeamId(team.getId());
        owner.setUserId(currentUserId);
        owner.setRole(OWNER_ROLE);
        teamMemberMapper.insert(owner);

        return new TeamListResponse(
                team.getId(),
                team.getCreatorId(),
                team.getName(),
                team.getDescription(),
                team.getInviteCode(),
                team.getGoalId(),
                goal.getTitle(),
                team.getStatus()
        );
    }

    @Override
    @Transactional
    public Team joinTeam(TeamJoinRequest request, Long currentUserId) {
        if (request == null || !StringUtils.hasText(request.getInviteCode())) {
            throw new IllegalArgumentException("邀请码不能为空");
        }

        Team team = teamMapper.selectOne(new LambdaQueryWrapper<Team>()
                .eq(Team::getInviteCode, request.getInviteCode()));
        if (team == null) {
            throw new IllegalArgumentException("小组不存在");
        }

        List<TeamMember> lockedMembers = teamMemberMapper.selectByTeamIdForUpdate(team.getId());
        validateTeamExists(team.getId());

        if (findMember(lockedMembers, currentUserId) != null) {
            throw new IllegalArgumentException("已加入该小组");
        }

        TeamMember member = new TeamMember();
        member.setTeamId(team.getId());
        member.setUserId(currentUserId);
        member.setRole(MEMBER_ROLE);
        try {
            teamMemberMapper.insert(member);
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("已加入该小组");
        }

        return team;
    }

    @Override
    @Transactional
    public String leaveTeam(Long teamId, Long currentUserId) {
        Team team = validateTeamExists(teamId);

        List<TeamMember> lockedMembers = teamMemberMapper.selectByTeamIdForUpdate(teamId);
        TeamMember currentMember = findMember(lockedMembers, currentUserId);
        if (currentMember == null) {
            throw new IllegalArgumentException("未加入该小组");
        }

        if (OWNER_ROLE.equals(currentMember.getRole()) && lockedMembers.size() > 1) {
            throw new IllegalArgumentException("组长不能直接退出，请先转让组长");
        }

        if (OWNER_ROLE.equals(currentMember.getRole())) {
            Long goalId = team.getGoalId();
            if (goalId != null) {
                checkinMapper.delete(new LambdaQueryWrapper<CheckinRecord>()
                        .eq(CheckinRecord::getGoalId, goalId));
            }
            teamMemberMapper.deleteById(currentMember.getId());
            teamMapper.deleteById(teamId);
            if (goalId != null) {
                goalMapper.deleteById(goalId);
            }
            return "已退出并删除空小组";
        }

        teamMemberMapper.deleteById(currentMember.getId());
        return "退出小组成功";
    }

    @Override
    @Transactional
    public String transferOwner(Long teamId, TeamTransferOwnerRequest request, Long currentUserId) {
        validateTeamExists(teamId);
        if (request == null || request.getNewOwnerUserId() == null) {
            throw new IllegalArgumentException("新组长ID不能为空");
        }

        Long newOwnerUserId = request.getNewOwnerUserId();
        if (currentUserId.equals(newOwnerUserId)) {
            throw new IllegalArgumentException("不能转让给自己");
        }

        List<TeamMember> lockedMembers = teamMemberMapper.selectByTeamIdForUpdate(teamId);
        TeamMember currentMember = findMember(lockedMembers, currentUserId);
        if (currentMember == null) {
            throw new IllegalArgumentException("未加入该小组");
        }
        if (!OWNER_ROLE.equals(currentMember.getRole())) {
            throw new IllegalArgumentException("只有组长可以转让组长");
        }

        TeamMember newOwner = findMember(lockedMembers, newOwnerUserId);
        if (newOwner == null) {
            throw new IllegalArgumentException("新组长必须是小组成员");
        }

        currentMember.setRole(MEMBER_ROLE);
        newOwner.setRole(OWNER_ROLE);
        teamMemberMapper.updateById(currentMember);
        teamMemberMapper.updateById(newOwner);

        return "组长转让成功";
    }

    @Override
    public List<TeamListResponse> listMyTeams(Long currentUserId) {
        List<TeamMember> memberships = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getUserId, currentUserId)
                .orderByDesc(TeamMember::getJoinedAt));
        if (memberships.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> teamIds = memberships.stream()
                .map(TeamMember::getTeamId)
                .toList();
        List<Team> teams = teamMapper.selectList(new LambdaQueryWrapper<Team>()
                .in(Team::getId, teamIds));

        Map<Long, Goal> goalMap = selectGoalsByTeams(teams);
        return teams.stream()
                .map(team -> {
                    Goal goal = goalMap.get(team.getGoalId());
                    return new TeamListResponse(
                            team.getId(),
                            team.getCreatorId(),
                            team.getName(),
                            team.getDescription(),
                            team.getInviteCode(),
                            team.getGoalId(),
                            goal == null ? null : goal.getTitle(),
                            team.getStatus()
                    );
                })
                .toList();
    }

    @Override
    public List<TeamMemberResponse> listMembers(Long teamId, Long currentUserId) {
        validateTeamExists(teamId);
        checkCurrentUserInTeam(teamId, currentUserId);

        List<TeamMember> members = listTeamMembers(teamId);
        if (members.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, User> userMap = selectUsersByMembers(members);
        return members.stream()
                .map(member -> {
                    User user = userMap.get(member.getUserId());
                    return new TeamMemberResponse(
                            member.getUserId(),
                            user == null ? null : user.getUsername(),
                            user == null ? null : user.getNickname(),
                            member.getRole()
                    );
                })
                .toList();
    }

    @Override
    public List<TeamCheckinTodayResponse> listTodayCheckins(Long teamId, Long currentUserId) {
        Team team = validateTeamExists(teamId);
        checkCurrentUserInTeam(teamId, currentUserId);

        List<TeamMember> members = listTeamMembers(teamId);
        if (members.isEmpty()) {
            return Collections.emptyList();
        }

        Goal goal = team.getGoalId() == null ? null : goalMapper.selectById(team.getGoalId());
        Set<Long> checkedUserIds = team.getGoalId() == null
                ? Collections.emptySet()
                : selectCheckedUserIdsToday(members, team.getGoalId());
        Map<Long, User> userMap = selectUsersByMembers(members);

        return members.stream()
                .map(member -> {
                    User user = userMap.get(member.getUserId());
                    return new TeamCheckinTodayResponse(
                            member.getUserId(),
                            user == null ? null : user.getNickname(),
                            checkedUserIds.contains(member.getUserId()),
                            team.getGoalId(),
                            goal == null ? null : goal.getTitle()
                    );
                })
                .toList();
    }

    private Map<Long, Goal> selectGoalsByTeams(List<Team> teams) {
        List<Long> goalIds = teams.stream()
                .map(Team::getGoalId)
                .filter(goalId -> goalId != null)
                .distinct()
                .toList();
        if (goalIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return goalMapper.selectList(new LambdaQueryWrapper<Goal>()
                        .in(Goal::getId, goalIds))
                .stream()
                .collect(Collectors.toMap(Goal::getId, Function.identity()));
    }

    private String generateUniqueInviteCode() {
        for (int i = 0; i < 10; i++) {
            String inviteCode = generateInviteCode();
            Long count = teamMapper.selectCount(new LambdaQueryWrapper<Team>()
                    .eq(Team::getInviteCode, inviteCode));
            if (count == null || count == 0) {
                return inviteCode;
            }
        }
        throw new IllegalArgumentException("邀请码生成失败");
    }

    private String generateInviteCode() {
        StringBuilder builder = new StringBuilder(INVITE_CODE_LENGTH);
        for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
            builder.append(INVITE_CODE_CHARS.charAt(RANDOM.nextInt(INVITE_CODE_CHARS.length())));
        }
        return builder.toString();
    }

    private void validateTeamId(Long teamId) {
        if (teamId == null) {
            throw new IllegalArgumentException("小组ID不能为空");
        }
    }

    private Team validateTeamExists(Long teamId) {
        validateTeamId(teamId);
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new IllegalArgumentException("小组不存在");
        }
        return team;
    }

    private TeamMember findMember(List<TeamMember> members, Long userId) {
        return members.stream()
                .filter(member -> userId.equals(member.getUserId()))
                .findFirst()
                .orElse(null);
    }

    private void checkCurrentUserInTeam(Long teamId, Long currentUserId) {
        if (!existsMember(teamId, currentUserId)) {
            throw new IllegalArgumentException("未加入该小组");
        }
    }

    private boolean existsMember(Long teamId, Long userId) {
        Long count = teamMemberMapper.selectCount(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getUserId, userId));
        return count != null && count > 0;
    }

    private List<TeamMember> listTeamMembers(Long teamId) {
        return teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId)
                .orderByAsc(TeamMember::getId));
    }

    private Map<Long, User> selectUsersByMembers(List<TeamMember> members) {
        List<Long> userIds = members.stream()
                .map(TeamMember::getUserId)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return userMapper.selectList(new LambdaQueryWrapper<User>()
                        .in(User::getId, userIds))
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private Set<Long> selectCheckedUserIdsToday(List<TeamMember> members, Long goalId) {
        List<Long> userIds = members.stream()
                .map(TeamMember::getUserId)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return Collections.emptySet();
        }

        List<CheckinRecord> records = checkinMapper.selectList(new LambdaQueryWrapper<CheckinRecord>()
                .select(CheckinRecord::getUserId)
                .in(CheckinRecord::getUserId, userIds)
                .eq(CheckinRecord::getGoalId, goalId)
                .eq(CheckinRecord::getCheckinDate, LocalDate.now()));

        return records.stream()
                .map(CheckinRecord::getUserId)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
