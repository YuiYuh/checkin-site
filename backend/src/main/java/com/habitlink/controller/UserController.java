package com.habitlink.controller;

import com.habitlink.common.Result;
import com.habitlink.dto.UserLoginRequest;
import com.habitlink.dto.UserRegisterRequest;
import com.habitlink.service.UserService;
import com.habitlink.vo.LoginVO;
import com.habitlink.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody UserRegisterRequest request) {
        return Result.success(userService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody UserLoginRequest request) {
        return Result.success(userService.login(request));
    }

    @GetMapping("/me")
    public Result<UserVO> me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return Result.success(userService.getCurrentUser(authorization));
    }
}
