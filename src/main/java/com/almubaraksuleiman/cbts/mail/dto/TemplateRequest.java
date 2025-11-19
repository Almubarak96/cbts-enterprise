package com.almubaraksuleiman.cbts.mail.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for admin to create/update a template in DB.
 */
@Setter
@Getter
public class TemplateRequest {
    private String name;
    private String subject;
    private String content;

}
