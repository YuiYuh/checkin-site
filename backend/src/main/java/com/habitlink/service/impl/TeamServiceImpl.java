package com.habitlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.habitlink.dto.TeamCheckinTodayResponse;
import com.habitlink.dto.TeamCreateRequest;
import com.habitlink.dto.TeamJoinRequest;
import com.habitlink.dto.TeamMemberResponse;
import com.habitlink.entity.CheckinRecord;
import com.habitlink.entity.Team;
import com.habitlink.entity.TeamMember;
import com.habitlink.entity.User;
import com.habitlink.mapper.CheckinMapper;
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

    private static final Long CURRENT_USER_ID = 1L;
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

    @Override
    @Transactional
    public Team createTeam(TeamCreateRequest request) {
        if (request == null || !StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("小组名称不能为空");
        }

        Team team = new Team();
        team.setCreatorId(CURRENT_USER_ID);
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setInviteCode(generateUniqueInviteCode());
        team.setStatus(DEFAULT_STATUS);
        teamMapper.insert(team);

        TeamMember owner = new TeamMember();
        owner.setTeamId(team.getId());
        owner.setUserId(CURRENT_USER_ID);
        owner.setRole(OWNER_ROLE);
        teamMemberMapper.insert(owner);

        return team;
    }

    @Override
    public Team joinTeam(TeamJoinRequest request) {
        if (request == null || !StringUtils.hasText(request.getInviteCode())) {
            throw new IllegalArgumentException("邀请码不能为空");
        }

        Team team = teamMapper.selectOne(new LambdaQueryWrapper<Team>()
                .eq(Team::getInviteCode, request.getInviteCode()));
        if (team == null) {
            throw new IllegalArgumentException("小组不存在");
        }

        if (existsMember(team.getId(), CURRENT_USER_ID)) {
            throw new IllegalArgumentException("已加入该小组");
        }

        TeamMember member = new TeamMember();
        member.setTeamId(team.getId());
        member.setUserId(CURRENT_USER_ID);
        member.setRole(MEMBER_ROLE);
        try {
            teamMemberMapper.insert(member);
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("已加入该小组");
        }

        return team;
    }

    @Override
    public List<Team> listMyTeams() {
        List<TeamMember> memberships = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getUserId, CURRENT_USER_ID)
                .orderByDesc(TeamMember::getJoinedAt));
        if (memberships.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> teamIds = memberships.stream()
                .map(TeamMember::getTeamId)
                .toList();
        return teamMapper.selectList(new LambdaQueryWrapper<Team>()
                .in(Team::getId, teamIds));
    }

    @Override
    public List<TeamMemberResponse> listMembers(Long teamId) {
        validateTeamExists(teamId);

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
    public List<TeamCheckinTodayResponse> listTodayCheckins(Long teamId) {
        validateTeamExists(teamId);

        List<TeamMember> members = listTeamMembers(teamId);
        if (members.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, User> userMap = selectUsersByMembers(members);
        Set<Long> checkedUserIds = selectCheckedUserIdsToday(members);

        return members.stream()
                .map(member -> {
                    User user = userMap.get(member.getUserId());
                    return new TeamCheckinTodayResponse(
                            member.getUserId(),
                            user == null ? null : user.getNickname(),
                            checkedUserIds.contains(member.getUserId())
                    );
                })
                .toList();
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

    private void validateTeamExists(Long teamId) {
        validateTeamId(teamId);
        if (teamMapper.selectById(teamId) == null) {
            throw new IllegalArgumentException("小组不存在");
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

    private Set<Long> selectCheckedUserIdsToday(List<TeamMember> members) {
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
                .eq(CheckinRecord::getCheckinDate, LocalDate.now()));

        return records.stream()
                .map(CheckinRecord::getUserId)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
