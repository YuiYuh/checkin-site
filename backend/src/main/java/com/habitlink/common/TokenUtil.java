package com.habitlink.common;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
public class TokenUtil {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final long MILLIS_PER_DAY = 24L * 60L * 60L * 1000L;

    private final TokenProperties tokenProperties;

    @PostConstruct
    public void validateConfig() {
        if (!StringUtils.hasText(tokenProperties.getSecret())) {
            throw new IllegalStateException("HABITLINK_TOKEN_SECRET is required");
        }
    }

    public String generateToken(Long userId) {
        long expireAt = Instant.now().toEpochMilli() + tokenProperties.getExpireDays() * MILLIS_PER_DAY;
        String payload = userId + "." + expireAt;
        return payload + "." + sign(payload);
    }

    public Long parseToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("请先登录");
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("请先登录");
        }

        Long userId;
        long expireAt;
        try {
            userId = Long.valueOf(parts[0]);
            expireAt = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("请先登录");
        }

        String payload = parts[0] + "." + parts[1];
        if (!constantTimeEquals(sign(payload), parts[2])) {
            throw new IllegalArgumentException("登录状态无效");
        }
        if (expireAt < Instant.now().toEpochMilli()) {
            throw new IllegalArgumentException("登录已过期，请重新登录");
        }

        return userId;
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(tokenProperties.getSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Token签名失败", e);
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (expected == null || actual == null || expected.length() != actual.length()) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < expected.length(); i++) {
            result |= expected.charAt(i) ^ actual.charAt(i);
        }
        return result == 0;
    }
}
