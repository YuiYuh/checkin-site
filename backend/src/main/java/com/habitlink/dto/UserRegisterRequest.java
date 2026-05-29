package com.habitlink.dto;

import lombok.Data;

@Data
public class UserRegisterRequest {

    private String username;

    private String password;

    private String nickname;
}
