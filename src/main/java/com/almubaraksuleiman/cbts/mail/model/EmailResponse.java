package com.almubaraksuleiman.cbts.mail.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponse {
    private boolean success;
    private String messageId;
    private String provider;
    private String errorMessage;
    private long timestamp;
    
    public static EmailResponse success(String messageId, String provider) {
        return EmailResponse.builder()
                .success(true)
                .messageId(messageId)
                .provider(provider)
                .timestamp(System.currentTimeMillis())
                .build();
    }
    
    public static EmailResponse failure(String provider, String errorMessage) {
        return EmailResponse.builder()
                .success(false)
                .provider(provider)
                .errorMessage(errorMessage)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}