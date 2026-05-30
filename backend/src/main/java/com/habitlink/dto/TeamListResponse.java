package com.habitlink.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamListResponse {

    private Long id;

    private Long creatorId;

    private String name;

    private String description;

    private String inviteCode;

    private Long goalId;

    private String goalTitle;

    private Integer status;
}
