package com.almubaraksuleiman.cbts.mail.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "email_variables")
@Data
public class EmailVariable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "variable_key", unique = true, nullable = false)
    private String key;

    @Column(name = "variable_value", nullable = false)
    private String value;

    @Column(name = "description")
    private String description;

}

