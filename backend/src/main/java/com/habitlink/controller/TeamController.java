package com.habitlink.controller;

import com.habitlink.common.AuthUtil;
import com.habitlink.common.Result;
import com.habitlink.dto.TeamCheckinTodayResponse;
import com.habitlink.dto.TeamCreateRequest;
import com.habitlink.dto.TeamJoinRequest;
import com.habitlink.dto.TeamListResponse;
import com.habitlink.dto.TeamMemberResponse;
import com.habitlink.dto.TeamTransferOwnerRequest;
import com.habitlink.entity.Team;
import com.habitlink.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final AuthUtil authUtil;
    private final TeamService teamService;

    @PostMapping
    public Result<TeamListResponse> createTeam(@RequestBody TeamCreateRequest request,
                                               @RequestHeader(value = "Authorization", required = false) String authorization) {
        return Result.success(teamService.createTeam(request, authUtil.parseUserId(authorization)));
    }

    @PostMapping("/join")
    public Result<Team> joinTeam(@RequestBody TeamJoinRequest request,
                                 @RequestHeader(value = "Authorization", required = false) String authorization) {
        return Result.success(teamService.joinTeam(request, authUtil.parseUserId(authorization)));
    }

    @PostMapping("/{teamId}/leave")
    public Result<String> leaveTeam(@PathVariable Long teamId,
                                    @RequestHeader(value = "Authorization", required = false) String authorization) {
        return Result.success(teamService.leaveTeam(teamId, authUtil.parseUserId(authorization)));
    }

    @PostMapping("/{teamId}/transfer-owner")
    public Result<String> transferOwner(@PathVariable Long teamId,
                                        @RequestBody TeamTransferOwnerRequest request,
                                        @RequestHeader(value = "Authorization", required = false) String authorization) {
        return Result.success(teamService.transferOwner(teamId, request, authUtil.parseUserId(authorization)));
    }

    @GetMapping("/my")
    public Result<List<TeamListResponse>> listMyTeams(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return Result.success(teamService.listMyTeams(authUtil.parseUserId(authorization)));
    }

    @GetMapping("/{teamId}/members")
    public Result<List<TeamMemberResponse>> listMembers(@PathVariable Long teamId,
                                                        @RequestHeader(value = "Authorization", required = false) String authorization) {
        return Result.success(teamService.listMembers(teamId, authUtil.parseUserId(authorization)));
    }

    @GetMapping("/{teamId}/checkins/today")
    public Result<List<TeamCheckinTodayResponse>> listTodayCheckins(@PathVariable Long teamId,
                                                                    @RequestHeader(value = "Authorization", required = false) String authorization) {
        return Result.success(teamService.listTodayCheckins(teamId, authUtil.parseUserId(authorization)));
    }
}
