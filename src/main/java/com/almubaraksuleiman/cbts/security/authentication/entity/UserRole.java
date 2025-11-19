package com.almubaraksuleiman.cbts.security.authentication.entity;

import jakarta.persistence.*;
import lombok.*;
/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

@Entity
@Table(name = "user_roles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserRole {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String userType; // ADMIN / EXAMINER / STUDENT

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
    private Role role;
}
