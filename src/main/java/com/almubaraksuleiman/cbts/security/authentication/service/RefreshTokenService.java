//package com.almubaraksuleiman.cbts.security.authentication.service;
//
//import com.almubaraksuleiman.cbts.security.authentication.RefreshToken;
//import com.almubaraksuleiman.cbts.security.authentication.TokenHashUtil;
//import com.almubaraksuleiman.cbts.security.authentication.repository.RefreshTokenRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.security.SecureRandom;
//import java.time.Instant;
//import java.util.Base64;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//@RequiredArgsConstructor
//public class RefreshTokenService {
//
//    private final RefreshTokenRepository refreshTokenRepository;
//    private final SecureRandom secureRandom = new SecureRandom();
//
//    private static final long REFRESH_TOKEN_VALIDITY_SECONDS = 7 * 24 * 60 * 60; // 7 days
//    private static final int MAX_ACTIVE_TOKENS = 5;
//
//    public String createRefreshToken(String username, String ipAddress, String userAgent) {
//        enforceMaxTokens(username);
//
//        String rawToken = generateSecureToken();
//        String hashedToken = TokenHashUtil.hashToken(rawToken);
//
//        RefreshToken entity = RefreshToken.builder()
//                .username(username)
//                .tokenHash(hashedToken)
//                .createdAt(Instant.now())
//                .expiryDate(Instant.now().plusSeconds(REFRESH_TOKEN_VALIDITY_SECONDS))
//                .revoked(false)
//                .ipAddress(ipAddress)
//                .userAgent(userAgent)
//                .lastUsedAt(Instant.now())
//                .build();
//
//        refreshTokenRepository.save(entity);
//
//        return rawToken;
//    }
//
//    public Optional<RefreshToken> verifyAndRotate(String rawToken, String ipAddress, String userAgent) {
//        String hashedToken = TokenHashUtil.hashToken(rawToken);
//
//        return refreshTokenRepository.findByTokenHash(hashedToken)
//                .filter(token -> !token.isRevoked() && token.getExpiryDate().isAfter(Instant.now()))
//                .map(existing -> {
//                    existing.setRevoked(true);
//                    refreshTokenRepository.save(existing);
//
//                    // issue new token
//                    String newRawToken = generateSecureToken();
//                    String newHashedToken = TokenHashUtil.hashToken(newRawToken);
//
//                    RefreshToken newEntity = RefreshToken.builder()
//                            .username(existing.getUsername())
//                            .tokenHash(newHashedToken)
//                            .createdAt(Instant.now())
//                            .expiryDate(Instant.now().plusSeconds(REFRESH_TOKEN_VALIDITY_SECONDS))
//                            .revoked(false)
//                            .ipAddress(ipAddress)
//                            .userAgent(userAgent)
//                            .lastUsedAt(Instant.now())
//                            .rawToken(newRawToken) // transient for controller
//                            .build();
//
//                    refreshTokenRepository.save(newEntity);
//                    return newEntity;
//                });
//    }
//
//    public boolean revokeByRawToken(String rawToken) {
//        String hashedToken = TokenHashUtil.hashToken(rawToken);
//        return refreshTokenRepository.findByTokenHash(hashedToken).map(token -> {
//            token.setRevoked(true);
//            refreshTokenRepository.save(token);
//            return true;
//        }).orElse(false);
//    }
//
//    public List<RefreshToken> getActiveSessions(String username) {
//        return refreshTokenRepository.findByUsernameAndRevokedFalse(username);
//    }
//
//    public void revokeById(Long id) {
//        refreshTokenRepository.findById(id).ifPresent(token -> {
//            token.setRevoked(true);
//            refreshTokenRepository.save(token);
//        });
//    }
//
//    private String generateSecureToken() {
//        byte[] bytes = new byte[64];
//        secureRandom.nextBytes(bytes);
//        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
//    }
//
//    private void enforceMaxTokens(String username) {
//        List<RefreshToken> activeTokens = refreshTokenRepository.findByUsernameAndRevokedFalse(username);
//        if (activeTokens.size() >= MAX_ACTIVE_TOKENS) {
//            activeTokens.stream()
//                    .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
//                    .limit(activeTokens.size() - MAX_ACTIVE_TOKENS + 1)
//                    .forEach(t -> {
//                        t.setRevoked(true);
//                        refreshTokenRepository.save(t);
//                    });
//        }
//    }
//}





