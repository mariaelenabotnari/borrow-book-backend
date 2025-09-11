package org.borrowbook.borrowbookbackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {
    private int maxAttempts;
    private int windowSeconds;
    private int defaultTTLSeconds;
}