package com.habitlink.service;

import com.habitlink.dto.CheckinRequest;
import com.habitlink.entity.CheckinRecord;

import java.util.List;

public interface CheckinService {

    CheckinRecord checkinToday(CheckinRequest request, Long currentUserId);

    List<CheckinRecord> listGoalCheckins(Long goalId, Long currentUserId);

    Boolean hasCheckedToday(Long goalId, Long currentUserId);

    void cancelTodayCheckin(Long goalId, Long currentUserId);
}
