package com.habitlink.controller;

import com.habitlink.common.Result;
import com.habitlink.dto.TeamCheckinTodayResponse;
import com.habitlink.dto.TeamCreateRequest;
import com.habitlink.dto.TeamJoinRequest;
import com.habitlink.dto.TeamMemberResponse;
import com.habitlink.entity.Team;
import com.habitlink.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    public Result<Team> createTeam(@RequestBody TeamCreateRequest request) {
        return Result.success(teamService.createTeam(request));
    }

    @PostMapping("/join")
    public Result<Team> joinTeam(@RequestBody TeamJoinRequest request) {
        return Result.success(teamService.joinTeam(request));
    }

    @GetMapping("/my")
    public Result<List<Team>> listMyTeams() {
        return Result.success(teamService.listMyTeams());
    }

    @GetMapping("/{teamId}/members")
    public Result<List<TeamMemberResponse>> listMembers(@PathVariable Long teamId) {
        return Result.success(teamService.listMembers(teamId));
    }

    @GetMapping("/{teamId}/checkins/today")
    public Result<List<TeamCheckinTodayResponse>> listTodayCheckins(@PathVariable Long teamId) {
        return Result.success(teamService.listTodayCheckins(teamId));
    }
}
