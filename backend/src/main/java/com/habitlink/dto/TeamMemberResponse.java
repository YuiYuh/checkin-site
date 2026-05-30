package com.habitlink.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberResponse {

    private Long userId;

    private String username;

    private String nickname;

    private String role;
}
