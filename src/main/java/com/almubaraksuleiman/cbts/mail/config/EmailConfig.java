package com.almubaraksuleiman.cbts.mail.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.sendgrid.SendGrid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class EmailConfig {

    private final EmailProperties emailProperties;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        EmailProperties.SmtpConfig smtp = emailProperties.getSmtp();
        
        mailSender.setHost(smtp.getHost());
        mailSender.setPort(smtp.getPort());
        mailSender.setUsername(smtp.getUsername());
        mailSender.setPassword(smtp.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", smtp.getProtocol());
        props.put("mail.smtp.auth", String.valueOf(smtp.isAuth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(smtp.isStarttls()));
        props.put("mail.smtp.connectiontimeout", smtp.getConnectionTimeout());
        props.put("mail.smtp.timeout", smtp.getTimeout());
        props.put("mail.smtp.writetimeout", smtp.getWriteTimeout());
        props.put("mail.debug", "false");

        return mailSender;
    }

    @Bean
    public SendGrid sendGrid() {
        return new SendGrid(emailProperties.getSendgrid().getApiKey());
    }

    @Bean
    public AmazonSimpleEmailService amazonSES() {
        EmailProperties.AwsSesConfig awsSes = emailProperties.getAwsSes();
        BasicAWSCredentials awsCreds = new BasicAWSCredentials(
            awsSes.getAccessKey(),
            awsSes.getSecretKey()
        );

        return AmazonSimpleEmailServiceClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withRegion(awsSes.getRegion())
                .build();
    }
}