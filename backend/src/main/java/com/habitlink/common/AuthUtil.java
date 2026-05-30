package com.habitlink.common;

import org.springframework.util.StringUtils;

public final class AuthUtil {

    private static final String TOKEN_PREFIX = "Bearer user-";

    private AuthUtil() {
    }

    public static Long parseUserId(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith(TOKEN_PREFIX)) {
            throw new IllegalArgumentException("请先登录");
        }

        String userIdText = authorization.substring(TOKEN_PREFIX.length());
        try {
            return Long.valueOf(userIdText);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("请先登录");
        }
    }
}
