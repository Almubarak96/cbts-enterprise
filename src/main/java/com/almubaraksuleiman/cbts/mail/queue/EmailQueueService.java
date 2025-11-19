package com.almubaraksuleiman.cbts.mail.queue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailQueueService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String EMAIL_QUEUE_KEY = "email:queue";
    private static final String EMAIL_DLQ_KEY = "email:dlq";
    private static final int MAX_RETRIES = 3;

    public void queueVerificationEmail(String to, String subject, String templateName,
                                       Map<String, Object> templateVariables, String username) {
        EmailQueueMessage message = EmailQueueMessage.builder()
                .id(UUID.randomUUID().toString())
                .to(to)
                .subject(subject)
                .templateName(templateName)
                .templateVariables(templateVariables)
                .username(username)
                .emailType("VERIFICATION")
                .attemptCount(0)
                .firstAttemptAt(LocalDateTime.now())
                .build();

        queueEmail(message);
    }

    public void queueEmail(EmailQueueMessage message) {
        try {
            redisTemplate.opsForList().leftPush(EMAIL_QUEUE_KEY, message);
            log.info("Email queued successfully for user: {}, email: {}",
                    message.getUsername(), message.getTo());
        } catch (Exception e) {
            log.error("Failed to queue email for user: {}", message.getUsername(), e);
            // Even if queue fails, we don't fail the registration
        }
    }

    public EmailQueueMessage getNextEmail() {
        try {
            return (EmailQueueMessage) redisTemplate.opsForList().rightPop(EMAIL_QUEUE_KEY);
        } catch (Exception e) {
            log.error("Failed to get next email from queue", e);
            return null;
        }
    }

    public void moveToDLQ(EmailQueueMessage message) {
        try {
            redisTemplate.opsForList().leftPush(EMAIL_DLQ_KEY, message);
            log.warn("Email moved to DLQ for user: {}", message.getUsername());
        } catch (Exception e) {
            log.error("Failed to move email to DLQ for user: {}", message.getUsername(), e);
        }
    }

    public void retryEmail(EmailQueueMessage message) {
        try {
            message.setAttemptCount(message.getAttemptCount() + 1);
            message.setLastAttemptAt(LocalDateTime.now());
            redisTemplate.opsForList().leftPush(EMAIL_QUEUE_KEY, message);
            log.info("Email retry queued for user: {}, attempt: {}",
                    message.getUsername(), message.getAttemptCount());
        } catch (Exception e) {
            log.error("Failed to retry email for user: {}", message.getUsername(), e);
        }
    }

    public long getQueueSize() {
        try {
            Long size = redisTemplate.opsForList().size(EMAIL_QUEUE_KEY);
            return size != null ? size : 0;
        } catch (Exception e) {
            log.error("Failed to get queue size", e);
            return 0;
        }
    }

    public long getDLQSize() {
        try {
            Long size = redisTemplate.opsForList().size(EMAIL_DLQ_KEY);
            return size != null ? size : 0;
        } catch (Exception e) {
            log.error("Failed to get DLQ size", e);
            return 0;
        }
    }
}