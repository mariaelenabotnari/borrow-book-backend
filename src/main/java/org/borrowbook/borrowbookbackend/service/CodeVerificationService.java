package org.borrowbook.borrowbookbackend.service;

import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.config.properties.RateLimitProperties;
import org.borrowbook.borrowbookbackend.exception.MaxOtpAttemptsExceededException;
import org.borrowbook.borrowbookbackend.model.dto.VerificationSessionDTO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CodeVerificationService {

    private final RedisTemplate<String, VerificationSessionDTO> sessionRedisTemplate;
    private final RedisTemplate<String, String> emailRedisTemplate;
    private final RateLimitProperties rateLimitProperties;

    private String buildSessionKey(String sessionId) {
        return "verification:session:" + sessionId;
    }

    private String buildEmailKey(String email) {
        return "verification:email:" + email;
    }

    public void storeSession(String sessionId, VerificationSessionDTO session) {
        sessionRedisTemplate.opsForValue().set(
                buildSessionKey(sessionId), session, Duration.ofSeconds(rateLimitProperties.getDefaultTTLSeconds()));
    }

    public void addSessionToEmail(String email, String sessionId) {
        emailRedisTemplate.opsForSet().add(buildEmailKey(email), sessionId);
    }

    private void deleteSession(String sessionId) {
        sessionRedisTemplate.delete(buildSessionKey(sessionId));
    }

    private void deleteAllOtherSessionsForEmail(String email, String exceptSessionId) {
        Set<String> sessionIds = emailRedisTemplate.opsForSet().members(buildEmailKey(email));
        if (sessionIds != null) {
            for (String id : sessionIds) {
                if (!id.equals(exceptSessionId)) {
                    deleteSession(id); // sessionRedisTemplate
                }
            }
        }
        emailRedisTemplate.delete(buildEmailKey(email));
    }

    private void incrementAttempts(String sessionId, VerificationSessionDTO session) {
        session.setAttemptCount(session.getAttemptCount() + 1);
        if (session.getAttemptCount() >= rateLimitProperties.getMaxAttempts()) {
            deleteSession(sessionId);
            throw new MaxOtpAttemptsExceededException("Maximum OTP attempts reached. Please request a new code.");
        }

        sessionRedisTemplate.boundValueOps(buildSessionKey(sessionId)).set(session);
    }

    public VerificationSessionDTO verifyCode(String sessionId, String code) {
        VerificationSessionDTO session = sessionRedisTemplate.boundValueOps(buildSessionKey(sessionId)).get();
        if (session == null) return null;

        if (session.getCode().equals(code)) {
            deleteSession(sessionId);

            if (emailRedisTemplate.hasKey(buildEmailKey(session.getEmail()))) {
                deleteAllOtherSessionsForEmail(session.getEmail(), sessionId);
            }
            return session;
        }
        incrementAttempts(sessionId, session);
        return null;
    }

}
