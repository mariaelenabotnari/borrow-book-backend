package org.borrowbook.borrowbookbackend.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "redis")
public class RedisProperties {

    private RedisConfig cache;
    private RedisConfig persistence;

    @Data
    public static class RedisConfig {
        private String host;
        private int port;
        private String username;
        private String password;
        private int timeout;
    }
}