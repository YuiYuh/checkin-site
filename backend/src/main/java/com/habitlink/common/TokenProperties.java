package com.habitlink.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "habitlink.token")
public class TokenProperties {

    private String secret;

    private Long expireDays = 7L;
}
