package com.almubaraksuleiman.cbts.security.authentication.service;

import com.almubaraksuleiman.cbts.mail.service.EmailService;
import com.almubaraksuleiman.cbts.mail.service.EmailVariableService;
import com.almubaraksuleiman.cbts.security.authentication.entity.PasswordResetToken;
import com.almubaraksuleiman.cbts.security.authentication.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for handling password reset operations
 */
@Service
@RequiredArgsConstructor
public class PasswordResetService {
    
    private final PasswordResetTokenRepository tokenRepository;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final EmailVariableService emailVariableService;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, RegistrationService> registrationServices;
    
    /**
     * Initiates password reset process
     */
    @Transactional
    public boolean initiatePasswordReset(String email) {
        try {
            // Invalidate any existing tokens for this email
            tokenRepository.invalidateAllTokensForEmail(email);
            
            // Generate unique token
            String token = UUID.randomUUID().toString();
            
            // Create and save token
            PasswordResetToken resetToken = new PasswordResetToken(token, email);
            tokenRepository.save(resetToken);
            
            // Generate reset link
            String resetLink = "http://localhost:4200/auth/reset-password?token=" + token;
            
            // Build email template data
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("resetLink", resetLink);
            templateData.put("expiryHours", 1);
            templateData.put("email", email);
            
            // Add global email variables
            templateData.putAll(emailVariableService.getAllVariables());
            
            // Send password reset email
            emailService.sendTemplate(
                email,
                "Reset Your Password - CBTS System",
                "password-reset", // Email template name
                templateData
            );
            
            return true;
        } catch (Exception e) {
            // Log the error but don't reveal too much information
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Validates a password reset token
     */
    public boolean isValidToken(String token) {
        return tokenRepository.findByToken(token)
                .map(PasswordResetToken::isValid)
                .orElse(false);
    }
    
    /**
     * Resets password using valid token
     */
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        try {
            PasswordResetToken resetToken = tokenRepository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Invalid token"));
            
            // Validate token
            if (!resetToken.isValid()) {
                throw new RuntimeException("Token is expired or already used");
            }
            
            String email = resetToken.getEmail();
            
            // Find user by email across all registration services
            boolean passwordUpdated = false;
            for (RegistrationService service : registrationServices.values()) {
                try {
                    if (service.updatePasswordByEmail(email, passwordEncoder.encode(newPassword))) {
                        passwordUpdated = true;
                        break;
                    }
                } catch (Exception ignored) {
                    // Continue to next service
                }
            }
            
            if (!passwordUpdated) {
                throw new RuntimeException("User not found with email: " + email);
            }
            
            // Mark token as used
            resetToken.setUsed(true);
            tokenRepository.save(resetToken);
            
            // Send confirmation email
            sendPasswordChangedConfirmation(email);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
//    /**
//     * Sends password changed confirmation email
//     */
//    private void sendPasswordChangedConfirmation(String email) {
//        try {
//            Map<String, Object> templateData = new HashMap<>();
//            templateData.put("email", email);
//            templateData.put("timestamp", LocalDateTime.now().toString());
//
//            // Add global email variables
//            templateData.putAll(emailVariableService.getAllVariables());
//
//            emailService.sendTemplate(
//                email,
//                "Password Changed Successfully - CBTS System",
//                "password-changed-confirmation",
//                templateData
//            );
//        } catch (Exception e) {
//            // Log but don't fail the reset process if email fails
//            e.printStackTrace();
//        }
//    }

    /**
     * Sends password changed confirmation email
     */
    private void sendPasswordChangedConfirmation(String email) {
        try {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("email", email);
            templateData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            templateData.put("loginLink", "http://localhost:4200/auth/login");

            // Add global email variables
            Map<String, Object> globalVariables = emailVariableService.getAllVariables();
            templateData.putAll(globalVariables);

            emailService.sendTemplate(
                    email,
                    "Password Changed Successfully - CBTS System",
                    "password-changed-confirmation",
                    templateData
            );
        } catch (Exception e) {
            // Log but don't fail the reset process if email fails
            e.printStackTrace();
        }
    }
    
    /**
     * Cleans up expired tokens (can be called by scheduled task)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens();
    }
    
    /**
     * Gets email associated with token
     */
    public String getEmailFromToken(String token) {
        return tokenRepository.findByToken(token)
                .map(PasswordResetToken::getEmail)
                .orElse(null);
    }
}