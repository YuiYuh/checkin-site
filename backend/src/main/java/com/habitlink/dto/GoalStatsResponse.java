package com.habitlink.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalStatsResponse {

    private Long goalId;

    private Long totalDays;

    private Integer continuousDays;
}
