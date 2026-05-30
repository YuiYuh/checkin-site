package com.habitlink.controller;

import com.habitlink.common.AuthUtil;
import com.habitlink.common.Result;
import com.habitlink.dto.GoalCreateRequest;
import com.habitlink.dto.GoalListResponse;
import com.habitlink.dto.GoalStatsResponse;
import com.habitlink.entity.Goal;
import com.habitlink.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    public Result<Goal> createGoal(@RequestBody GoalCreateRequest request,
                                   @RequestHeader(value = "Authorization", required = false) String authorization) {
        return Result.success(goalService.createGoal(request, AuthUtil.parseUserId(authorization)));
    }

    @GetMapping
    public Result<List<GoalListResponse>> listGoals(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return Result.success(goalService.listGoals(AuthUtil.parseUserId(authorization)));
    }

    @GetMapping("/{goalId}")
    public Result<Goal> getGoal(@PathVariable Long goalId,
                                @RequestHeader(value = "Authorization", required = false) String authorization) {
        return Result.success(goalService.getGoal(goalId, AuthUtil.parseUserId(authorization)));
    }

    @GetMapping("/{goalId}/stats")
    public Result<GoalStatsResponse> getGoalStats(@PathVariable Long goalId,
                                                  @RequestHeader(value = "Authorization", required = false) String authorization) {
        return Result.success(goalService.getGoalStats(goalId, AuthUtil.parseUserId(authorization)));
    }

    @DeleteMapping("/{goalId}")
    public Result<Void> deleteGoal(@PathVariable Long goalId,
                                   @RequestHeader(value = "Authorization", required = false) String authorization) {
        goalService.deleteGoal(goalId, AuthUtil.parseUserId(authorization));
        return Result.success();
    }
}
