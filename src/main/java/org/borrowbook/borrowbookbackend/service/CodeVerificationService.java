package org.borrowbook.borrowbookbackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CodeVerificationService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final Duration defaultTTL = Duration.ofMinutes(15);

    private String buildKey(String username) {
        return "verification:" + username;
    }

    public void storeCode(String username, String code) {
        redisTemplate.opsForValue().set(buildKey(username),  code, defaultTTL);
    }

    public String getCode(String username) {
        return (String) redisTemplate.opsForValue().get(buildKey(username));
    }

    public void deleteCode(String username) {
        redisTemplate.delete(buildKey(username));
    }
}
