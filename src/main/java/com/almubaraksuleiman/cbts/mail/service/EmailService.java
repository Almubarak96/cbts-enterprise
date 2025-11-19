package com.almubaraksuleiman.cbts.mail.service;



import com.almubaraksuleiman.cbts.mail.model.EmailRequest;
import com.almubaraksuleiman.cbts.mail.model.EmailResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface EmailService {
    
    // Single email sending
    EmailResponse sendEmail(EmailRequest emailRequest);
    CompletableFuture<EmailResponse> sendEmailAsync(EmailRequest emailRequest);
    
    // Bulk email sending
    List<EmailResponse> sendBulkEmails(List<EmailRequest> emailRequests);
    CompletableFuture<List<EmailResponse>> sendBulkEmailsAsync(List<EmailRequest> emailRequests);
    
    // Template-based email sending
    EmailResponse sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables);
    
    // Provider health check
    boolean isHealthy();
    String getProviderName();
    
    // Backward compatibility with existing code
    void sendHtml(String to, String subject, String htmlContent);
    void sendTemplate(String to, String subject, String templateName, Map<String, Object> variables);
}