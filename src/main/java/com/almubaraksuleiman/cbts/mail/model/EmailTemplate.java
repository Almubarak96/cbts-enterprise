package com.almubaraksuleiman.cbts.mail.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity to store editable templates in the DB.
 * Useful for admins to edit template content via UI.
 */
@Setter
@Getter
@Entity
@Table(name = "email_templates")
public class EmailTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // logical name like 'reset-password', unique per institution if multi-tenant
    @Column(nullable = false, unique = true, length = 150)
    private String name;

    // optional subject line
    @Column(length = 250)
    private String subject;

    // template content (HTML)
    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private boolean active = true;


    // store path relative to templates dir
    @Column(length = 500)
    private String path;


}
