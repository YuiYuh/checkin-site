package com.habitlink.service;

import com.habitlink.dto.CheckinRequest;
import com.habitlink.entity.CheckinRecord;

import java.util.List;

public interface CheckinService {

    CheckinRecord checkinToday(CheckinRequest request);

    List<CheckinRecord> listGoalCheckins(Long goalId);

    Boolean hasCheckedToday(Long goalId);
}
