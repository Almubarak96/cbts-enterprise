package com.almubaraksuleiman.cbts.mail.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAttachment {
    private String filename;
    private byte[] content;
    private String contentType;
}