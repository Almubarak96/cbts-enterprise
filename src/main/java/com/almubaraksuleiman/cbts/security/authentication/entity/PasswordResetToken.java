package com.almubaraksuleiman.cbts.security.authentication.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for storing password reset tokens
 */
@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
public class PasswordResetToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false)
    private LocalDateTime expiryDate;
    
    @Column(nullable = false)
    private boolean used = false;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    public PasswordResetToken(String token, String email) {
        this.token = token;
        this.email = email;
        this.createdAt = LocalDateTime.now();
        this.expiryDate = LocalDateTime.now().plusHours(1); // 1 hour expiry
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
    
    public boolean isValid() {
        return !used && !isExpired();
    }
}