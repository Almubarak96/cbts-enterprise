package com.almubaraksuleiman.cbts.api;


import com.almubaraksuleiman.cbts.examiner.model.AnalyticsData;
import com.almubaraksuleiman.cbts.examiner.service.impl.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketAnalyticsController {

    private final AnalyticsService analyticsService;
    private final Map<Long, Integer> activeTestSubscriptions = new HashMap<>();

    @MessageMapping("/analytics/{testId}")
    @SendTo("/topic/analytics/{testId}")
    public AnalyticsData streamAnalytics(@DestinationVariable Long testId) {
        log.info("WebSocket request for analytics for test ID: {}", testId);
        activeTestSubscriptions.put(testId, activeTestSubscriptions.getOrDefault(testId, 0) + 1);
        
        try {
            return analyticsService.getTestAnalytics(testId, null);
        } catch (Exception e) {
            log.error("Error fetching analytics for test {}: {}", testId, e.getMessage());
            throw new RuntimeException("Unable to fetch analytics data");
        }
    }

    @MessageMapping("/analytics/{testId}/subscribe")
    @SendTo("/topic/analytics/{testId}")
    public String handleSubscription(@DestinationVariable Long testId) {
        activeTestSubscriptions.put(testId, activeTestSubscriptions.getOrDefault(testId, 0) + 1);
        log.info("New subscription for test {} analytics. Total subscribers: {}", testId, activeTestSubscriptions.get(testId));
        return "SUBSCRIBED_TO_ANALYTICS";
    }

    @MessageMapping("/analytics/{testId}/unsubscribe")
    @SendTo("/topic/analytics/{testId}")
    public String handleUnsubscription(@DestinationVariable Long testId) {
        int currentSubscribers = activeTestSubscriptions.getOrDefault(testId, 0);
        if (currentSubscribers > 0) {
            activeTestSubscriptions.put(testId, currentSubscribers - 1);
        }
        log.info("Unsubscription for test {} analytics. Remaining subscribers: {}", testId, activeTestSubscriptions.get(testId));
        return "UNSUBSCRIBED_FROM_ANALYTICS";
    }

    // Auto-refresh analytics every 30 seconds for active subscribers
    @Scheduled(fixedRate = 30000)
    public void broadcastAnalyticsUpdates() {
        for (Map.Entry<Long, Integer> entry : activeTestSubscriptions.entrySet()) {
            if (entry.getValue() > 0) {
                Long testId = entry.getKey();
                try {
                    AnalyticsData updatedAnalytics = analyticsService.getTestAnalytics(testId, null);
                    // In a real implementation, you'd use SimpMessagingTemplate to send to specific topics
                    log.debug("Auto-refreshing analytics for test {} ({} subscribers)", testId, entry.getValue());
                } catch (Exception e) {
                    log.warn("Failed to auto-refresh analytics for test {}: {}", testId, e.getMessage());
                }
            }
        }
    }

    public int getActiveSubscribers(Long testId) {
        return activeTestSubscriptions.getOrDefault(testId, 0);
    }
}