package com.habitlink.controller;

import com.habitlink.common.Result;
import com.habitlink.dto.GoalCreateRequest;
import com.habitlink.entity.Goal;
import com.habitlink.service.GoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    public Result<Goal> createGoal(@RequestBody GoalCreateRequest request) {
        return Result.success(goalService.createGoal(request));
    }

    @GetMapping
    public Result<List<Goal>> listGoals() {
        return Result.success(goalService.listGoals());
    }

    @GetMapping("/{goalId}")
    public Result<Goal> getGoal(@PathVariable Long goalId) {
        return Result.success(goalService.getGoal(goalId));
    }

    @DeleteMapping("/{goalId}")
    public Result<Void> deleteGoal(@PathVariable Long goalId) {
        goalService.deleteGoal(goalId);
        return Result.success();
    }
}
