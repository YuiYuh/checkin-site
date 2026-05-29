package com.habitlink.service;

import com.habitlink.dto.UserLoginRequest;
import com.habitlink.dto.UserRegisterRequest;
import com.habitlink.vo.LoginVO;
import com.habitlink.vo.UserVO;

public interface UserService {

    UserVO register(UserRegisterRequest request);

    LoginVO login(UserLoginRequest request);

    UserVO getCurrentUser(String authorization);
}
