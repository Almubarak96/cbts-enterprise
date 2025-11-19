package com.almubaraksuleiman.cbts.mail.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    private String to;
    @Builder.Default
    private List<String> cc = new ArrayList<>();
    @Builder.Default
    private List<String> bcc = new ArrayList<>();
    private String subject;
    private String content;
    private boolean isHtml;
    private String templateName;
    private Map<String, Object> templateVariables;
    @Builder.Default
    private List<EmailAttachment> attachments = new ArrayList<>();
    private String from;
}