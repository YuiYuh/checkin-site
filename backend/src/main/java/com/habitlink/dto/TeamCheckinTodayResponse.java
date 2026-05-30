package com.habitlink.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamCheckinTodayResponse {

    private Long userId;

    private String nickname;

    private Boolean checkedToday;
}
