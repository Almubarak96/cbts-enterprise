package com.almubaraksuleiman.cbts.security.authentication.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

@Entity
@Table(name = "permissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Permission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // e.g. CAN_CREATE_EXAM
}
