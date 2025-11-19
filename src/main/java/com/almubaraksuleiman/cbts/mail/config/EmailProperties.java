package com.almubaraksuleiman.cbts.mail.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.email")
public class EmailProperties {
    private Provider provider = Provider.SMTP;
    private String from;
    private String fromName;
    private int maxRetryAttempts = 3;
    private long retryDelayMs = 1000;
    
    private SmtpConfig smtp = new SmtpConfig();
    private SendGridConfig sendgrid = new SendGridConfig();
    private AwsSesConfig awsSes = new AwsSesConfig();
    
    public enum Provider {
        SMTP, SENDGRID, AWS_SES
    }
    
    @Data
    public static class SmtpConfig {
        private String host;
        private int port = 587;
        private String username;
        private String password;
        private String protocol = "smtp";
        private boolean auth = true;
        private boolean starttls = true;
        private String connectionTimeout = "5000";
        private String timeout = "5000";
        private String writeTimeout = "5000";
    }
    
    @Data
    public static class SendGridConfig {
        private String apiKey;
    }
    
    @Data
    public static class AwsSesConfig {
        private String accessKey;
        private String secretKey;
        private String region = "us-east-1";
    }
}