package com.almubaraksuleiman.cbts.mail.queue;


import com.almubaraksuleiman.cbts.mail.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisEmailConsumer {

    private final EmailQueueService emailQueueService;
    private final EmailService emailService;
    private static final int MAX_RETRIES = 3;

//    @Scheduled(fixedDelay = 5000) // Process every 5 seconds
//    public void processQueuedEmails() {
//        try {
//            EmailQueueMessage message;
//            while ((message = emailQueueService.getNextEmail()) != null) {
//                processEmailMessage(message);
//            }
//        } catch (Exception e) {
//            log.error("Error in email consumer", e);
//        }
//    }

    private void processEmailMessage(EmailQueueMessage message) {
        log.info("Processing queued email for user: {}, attempt: {}",
                message.getUsername(), message.getAttemptCount() + 1);

        try {
            // Use your existing robust email service
            emailService.sendTemplateEmail(
                message.getTo(),
                message.getSubject(),
                message.getTemplateName(),
                message.getTemplateVariables()
            );

            log.info("Queued email sent successfully to: {}", message.getTo());

        } catch (Exception e) {
            log.error("Failed to process queued email for user: {}", message.getUsername(), e);

            // Handle retry logic
            if (message.getAttemptCount() < MAX_RETRIES) {
                // Retry with exponential backoff
                emailQueueService.retryEmail(message);
            } else {
                // Max retries exceeded, move to DLQ
                message.setLastError(e.getMessage());
                emailQueueService.moveToDLQ(message);
                log.warn("Email permanently failed after {} attempts for user: {}",
                        MAX_RETRIES, message.getUsername());
            }
        }
    }

    // Optional: Process DLQ periodically for manual intervention alerts
    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    public void monitorDLQ() {
        long dlqSize = emailQueueService.getDLQSize();
        if (dlqSize > 0) {
            log.warn("DLQ has {} failed emails requiring manual intervention", dlqSize);
            // Here you could send an alert to admins
        }
    }
}