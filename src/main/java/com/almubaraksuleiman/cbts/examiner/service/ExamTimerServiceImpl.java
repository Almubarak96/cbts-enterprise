// Package declaration - organizes classes within the examiner service package
package com.almubaraksuleiman.cbts.examiner.service;

// Import statements - required dependencies
import com.almubaraksuleiman.cbts.dto.ExamSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Service implementation for exam timer functionality using Redis.
 * Handles real-time countdown timers for exam sessions with WebSocket updates.

 * This service provides:
 * - Redis-based timer resource for persistence and scalability
 * - Real-time WebSocket updates to frontend clients
 * - Automatic exam submission when time expires
 * - Scheduled timer updates every second
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/
@Service // Marks this class as a Spring service bean
@RequiredArgsConstructor // Lombok: generates constructor for final fields
public class ExamTimerServiceImpl {

    // Redis template for storing and retrieving timer values
    private final StringRedisTemplate redisTemplate;

    // WebSocket messaging template for real-time updates to clients
    private final SimpMessagingTemplate messagingTemplate;

    // Service for exam session management (currently not used in this implementation)
    private ExamSessionService examSessionService;

    // Service for automatic exam submission when time expires
    private final TestAutoSubmitService autoSubmitService;

    /**
     * Starts a countdown timer for an exam session.
     * Stores the initial time in Redis and sends immediate update to frontend.
     *
     * @param sessionId The ID of the exam session to start timer for
     * @param durationInSeconds The total duration of the exam in seconds

     * Redis key format: "exam:{sessionId}:timeLeft"
     * WebSocket topic: "/topic/timer/{sessionId}"
     */
    public void startTimer(Long sessionId, int durationInSeconds) {
        String key = "exam:" + sessionId + ":timeLeft";
        // Store initial time in Redis
        redisTemplate.opsForValue().set(key, String.valueOf(durationInSeconds));

        // Immediately send initial time to all connected clients for this session
        messagingTemplate.convertAndSend("/topic/timer/" + sessionId, durationInSeconds);
    }

    /**
     * Retrieves the remaining time for an exam session.
     *
     * @param sessionId The ID of the exam session
     * @return int The remaining time in seconds, or 0 if not found/expired
     */
    public int getTimeLeft(Long sessionId) {
        String key = "exam:" + sessionId + ":timeLeft";
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Integer.parseInt(value) : 0;
    }

    /**
     * Scheduled method that runs every second to update all active exam timers.
     * Decrements each timer, sends WebSocket updates, and handles auto-submission.

     * This method is automatically invoked by Spring's scheduled task executor.
     */
    @Scheduled(fixedRate = 1000) // Runs every 1000 milliseconds (1 second)
    public void updateTimers() {
        // Find all active exam timer keys in Redis
        Set<String> keys = redisTemplate.keys("exam:*:timeLeft");
        if (keys == null) return;

        for (String key : keys) {
            // Get current time and parse session ID from key
            int time = Integer.parseInt(redisTemplate.opsForValue().get(key));
            Long sessionId = Long.parseLong(key.split(":")[1]);

            if (time > 0) {
                // Decrement timer and update Redis
                redisTemplate.opsForValue().set(key, String.valueOf(time - 1));
                // Send updated time to frontend
                messagingTemplate.convertAndSend("/topic/timer/" + sessionId, time - 1);
            } else {
                // Time has expired - handle auto-submission

                // 1. Trigger automatic exam submission
                autoSubmitService.completeExam(sessionId);

                // 2. Clean up Redis key
                redisTemplate.delete(key);

                // 3. Send final timer update (0 seconds)
                messagingTemplate.convertAndSend("/topic/timer/" + sessionId, "0");

                // 4. Notify frontend that exam has been auto-completed
                messagingTemplate.convertAndSend("/topic/exam-completed/" + sessionId, "completed");
            }
        }
    }

    /*
     * Potential additional methods that could be added:
     *
     * // Pause timer for a session (e.g., for technical issues)
     * public void pauseTimer(Long sessionId) {
     *     // Store current time and mark as paused
     * }
     *
     * // Resume paused timer
     * public void resumeTimer(Long sessionId) {
     *     // Restore from paused state
     * }
     *
     * // Add extra time to a session (e.g., for accommodations)
     * public void addTime(Long sessionId, int additionalSeconds) {
     *     // Update Redis value and notify frontend
     * }
     *
     * // Get all active sessions with their remaining times
     * public Map<Long, Integer> getAllActiveTimers() {
     *     // Return map of sessionId -> remaining time
     * }

     * // Forcefully stop a timer (admin function)
     * public void stopTimer(Long sessionId) {
     *     // Delete from Redis and notify frontend
     * }
     */
}