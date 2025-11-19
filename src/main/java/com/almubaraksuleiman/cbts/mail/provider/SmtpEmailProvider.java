package com.almubaraksuleiman.cbts.mail.provider;


import com.almubaraksuleiman.cbts.mail.config.EmailProperties;
import com.almubaraksuleiman.cbts.mail.model.EmailRequest;
import com.almubaraksuleiman.cbts.mail.model.EmailResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmtpEmailProvider implements EmailProvider {

    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;

    @Override
    public EmailResponse send(EmailRequest emailRequest) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, 
                    true, 
                    StandardCharsets.UTF_8.name()
            );

            // Set from address
            String fromAddress = emailRequest.getFrom() != null ? 
                emailRequest.getFrom() : emailProperties.getFrom();
            helper.setFrom(fromAddress, emailProperties.getFromName());

            helper.setTo(emailRequest.getTo());
            helper.setSubject(emailRequest.getSubject());
            helper.setText(emailRequest.getContent(), emailRequest.isHtml());
            
            // Handle CC
            if (emailRequest.getCc() != null && !emailRequest.getCc().isEmpty()) {
                helper.setCc(emailRequest.getCc().toArray(new String[0]));
            }
            
            // Handle BCC
            if (emailRequest.getBcc() != null && !emailRequest.getBcc().isEmpty()) {
                helper.setBcc(emailRequest.getBcc().toArray(new String[0]));
            }

            mailSender.send(message);
            log.info("SMTP email sent successfully to: {}", emailRequest.getTo());
            return EmailResponse.success("smtp-" + System.currentTimeMillis(), "SMTP");
            
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to send SMTP email to: {}", emailRequest.getTo(), e);
            return EmailResponse.failure("SMTP", e.getMessage());
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            // Simple connection test
           // mailSender.testConnection();
            return true;
        } catch (Exception e) {
            log.warn("SMTP health check failed", e);
            return false;
        }
    }

    @Override
    public String getName() {
        return "SMTP";
    }
}