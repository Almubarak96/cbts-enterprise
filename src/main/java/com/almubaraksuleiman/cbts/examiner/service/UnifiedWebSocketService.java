package com.almubaraksuleiman.cbts.examiner.service;


import com.almubaraksuleiman.cbts.examiner.model.AnalyticsData;
import com.almubaraksuleiman.cbts.examiner.service.TestAutoSubmitService;
import com.almubaraksuleiman.cbts.examiner.service.impl.AnalyticsService;
import com.almubaraksuleiman.cbts.student.repository.StudentExamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unified WebSocket service handling both exam timers and analytics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UnifiedWebSocketService {

    private final StudentExamRepository studentExamRepository;

    // Timer dependencies
    private final StringRedisTemplate redisTemplate;
    private final TestAutoSubmitService autoSubmitService;
    
    // Analytics dependencies
    private final AnalyticsService analyticsService;
    private final SimpMessagingTemplate messagingTemplate;
    
    // Track active subscriptions
    private final Map<Long, Integer> activeAnalyticsSubscriptions = new ConcurrentHashMap<>();

    // ==================== TIMER FUNCTIONALITY ====================
    
    /**
     * Starts a countdown timer for an exam session
     */
    public void startTimer(Long sessionId, int durationInSeconds) {        String key = "exam:" + sessionId + ":timeLeft";
        redisTemplate.opsForValue().set(key, String.valueOf(durationInSeconds));
        messagingTemplate.convertAndSend("/topic/timer/" + sessionId, durationInSeconds);
    }


    /**
     * Stops and clears the timer for an exam session
     */
    public void stopTimer(Long sessionId) {
        String key = "exam:" + sessionId + ":timeLeft";

        // Delete the timer from Redis
        Boolean deleted = redisTemplate.delete(key);

        if (Boolean.TRUE.equals(deleted)) {
            log.info("Stopped timer for session: {}", sessionId);

            // Notify clients that timer has been stopped
            messagingTemplate.convertAndSend("/topic/timer/" + sessionId, "stopped");
            messagingTemplate.convertAndSend("/topic/exam-completed/" + sessionId, "timer_stopped");
        } else {
            log.warn("Timer not found for session: {}", sessionId);
        }
    }

    /**
     * Pauses the timer for an exam session (optional - useful for breaks)
     */
    public void pauseTimer(Long sessionId) {
        String key = "exam:" + sessionId + ":timeLeft";
        String timeLeft = redisTemplate.opsForValue().get(key);

        if (timeLeft != null) {
            // You could store the paused time in a separate key for resuming
            String pausedKey = "exam:" + sessionId + ":pausedTime";
            redisTemplate.opsForValue().set(pausedKey, timeLeft);

            log.info("Paused timer for session: {} with {} seconds remaining", sessionId, timeLeft);
            messagingTemplate.convertAndSend("/topic/timer/" + sessionId, "paused");
        }
    }

    /**
     * Resumes a paused timer (optional)
     */
    public void resumeTimer(Long sessionId) {
        String pausedKey = "exam:" + sessionId + ":pausedTime";
        String timeLeft = redisTemplate.opsForValue().get(pausedKey);

        if (timeLeft != null) {
            String timerKey = "exam:" + sessionId + ":timeLeft";
            redisTemplate.opsForValue().set(timerKey, timeLeft);
            redisTemplate.delete(pausedKey);

            log.info("Resumed timer for session: {} with {} seconds remaining", sessionId, timeLeft);
            messagingTemplate.convertAndSend("/topic/timer/" + sessionId, timeLeft);
        }
    }


    /**
     * Checks if a timer is active for the given session
     */
    public boolean isTimerActive(Long sessionId) {
        String key = "exam:" + sessionId + ":timeLeft";
        String timeLeft = redisTemplate.opsForValue().get(key);
        return timeLeft != null && Integer.parseInt(timeLeft) > 0;
    }


    /**
     * Retrieves the remaining time for an exam session by student and test
     */
    public int getTimeLeft(Long studentId, Long testId) {
        // First, we need to find the session ID for this student and test
        Long sessionId = findSessionIdByStudentAndTest(studentId, testId);
        if (sessionId == null) {
            log.warn("No active session found for student {} and test {}", studentId, testId);
            return 0;
        }

        String key = "exam:" + sessionId + ":timeLeft";
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Integer.parseInt(value) : 0;
    }

    /**
     * Helper method to find session ID by student and test - USING OPTION 2
     */
    private Long findSessionIdByStudentAndTest(Long studentId, Long testId) {
        // Option 2: Query from database (most reliable)
        return studentExamRepository.findByStudentIdAndTestId(studentId, testId)
                .map(studentExam -> studentExam.getSessionId())
                .orElse(null);
    }

    /**
     * Get all active timer sessions (compatibility method)
     */
    public Map<String, String> getActiveTimers() {
        Set<String> keys = redisTemplate.keys("exam:*:timeLeft");
        Map<String, String> activeTimers = new HashMap<>();

        if (keys != null) {
            for (String key : keys) {
                String timeLeft = redisTemplate.opsForValue().get(key);
                activeTimers.put(key, timeLeft);
            }
        }

        return activeTimers;
    }


    /**
     * Scheduled timer updates - runs every second
     */
//    @Scheduled(fixedRate = 1000)
//    public void updateTimers() {
//        Set<String> keys = redisTemplate.keys("exam:*:timeLeft");
//        if (keys == null) return;
//
//        for (String key : keys) {
//            int time = Integer.parseInt(redisTemplate.opsForValue().get(key));
//            Long sessionId = Long.parseLong(key.split(":")[1]);
//
//            if (time > 0) {
//                redisTemplate.opsForValue().set(key, String.valueOf(time - 1));
//                messagingTemplate.convertAndSend("/topic/timer/" + sessionId, time - 1);
//            } else {
//                // Time expired - auto submit
//                autoSubmitService.completeExam(sessionId);
//                redisTemplate.delete(key);
//                messagingTemplate.convertAndSend("/topic/timer/" + sessionId, "0");
//                messagingTemplate.convertAndSend("/topic/exam-completed/" + sessionId, "completed");
//            }
//        }
//    }



    /**
     * Scheduled timer updates - runs every second with improved error handling
     */
    @Scheduled(fixedRate = 1000)
    public void updateTimers() {
        Set<String> keys = redisTemplate.keys("exam:*:timeLeft");
        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {
            try {
                String timeValue = redisTemplate.opsForValue().get(key);
                if (timeValue == null) continue;

                int time = Integer.parseInt(timeValue);
                Long sessionId = Long.parseLong(key.split(":")[1]);

                if (time > 0) {
                    // Use atomic decrement to prevent race conditions
                    Long newTime = redisTemplate.opsForValue().decrement(key);
                    if (newTime != null) {
                        messagingTemplate.convertAndSend("/topic/timer/" + sessionId, newTime.intValue());
                    }
                } else {
                    // Time expired - auto submit
                    handleExpiredTimer(sessionId, key);
                }
            } catch (NumberFormatException e) {
                log.error("Invalid time value for key {}: {}", key, redisTemplate.opsForValue().get(key));
                // Clean up invalid timer
                redisTemplate.delete(key);
            } catch (Exception e) {
                log.error("Error processing timer for key {}: {}", key, e.getMessage());
            }
        }
    }

    /**
     * Handle expired timer with proper cleanup
     */
    private void handleExpiredTimer(Long sessionId, String key) {
        try {
            log.info("Timer expired for session {}, initiating auto-submit", sessionId);

            // Auto submit the exam
            autoSubmitService.completeExam(sessionId);

            // Clean up Redis keys
            redisTemplate.delete(key);

            // Send completion notifications
            messagingTemplate.convertAndSend("/topic/timer/" + sessionId, "0");
            messagingTemplate.convertAndSend("/topic/exam-completed/" + sessionId, "completed");

            log.info("Successfully processed auto-submit for session {}", sessionId);

        } catch (Exception e) {
            log.error("Failed to auto-submit exam for session {}: {}", sessionId, e.getMessage());

            // Still send notifications but indicate error
            messagingTemplate.convertAndSend("/topic/timer/" + sessionId, "error");
            messagingTemplate.convertAndSend("/topic/exam-completed/" + sessionId, "auto_submit_failed");
        }
    }






    // ==================== ANALYTICS FUNCTIONALITY ====================

    @MessageMapping("/analytics/{testId}")
    @SendTo("/topic/analytics/{testId}")
    public AnalyticsData streamAnalytics(@DestinationVariable Long testId) {
        log.info("WebSocket analytics request received for test ID: {}", testId);
        log.info("Message mapping: /app/analytics/{} -> /topic/analytics/{}", testId, testId);

        activeAnalyticsSubscriptions.put(testId, activeAnalyticsSubscriptions.getOrDefault(testId, 0) + 1);
        log.info("👥 Active subscribers for test {}: {}", testId, activeAnalyticsSubscriptions.get(testId));

        try {
            AnalyticsData analytics = analyticsService.getTestAnalytics(testId, null);
            log.info("Successfully fetched analytics for test {}: {} students", testId,
                    analytics.getSummary().getTotalStudents());

            // Log the response being sent
            log.info("Sending analytics data to /topic/analytics/{}", testId);
            return analytics;
        } catch (Exception e) {
            log.error("Error fetching analytics for test {}: {}", testId, e.getMessage());
            // Return empty analytics data instead of throwing exception
            AnalyticsData emptyData = new AnalyticsData();
            // Initialize empty summary to avoid NPE
            // emptyData.setSummary(new AnalyticsSummary(0, 0, 0, 0, 0, 0));
            return emptyData;
        }
    }

    @MessageMapping("/analytics/{testId}/subscribe")
    @SendTo("/topic/analytics/{testId}")
    public String handleAnalyticsSubscription(@DestinationVariable Long testId) {
        activeAnalyticsSubscriptions.put(testId, activeAnalyticsSubscriptions.getOrDefault(testId, 0) + 1);
        log.info("New analytics subscription for test {}. Total subscribers: {}", 
                testId, activeAnalyticsSubscriptions.get(testId));
        return "ANALYTICS_SUBSCRIBED_" + testId;
    }

    @MessageMapping("/analytics/{testId}/unsubscribe")
    @SendTo("/topic/analytics/{testId}")
    public String handleAnalyticsUnsubscription(@DestinationVariable Long testId) {
        int currentSubscribers = activeAnalyticsSubscriptions.getOrDefault(testId, 0);
        if (currentSubscribers > 0) {
            activeAnalyticsSubscriptions.put(testId, currentSubscribers - 1);
        }
        log.info("Analytics unsubscription for test {}. Remaining subscribers: {}", 
                testId, activeAnalyticsSubscriptions.get(testId));
        return "ANALYTICS_UNSUBSCRIBED_" + testId;
    }

    /**
     * Auto-refresh analytics every 30 seconds for active subscribers
     */
    @Scheduled(fixedRate = 30000)
    public void broadcastAnalyticsUpdates() {
        log.debug("Checking for analytics updates...");
        
        for (Map.Entry<Long, Integer> entry : activeAnalyticsSubscriptions.entrySet()) {
            Long testId = entry.getKey();
            int subscriberCount = entry.getValue();
            
            if (subscriberCount > 0) {
                try {
                    log.debug("Auto-refreshing analytics for test {} ({} subscribers)", testId, subscriberCount);
                    AnalyticsData updatedAnalytics = analyticsService.getTestAnalytics(testId, null);
                    messagingTemplate.convertAndSend("/topic/analytics/" + testId, updatedAnalytics);
                    log.debug("Sent analytics update for test {} to {} subscribers", testId, subscriberCount);
                } catch (Exception e) {
                    log.warn("Failed to auto-refresh analytics for test {}: {}", testId, e.getMessage());
                }
            }
        }
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Manual trigger to refresh analytics for a specific test
     */
    public void refreshAnalytics(Long testId) {
        if (activeAnalyticsSubscriptions.getOrDefault(testId, 0) > 0) {
            try {
                AnalyticsData updatedAnalytics = analyticsService.getTestAnalytics(testId, null);
                messagingTemplate.convertAndSend("/topic/analytics/" + testId, updatedAnalytics);
                log.info("Manually refreshed analytics for test {}", testId);
            } catch (Exception e) {
                log.error("Error manually refreshing analytics for test {}: {}", testId, e.getMessage());
            }
        }
    }

    public int getActiveAnalyticsSubscribers(Long testId) {
        return activeAnalyticsSubscriptions.getOrDefault(testId, 0);
    }

    /**
     * Get all active timer sessions
     */
//    public Map<String, String> getActiveTimers() {
//        Set<String> keys = redisTemplate.keys("exam:*:timeLeft");
//        Map<String, String> activeTimers = new java.util.HashMap<>();
//
//        if (keys != null) {
//            for (String key : keys) {
//                String timeLeft = redisTemplate.opsForValue().get(key);
//                activeTimers.put(key, timeLeft);
//            }
//        }
//
//        return activeTimers;
//    }
}