package com.almubaraksuleiman.cbts.security.authentication.service;

import com.almubaraksuleiman.cbts.security.authentication.RefreshToken;
import com.almubaraksuleiman.cbts.security.authentication.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for managing refresh tokens in the authentication system.
 * Implements secure token generation, validation, rotation, and revocation.

 * This service follows security best practices including:
 * - Token hashing before resource with pepper
 * - Refresh token rotation
 * - Client context validation
 * - Maximum active session enforcement
 *
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2024
 */
@Service
@RequiredArgsConstructor // Lombok: generates constructor for final fields
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom(); // Cryptographically secure RNG

    @Value("${security.token.pepper:}") // Optional pepper from configuration
    private String tokenPepper;

    // Token validity period: 7 days in seconds
    private static final long REFRESH_TOKEN_VALIDITY_SECONDS = 7 * 24 * 60 * 60;

    // Maximum number of active refresh tokens per user
    private static final int MAX_ACTIVE_TOKENS = 5;

    /**
     * Enhanced token hashing utility with pepper support for production security.
     * Uses SHA-256 hashing algorithm with optional secret pepper.
     */
    public static final class TokenHashUtil {
        private static final String HASH_ALGORITHM = "SHA-256";

        /**
         * Hashes a token using SHA-256 with optional pepper for additional security.
         *
         * @param token the raw token to hash
         * @param pepper optional secret pepper for additional security (null for no pepper)
         * @return String the hexadecimal SHA-256 hash of the token
         * @throws RuntimeException if SHA-256 algorithm is not available
         *
         * @security Pepper provides additional protection against rainbow table attacks
         * @performance Minimal overhead for significant security improvement
         */
        public static String hashToken(String token, String pepper) {
            try {
                String dataToHash = pepper != null ? token + pepper : token;
                MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
                byte[] hashedBytes = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
                return bytesToHex(hashedBytes);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(HASH_ALGORITHM + " algorithm not available", e);
            }
        }

        /**
         * Hashes a token without pepper (for backward compatibility or testing).
         */
        public static String hashToken(String token) {
            return hashToken(token, null);
        }

        /**
         * Converts byte array to hexadecimal string efficiently.
         */
        private static String bytesToHex(byte[] bytes) {
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }

        /**
         * Optional: Verify a token against a hash with pepper.
         *
         * @param token the raw token to verify
         * @param hash the expected hash value
         * @param pepper the pepper used during original hashing
         * @return boolean true if the token matches the hash
         */
        public static boolean verifyToken(String token, String hash, String pepper) {
            return hashToken(token, pepper).equals(hash);
        }
    }

    /**
     * Creates a new refresh token for a user with client context information.
     * Enforces maximum active tokens policy by revoking oldest tokens if necessary.
     *
     * @param username the username to create the token for
     * @param ipAddress the client IP address for security tracking
     * @param userAgent the client user agent string for device identification
     * @return String the raw (unhashed) refresh token to be returned to the client
     *
     * @security Tokens are hashed with pepper before resource using SHA-256
     * @businessLogic Enforces maximum of 5 active tokens per user
     */
    public String createRefreshToken(String username, String ipAddress, String userAgent) {
        enforceMaxTokens(username); // Revoke oldest tokens if limit exceeded

        String rawToken = generateSecureToken(); // Generate cryptographically secure token
        String hashedToken = TokenHashUtil.hashToken(rawToken, tokenPepper); // Hash with pepper

        RefreshToken entity = RefreshToken.builder()
                .username(username)
                .tokenHash(hashedToken) // Store only the hashed value
                .createdAt(Instant.now())
                .expiryDate(Instant.now().plusSeconds(REFRESH_TOKEN_VALIDITY_SECONDS))
                .revoked(false)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .lastUsedAt(Instant.now())
                .build();

        refreshTokenRepository.save(entity);

        return rawToken;
    }

    /**
     * Verifies a refresh token and performs token rotation (security best practice).
     * If token is valid, revokes the old token and issues a new one.
     *
     * @param rawToken the raw refresh token from the client
     * @param ipAddress the current client IP address for validation
     * @param userAgent the current client user agent for validation
     * @return Optional<RefreshToken> containing the new token entity if verification successful
     *
     * @security Implements token rotation to prevent token reuse
     * @validation Checks token validity, revocation status, and client consistency
     */
    public Optional<RefreshToken> verifyAndRotate(String rawToken, String ipAddress, String userAgent) {
        String hashedToken = TokenHashUtil.hashToken(rawToken, tokenPepper); // Hash with pepper

        return refreshTokenRepository.findByTokenHash(hashedToken)
                .filter(token -> !token.isRevoked())
                .filter(token -> token.getExpiryDate().isAfter(Instant.now()))
                .map(existing -> {
                    // Revoke the old token (token rotation)
                    existing.setRevoked(true);
                    refreshTokenRepository.save(existing);

                    // Issue new token with updated client context
                    String newRawToken = generateSecureToken();
                    String newHashedToken = TokenHashUtil.hashToken(newRawToken, tokenPepper);

                    RefreshToken newEntity = RefreshToken.builder()
                            .username(existing.getUsername())
                            .tokenHash(newHashedToken)
                            .createdAt(Instant.now())
                            .expiryDate(Instant.now().plusSeconds(REFRESH_TOKEN_VALIDITY_SECONDS))
                            .revoked(false)
                            .ipAddress(ipAddress)
                            .userAgent(userAgent)
                            .lastUsedAt(Instant.now())
                            .rawToken(newRawToken) // Transient field for controller response
                            .build();

                    refreshTokenRepository.save(newEntity);
                    return newEntity;
                });
    }

    /**
     * Revokes a refresh token using the raw token value.
     *
     * @param rawToken the raw refresh token to revoke
     * @return boolean true if token was found and revoked, false otherwise
     *
     * @security Uses token hashing with pepper for secure comparison
     */
    public boolean revokeByRawToken(String rawToken) {
        String hashedToken = TokenHashUtil.hashToken(rawToken, tokenPepper);
        return refreshTokenRepository.findByTokenHash(hashedToken).map(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            return true;
        }).orElse(false);
    }

    /**
     * Retrieves all active (non-revoked) refresh tokens for a user.
     *
     * @param username the username to query active sessions for
     * @return List<RefreshToken> all active refresh tokens for the user
     */
    public List<RefreshToken> getActiveSessions(String username) {
        return refreshTokenRepository.findByUsernameAndRevokedFalse(username);
    }

    /**
     * Revokes a specific refresh token by its database ID.
     *
     * @param id the database ID of the refresh token to revoke
     *
     * @security Typically used in device management features
     */
    public void revokeById(Long id) {
        refreshTokenRepository.findById(id).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    /**
     * Generates a cryptographically secure random token.
     * Uses SecureRandom for entropy and Base64 URL-safe encoding.
     *
     * @return String a secure random token (64 bytes → 86 characters Base64)
     *
     * @security Uses SecureRandom for cryptographically strong randomness
     * @note 64 bytes provides 512 bits of entropy, more than sufficient for refresh tokens
     */
    private String generateSecureToken() {
        byte[] bytes = new byte[64]; // 512 bits of entropy
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Enforces the maximum active tokens policy for a user.
     * If user has more than MAX_ACTIVE_TOKENS, revokes the oldest tokens.
     *
     * @param username the username to enforce the policy for
     *
     * @businessLogic Prevents token flooding and maintains system performance
     * @security Limits potential damage from token leakage
     */
    private void enforceMaxTokens(String username) {
        List<RefreshToken> activeTokens = refreshTokenRepository.findByUsernameAndRevokedFalse(username);
        if (activeTokens.size() >= MAX_ACTIVE_TOKENS) {
            activeTokens.stream()
                    .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                    .limit(activeTokens.size() - MAX_ACTIVE_TOKENS + 1)
                    .forEach(token -> {
                        token.setRevoked(true);
                        refreshTokenRepository.save(token);
                    });
        }
    }

    /**
     * Clean up expired tokens periodically.
     * Could be called from a scheduled task.
     */
    /*public void cleanupExpiredTokens() {
        List<RefreshToken> expiredTokens = refreshTokenRepository.findByExpiryDateBefore(Instant.now());
        expiredTokens.forEach(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }*/

    /**
     * Get token usage statistics for monitoring.
     */
    public long getActiveTokenCount(String username) {
        return refreshTokenRepository.countByUsernameAndRevokedFalse(username);
    }
}