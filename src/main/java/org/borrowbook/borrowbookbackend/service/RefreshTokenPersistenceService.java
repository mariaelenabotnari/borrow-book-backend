package org.borrowbook.borrowbookbackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenPersistenceService {

    private final RedisTemplate<String, String> tokenPersistanceRedisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    public void storeRefreshToken(String email, String refreshToken, Duration tokenExpiry) {
        String key = buildKey(email);
        tokenPersistanceRedisTemplate.opsForValue().set(key, refreshToken, tokenExpiry);
    }

    public String getRefreshToken(String email) {
        String key = buildKey(email);
        return tokenPersistanceRedisTemplate.opsForValue().get(key);
    }

    public void removeRefreshToken(String email) {
        String key = buildKey(email);
        tokenPersistanceRedisTemplate.delete(key);
    }

    public boolean isPersistedRefreshToken(String email, String providedToken) {
        if (providedToken == null)
            return false;

        String cachedToken = getRefreshToken(email);
        return cachedToken != null && cachedToken.equals(providedToken);
    }

    private String buildKey(String email) {
        return REFRESH_TOKEN_PREFIX + email;
    }
}