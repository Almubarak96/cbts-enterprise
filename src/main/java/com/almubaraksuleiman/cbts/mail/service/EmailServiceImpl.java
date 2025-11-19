package com.almubaraksuleiman.cbts.mail.service;



import com.almubaraksuleiman.cbts.mail.config.EmailProperties;
import com.almubaraksuleiman.cbts.mail.model.EmailRequest;
import com.almubaraksuleiman.cbts.mail.model.EmailResponse;
import com.almubaraksuleiman.cbts.mail.provider.EmailProvider;
import com.almubaraksuleiman.cbts.mail.templates.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final Map<String, EmailProvider> emailProviders;
    private final EmailProperties emailProperties;
    private final TemplateService templateService;

    @Override
    @Retryable(
        value = {Exception.class},
        maxAttemptsExpression = "${app.email.max-retry-attempts:3}",
        backoff = @Backoff(delayExpression = "${app.email.retry-delay-ms:1000}")
    )
    public EmailResponse sendEmail(EmailRequest emailRequest) {
        try {
            EmailProvider provider = getActiveProvider();
            log.debug("Sending email via {} to: {}", provider.getName(), emailRequest.getTo());

            return provider.send(emailRequest);

        } catch (Exception e) {
            log.error("Failed to send email after retries to: {}", emailRequest.getTo(), e);
            return EmailResponse.failure("SYSTEM", e.getMessage());
        }
    }

    @Async
    @Override
    public CompletableFuture<EmailResponse> sendEmailAsync(EmailRequest emailRequest) {
        return CompletableFuture.completedFuture(sendEmail(emailRequest));
    }

    @Override
    public List<EmailResponse> sendBulkEmails(List<EmailRequest> emailRequests) {
        return emailRequests.parallelStream()
                .map(this::sendEmail)
                .collect(Collectors.toList());
    }

    @Async
    @Override
    public CompletableFuture<List<EmailResponse>> sendBulkEmailsAsync(List<EmailRequest> emailRequests) {
        return CompletableFuture.completedFuture(sendBulkEmails(emailRequests));
    }

    @Override
    public EmailResponse sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        String htmlContent = templateService.renderToHtml(templateName, variables);

        EmailRequest emailRequest = EmailRequest.builder()
                .to(to)
                .subject(subject)
                .content(htmlContent)
                .isHtml(true)
                .templateName(templateName)
                .templateVariables(variables)
                .build();

        return sendEmail(emailRequest);
    }

    @Override
    public boolean isHealthy() {
        return getActiveProvider().isHealthy();
    }

    @Override
    public String getProviderName() {
        return getActiveProvider().getName();
    }

    // Backward compatibility methods
    @Override
    public void sendHtml(String to, String subject, String htmlContent) {
        EmailRequest emailRequest = EmailRequest.builder()
                .to(to)
                .subject(subject)
                .content(htmlContent)
                .isHtml(true)
                .build();

        EmailResponse response = sendEmail(emailRequest);
        if (!response.isSuccess()) {
            throw new RuntimeException("Failed to send email: " + response.getErrorMessage());
        }
    }

    @Override
    public void sendTemplate(String to, String subject, String templateName, Map<String, Object> variables) {
        EmailResponse response = sendTemplateEmail(to, subject, templateName, variables);
        if (!response.isSuccess()) {
            throw new RuntimeException("Failed to send template email: " + templateName + "  for " + to + response.getErrorMessage());
        }
    }

    private EmailProvider getActiveProvider() {
        String providerBeanName = emailProperties.getProvider().name().toLowerCase() + "EmailProvider";
        EmailProvider provider = emailProviders.get(providerBeanName);

        if (provider == null) {
            throw new IllegalStateException("No email provider configured for: " + emailProperties.getProvider());
        }

        return provider;
    }
}