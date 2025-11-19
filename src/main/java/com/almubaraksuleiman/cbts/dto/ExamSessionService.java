package com.almubaraksuleiman.cbts.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/
@Service
@RequiredArgsConstructor
public class ExamSessionService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;


    public boolean hasActiveSession(Long studentId, Long testId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(getKey(studentId, testId)));
    }

        public void startSession(Long studentId, Long testId, int durationMinutes) {
        String key = getKey(studentId, testId);

        Map<String, Object> sessionData = Map.of(
                "startTime", System.currentTimeMillis(),
                "duration", durationMinutes
        );

        try {
            redisTemplate.opsForValue().set(
                    key,
                    objectMapper.writeValueAsString(sessionData),
                    durationMinutes, TimeUnit.MINUTES
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error saving exam session", e);
        }
    }

    public Optional<Map<String, Object>> getSession(Long studentId, Long testId) {
        String key = getKey(studentId, testId);
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) return Optional.empty();

        try {
            return Optional.of(objectMapper.readValue(json, new TypeReference<>() {}));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    public void endSession(Long studentId, Long testId) {
        redisTemplate.delete(getKey(studentId, testId));
    }

    private String getKey(Long studentId, Long testId) {
        return "exam:" + studentId + ":" + testId;
    }
}
