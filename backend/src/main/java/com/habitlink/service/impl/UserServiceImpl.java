package com.habitlink.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.habitlink.common.AuthUtil;
import com.habitlink.common.TokenUtil;
import com.habitlink.dto.UserLoginRequest;
import com.habitlink.dto.UserRegisterRequest;
import com.habitlink.entity.User;
import com.habitlink.mapper.UserMapper;
import com.habitlink.service.UserService;
import com.habitlink.vo.LoginVO;
import com.habitlink.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final AuthUtil authUtil;
    private final TokenUtil tokenUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserVO register(UserRegisterRequest request) {
        validateRegisterRequest(request);

        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : request.getUsername());
        user.setStatus(1);
        userMapper.insert(user);

        return UserVO.from(user);
    }

    @Override
    public LoginVO login(UserLoginRequest request) {
        validateLoginRequest(request);

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .last("LIMIT 1"));
        if (user == null || !matchesPassword(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        upgradePlainPasswordIfNeeded(user, request.getPassword());
        return new LoginVO(tokenUtil.generateToken(user.getId()), UserVO.from(user));
    }

    @Override
    public UserVO getCurrentUser(String authorization) {
        Long userId = authUtil.parseUserId(authorization);
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return UserVO.from(user);
    }

    private void validateRegisterRequest(UserRegisterRequest request) {
        if (request == null || !StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
    }

    private void validateLoginRequest(UserLoginRequest request) {
        if (request == null || !StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (!StringUtils.hasText(storedPassword)) {
            return false;
        }
        if (isBcryptPassword(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return rawPassword.equals(storedPassword);
    }

    private void upgradePlainPasswordIfNeeded(User user, String rawPassword) {
        if (isBcryptPassword(user.getPassword())) {
            return;
        }
        user.setPassword(passwordEncoder.encode(rawPassword));
        userMapper.updateById(user);
    }

    private boolean isBcryptPassword(String password) {
        return StringUtils.hasText(password)
                && (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"));
    }
}
