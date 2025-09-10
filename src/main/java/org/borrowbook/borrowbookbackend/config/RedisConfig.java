package org.borrowbook.borrowbookbackend.config;

import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.model.dto.VerificationSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final RedisProperties redisProperties;

    @Bean
    @Primary
    public RedisConnectionFactory cacheRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisProperties.getCache().getHost());
        config.setPort(redisProperties.getCache().getPort());
        config.setPassword(redisProperties.getCache().getPassword());
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisConnectionFactory persistenceRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisProperties.getPersistence().getHost());
        config.setPort(redisProperties.getPersistence().getPort());
        config.setPassword(redisProperties.getPersistence().getPassword());
        return new LettuceConnectionFactory(config);
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(cacheRedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    public RedisTemplate<String, VerificationSession> sessionRedisTemplate() {
        RedisTemplate<String, VerificationSession> template = new RedisTemplate<>();
        template.setConnectionFactory(cacheRedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(VerificationSession.class));
        return template;
    }

    @Bean
    public RedisTemplate<String, String> emailRedisTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(cacheRedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    public RedisTemplate<String, String> tokenPersistanceRedisTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(persistenceRedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
}