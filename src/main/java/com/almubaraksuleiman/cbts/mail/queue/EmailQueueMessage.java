package com.almubaraksuleiman.cbts.mail.queue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailQueueMessage {
    private String id;
    private String to;
    private String subject;
    private String templateName;
    private Map<String, Object> templateVariables;
    private String from;

    // Queue tracking
    private int attemptCount;
    private LocalDateTime firstAttemptAt;
    private LocalDateTime lastAttemptAt;
    private String lastError;

    // For tracking and resend functionality
    private String username; // User who triggered the email
    private String emailType; // "VERIFICATION", "PASSWORD_RESET", etc.
}