package com.almubaraksuleiman.cbts.security.authentication;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, length = 64) // SHA-256 hash length
    private String tokenHash;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private boolean revoked;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private String userAgent;

    private Instant lastUsedAt;

    @Transient
    private String rawToken; // transient for sending new token back to client after rotation
}
