package com.almubaraksuleiman.cbts.mail.provider;


import com.almubaraksuleiman.cbts.mail.config.EmailProperties;
import com.almubaraksuleiman.cbts.mail.model.EmailRequest;
import com.almubaraksuleiman.cbts.mail.model.EmailResponse;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendGridEmailProvider implements EmailProvider {

    private final SendGrid sendGrid;
    private final EmailProperties emailProperties;

    @Override
    public EmailResponse send(EmailRequest emailRequest) {
        try {
            String fromAddress = emailRequest.getFrom() != null ? 
                emailRequest.getFrom() : emailProperties.getFrom();
            
            Email from = new Email(fromAddress, emailProperties.getFromName());
            Email to = new Email(emailRequest.getTo());
            Content content = new Content(
                emailRequest.isHtml() ? "text/html" : "text/plain", 
                emailRequest.getContent()
            );
            
            Mail mail = new Mail(from, emailRequest.getSubject(), to, content);
            
            // Handle CC
            if (emailRequest.getCc() != null) {
                for (String ccEmail : emailRequest.getCc()) {
                   // mail.addCc(new Email(ccEmail));
                }
            }
            
            // Handle BCC
            if (emailRequest.getBcc() != null) {
                for (String bccEmail : emailRequest.getBcc()) {
                    // mail.addBcc(new Email(bccEmail));
                }
            }
            
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sendGrid.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("SendGrid email sent successfully to: {}", emailRequest.getTo());
                return EmailResponse.success(getMessageIdFromHeaders(response), "SendGrid");
            } else {
                log.error("SendGrid API error: {} - {}", response.getStatusCode(), response.getBody());
                return EmailResponse.failure("SendGrid", "HTTP " + response.getStatusCode() + ": " + response.getBody());
            }
            
        } catch (IOException e) {
            log.error("Failed to send SendGrid email to: {}", emailRequest.getTo(), e);
            return EmailResponse.failure("SendGrid", e.getMessage());
        }
    }

    private String getMessageIdFromHeaders(Response response) {
        // Extract message ID from response headers
        return response.getHeaders().get("X-Message-Id");
    }

    @Override
    public boolean isHealthy() {
        try {
            Request request = new Request();
            request.setMethod(Method.GET);
            request.setEndpoint("user/account");
            Response response = sendGrid.api(request);
            return response.getStatusCode() == 200;
        } catch (Exception e) {
            log.warn("SendGrid health check failed", e);
            return false;
        }
    }

    @Override
    public String getName() {
        return "SendGrid";
    }
}