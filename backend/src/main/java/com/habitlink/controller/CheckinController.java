package com.habitlink.controller;

import com.habitlink.common.AuthUtil;
import com.habitlink.common.Result;
import com.habitlink.dto.CheckinRequest;
import com.habitlink.entity.CheckinRecord;
import com.habitlink.service.CheckinService;
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
@RequestMapping("/api/checkins")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    @PostMapping
    public Result<CheckinRecord> checkinToday(@RequestBody CheckinRequest request,
                                              @RequestHeader(value = "Authorization", required = false) String authorization) {
        return Result.success(checkinService.checkinToday(request, AuthUtil.parseUserId(authorization)));
    }

    @GetMapping("/goal/{goalId}")
    public Result<List<CheckinRecord>> listGoalCheckins(@PathVariable Long goalId,
                                                        @RequestHeader(value = "Authorization", required = false) String authorization) {
        return Result.success(checkinService.listGoalCheckins(goalId, AuthUtil.parseUserId(authorization)));
    }

    @GetMapping("/today/{goalId}")
    public Result<Boolean> hasCheckedToday(@PathVariable Long goalId,
                                           @RequestHeader(value = "Authorization", required = false) String authorization) {
        return Result.success(checkinService.hasCheckedToday(goalId, AuthUtil.parseUserId(authorization)));
    }
}
