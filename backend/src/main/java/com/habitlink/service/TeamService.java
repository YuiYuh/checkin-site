package com.habitlink.service;

import com.habitlink.dto.TeamCheckinTodayResponse;
import com.habitlink.dto.TeamCreateRequest;
import com.habitlink.dto.TeamJoinRequest;
import com.habitlink.dto.TeamListResponse;
import com.habitlink.dto.TeamMemberResponse;
import com.habitlink.dto.TeamTransferOwnerRequest;
import com.habitlink.entity.Team;

import java.util.List;

public interface TeamService {

    TeamListResponse createTeam(TeamCreateRequest request, Long currentUserId);

    Team joinTeam(TeamJoinRequest request, Long currentUserId);

    String leaveTeam(Long teamId, Long currentUserId);

    String transferOwner(Long teamId, TeamTransferOwnerRequest request, Long currentUserId);

    List<TeamListResponse> listMyTeams(Long currentUserId);

    List<TeamMemberResponse> listMembers(Long teamId, Long currentUserId);

    List<TeamCheckinTodayResponse> listTodayCheckins(Long teamId, Long currentUserId);
}
