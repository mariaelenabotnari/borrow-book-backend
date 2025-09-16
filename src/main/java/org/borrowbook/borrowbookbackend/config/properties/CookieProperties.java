package org.borrowbook.borrowbookbackend.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.cookie")
public class CookieProperties {
    private TokenConfig accessToken;
    private TokenConfig refreshToken;

    @Data
    public static class TokenConfig {
        private String name;
        private int maxAgeSeconds;
    }
}