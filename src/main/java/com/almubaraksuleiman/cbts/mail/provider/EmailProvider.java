package com.almubaraksuleiman.cbts.mail.provider;


import com.almubaraksuleiman.cbts.mail.model.EmailRequest;
import com.almubaraksuleiman.cbts.mail.model.EmailResponse;

public interface EmailProvider {
    EmailResponse send(EmailRequest emailRequest);
    boolean isHealthy();
    String getName();
}