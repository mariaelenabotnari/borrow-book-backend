package org.borrowbook.borrowbookbackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.management.openmbean.ArrayType;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RateLimiterService {
    private final RedisTemplate<String, String> redisTemplate;

    private String buildKey(String keyPrefix, String identifier) {
        return "rate_limit:" + keyPrefix + ":" + identifier;
    }

    public long checkRateLimit(String keyPrefix, String identifier, int limit, long windowSeconds) {
        String key = buildKey(keyPrefix, identifier);

        long now = Instant.now().toEpochMilli();
        long windowStart = now - windowSeconds * 1000;

        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

        Long count = redisTemplate.opsForZSet().zCard(key);

        if (count != null && count >= limit) {
            var rangeWithScores =
                    redisTemplate.opsForZSet().rangeWithScores(key, 0, 0);

            Double oldestScore = (rangeWithScores == null || rangeWithScores.isEmpty())
                    ? null
                    : rangeWithScores.iterator().next().getScore();

            if (oldestScore != null) {
                long oldestTs = oldestScore.longValue();
                long retryAfterMillis = (oldestTs + windowSeconds * 1000) - now;
                return TimeUnit.MILLISECONDS.toSeconds(Math.max(retryAfterMillis, 1));
            }
            return windowSeconds;
        }

        String member = UUID.randomUUID().toString();
        redisTemplate.opsForZSet().add(key, member, now);

        redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);

        return 0;
    }

    public void deleteRateLimit(String identifier, String... keyPrefixes) {
        for (String keyPrefix : keyPrefixes) {
            redisTemplate.delete(buildKey(keyPrefix, identifier));
        }
    }
}
