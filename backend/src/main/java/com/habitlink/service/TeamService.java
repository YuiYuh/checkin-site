package com.habitlink.service;

import com.habitlink.dto.TeamCheckinTodayResponse;
import com.habitlink.dto.TeamCreateRequest;
import com.habitlink.dto.TeamJoinRequest;
import com.habitlink.dto.TeamMemberResponse;
import com.habitlink.entity.Team;

import java.util.List;

public interface TeamService {

    Team createTeam(TeamCreateRequest request);

    Team joinTeam(TeamJoinRequest request);

    List<Team> listMyTeams();

    List<TeamMemberResponse> listMembers(Long teamId);

    List<TeamCheckinTodayResponse> listTodayCheckins(Long teamId);
}
