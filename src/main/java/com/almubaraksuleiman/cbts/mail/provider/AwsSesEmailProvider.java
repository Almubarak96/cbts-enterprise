package com.almubaraksuleiman.cbts.mail.provider;


import com.almubaraksuleiman.cbts.mail.config.EmailProperties;
import com.almubaraksuleiman.cbts.mail.model.EmailRequest;
import com.almubaraksuleiman.cbts.mail.model.EmailResponse;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AwsSesEmailProvider implements EmailProvider {

    private final AmazonSimpleEmailService amazonSES;
    private final EmailProperties emailProperties;

    @Override
    public EmailResponse send(EmailRequest emailRequest) {
        try {
            String fromAddress = emailRequest.getFrom() != null ? 
                emailRequest.getFrom() : emailProperties.getFrom();
            
            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(new Destination()
                            .withToAddresses(emailRequest.getTo())
                            .withCcAddresses(emailRequest.getCc())
                            .withBccAddresses(emailRequest.getBcc()))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withHtml(new Content()
                                            .withCharset("UTF-8")
                                            .withData(emailRequest.getContent()))
                                    .withText(new Content()
                                            .withCharset("UTF-8")
                                            .withData(emailRequest.getContent())))
                            .withSubject(new Content()
                                    .withCharset("UTF-8")
                                    .withData(emailRequest.getSubject())))
                    .withSource(fromAddress);

            SendEmailResult result = amazonSES.sendEmail(request);
            log.info("AWS SES email sent successfully to: {} with message ID: {}", 
                    emailRequest.getTo(), result.getMessageId());
            
            return EmailResponse.success(result.getMessageId(), "AWS_SES");
            
        } catch (Exception e) {
            log.error("Failed to send AWS SES email to: {}", emailRequest.getTo(), e);
            return EmailResponse.failure("AWS_SES", e.getMessage());
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            GetSendQuotaResult quota = amazonSES.getSendQuota(new GetSendQuotaRequest());
            return quota != null;
        } catch (Exception e) {
            log.warn("AWS SES health check failed", e);
            return false;
        }
    }

    @Override
    public String getName() {
        return "AWS_SES";
    }
}