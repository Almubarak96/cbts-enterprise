package com.almubaraksuleiman.cbts.security.authentication.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

@Entity
@Table(name = "roles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // e.g. ROLE_ADMIN

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;
}
