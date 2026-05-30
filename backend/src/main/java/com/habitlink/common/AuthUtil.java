package com.habitlink.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class AuthUtil {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenUtil tokenUtil;

    public Long parseUserId(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(BEARER_PREFIX)) {
            throw new IllegalArgumentException("请先登录");
        }

        return tokenUtil.parseToken(authorization.substring(BEARER_PREFIX.length()));
    }
}
