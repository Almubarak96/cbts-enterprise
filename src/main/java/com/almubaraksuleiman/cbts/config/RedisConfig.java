package com.almubaraksuleiman.cbts.config;

import com.almubaraksuleiman.cbts.dto.TestInstructions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/


@Configuration
public class RedisConfig {

    // Redis key patterns for better organization
    public static final String TEST_INSTRUCTIONS_KEY = "test:instructions"; // Hash key: testId -> instructions
    public static final String USER_READ_STATUS_KEY = "user:read:status";   // Hash key: userId:testId -> status

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(
                new org.springframework.data.redis.connection.RedisStandaloneConfiguration(
                        System.getenv().getOrDefault("SPRING_DATA_REDIS_HOST", "localhost"),
                        Integer.parseInt(System.getenv().getOrDefault("SPRING_DATA_REDIS_PORT", "6379"))
                )
        );
    }


    // For exam timer (existing)
    @Bean
    public RedisTemplate<String, Integer> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Integer> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }



    // For exam instructions - Object template
    @Bean
    public RedisTemplate<String, Object> redisObjectTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    // Hash operations for user status
    @Bean
    public HashOperations<String, String, String> userStatusHashOperations(
            RedisTemplate<String, Object> redisObjectTemplate) {
        return redisObjectTemplate.opsForHash();
    }

    // Hash operations for test instructions
    @Bean
    public HashOperations<String, String, TestInstructions> examInstructionsHashOperations(
            RedisTemplate<String, Object> redisObjectTemplate) {
        return redisObjectTemplate.opsForHash();
    }

    // Default instructions template (will be customized per test)
    @Bean
    public List<String> defaultInstructionTemplate() {
        return List.of(
                "This exam consists of multiple-choice questions. You must answer all questions.",
                "You have %d minutes to complete the exam.",
                "The exam contains %d questions.",
                "Passing score is %d%%.",
                "Questions will be presented %s.",
                "Answer choices will be %s.",
                "Do not refresh the page during the exam.",
                "Ensure you have a stable internet connection.",
                "Any attempt to cheat will result in immediate disqualification."
        );
    }



}

