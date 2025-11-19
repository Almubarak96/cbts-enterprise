package com.almubaraksuleiman.cbts.security.authentication.service;

import com.almubaraksuleiman.cbts.admin.repository.AdminRepository;
import com.almubaraksuleiman.cbts.examiner.repository.ExaminerRepository;
import com.almubaraksuleiman.cbts.proctor.repository.ProctorRepository;
import com.almubaraksuleiman.cbts.student.repository.StudentRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

/**
 * Service for JWT token operations including generation, validation, and parsing.
 * Handles role-based authentication and token management for the CBTS system.
 * @Service Indicates this class is a Spring service component
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/
@Service
public class JwtService {

    // Repository dependencies for role determination
    private final AdminRepository adminRepository;
    private final ExaminerRepository examinerRepository;
    private final StudentRepository studentRepository;
    private final ProctorRepository proctorRepository;

    // JWT configuration properties
    private final long accessTokenSeconds;
    private final SecretKey key;

    /**
     * Constructs a JwtService with required dependencies and configuration.
     *
     * @param adminRepository Repository for admin user operations
     * @param examinerRepository Repository for examiner user operations
     * @param studentRepository Repository for student user operations
     * @param proctorRepository Repository for proctor user operations
     * @param secret JWT secret key for signing tokens from application properties
     * @param accessTokenSeconds Access token expiration time in seconds from application properties
     */
    public JwtService(
            AdminRepository adminRepository,
            ExaminerRepository examinerRepository,
            StudentRepository studentRepository,
            ProctorRepository proctorRepository,
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-exp-sec}") long accessTokenSeconds
    ) {
        this.adminRepository = adminRepository;
        this.examinerRepository = examinerRepository;
        this.studentRepository = studentRepository;
        this.proctorRepository = proctorRepository;
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenSeconds = accessTokenSeconds;
    }

    /**
     * Validates the structural and signature integrity of a JWT token.
     *
     * @param token The JWT token to validate
     * @return true if the token is valid and not expired, false otherwise
     * @throws JwtException if the token is malformed or signature is invalid
     */
    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Parses and validates a JWT token, returning the parsed claims.
     *
     * @param token The JWT token to parse
     * @return Jws<Claims> containing the token's header, body, and signature
     * @throws JwtException if the token is invalid, expired, or malformed
     * @throws IllegalArgumentException if the token is null or empty
     */
    public Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    /**
     * Extracts the role claim from a JWT token.
     *
     * @param token The JWT token to parse
     * @return String representing the user's role (e.g., "ROLE_ADMIN")
     * @throws JwtException if the token is invalid or doesn't contain a role claim
     */
    public String parseRole(String token) {
        return parseToken(token).getBody().get("role", String.class);
    }

    /**
     * Generates a new JWT token for a given username with appropriate role claims.
     *
     * @param username The username to include in the token subject
     * @return String containing the signed JWT token
     * @throws IllegalArgumentException if the username is not found in any repository
     */
    public String generateToken(String username) {
        String role = determineRoleForUsername(username);

        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plusSeconds(accessTokenSeconds)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Determines the appropriate role for a given username by checking all user repositories.
     *
     * @param username The username to look up
     * @return String representing the user's role (e.g., "ROLE_ADMIN")
     * @throws IllegalArgumentException if the username is not found in any repository
     *
     * @implNote Checks repositories in order: Admin → Examiner → Proctor → Student
     * @implSpec Role determination follows a specific hierarchy for user type resolution
     */
    private String determineRoleForUsername(String username) {
        // Check admin repository first
        if (adminRepository.findByUsername(username).isPresent()) {
            return "ROLE_ADMIN";
        }
        // Check examiner repository
        else if (examinerRepository.findByUsername(username).isPresent()) {
            return "ROLE_EXAMINER";
        }
        // Check proctor repository
        else if (proctorRepository.findByUsername(username).isPresent()) {
            return "ROLE_PROCTOR";
        }
        // Check student repository
        else if (studentRepository.findByUsername(username).isPresent()) {
            return "ROLE_STUDENT";
        }

        throw new IllegalArgumentException("User not found: " + username);
    }

    /**
     * Extracts the username (subject) from a JWT token.
     *
     * @param token The JWT token to parse
     * @return String containing the username from the token subject
     * @throws JwtException if the token is invalid or malformed
     */
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Extracts the role claim from a JWT token using parser directly.
     * Alternative to parseRole() method.
     *
     * @param token The JWT token to parse
     * @return String representing the user's role
     * @throws JwtException if the token is invalid or doesn't contain a role claim
     */
    public String extractRole(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }

    /**
     * Extracts the IP address claim from a JWT token.
     * Note: This assumes IP address is stored in the token during generation.
     *
     * @param token The JWT token to parse
     * @return String containing the IP address or null if not present
     * @throws JwtException if the token is invalid
     */
    public String extractIp(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("ip", String.class);
    }

    /**
     * Extracts the user agent claim from a JWT token.
     * Note: This assumes user agent is stored in the token during generation.
     *
     * @param token The JWT token to parse
     * @return String containing the user agent or null if not present
     * @throws JwtException if the token is invalid
     */
    public String extractUserAgent(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userAgent", String.class);
    }

    /**
     * Validates if a token is both valid and matches a specific username.
     *
     * @param token The JWT token to validate
     * @param username The username to verify against the token
     * @return true if the token is valid, not expired, and matches the username
     * @throws JwtException if the token is malformed or signature is invalid
     */
    public boolean isTokenValid(String token, String username) {
        return username.equals(extractUsername(token)) && !isTokenExpired(token);
    }

    /**
     * Checks if a JWT token has expired.
     *
     * @param token The JWT token to check
     * @return true if the token is expired, false otherwise
     * @throws JwtException if the token is invalid or malformed
     */
    private boolean isTokenExpired(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expiration.before(new Date());
    }
}