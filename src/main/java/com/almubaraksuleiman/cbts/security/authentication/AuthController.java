package com.almubaraksuleiman.cbts.security.authentication;

import com.almubaraksuleiman.cbts.mail.service.EmailService;
import com.almubaraksuleiman.cbts.mail.service.EmailVariableService;
import com.almubaraksuleiman.cbts.security.authentication.manager.AuthenticationManagerImpl;
import com.almubaraksuleiman.cbts.security.authentication.service.JwtService;
import com.almubaraksuleiman.cbts.security.authentication.service.PasswordResetService;
import com.almubaraksuleiman.cbts.security.authentication.service.RefreshTokenService;
import com.almubaraksuleiman.cbts.security.authentication.service.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Authentication Controller handling user authentication, registration, and token management.
 * Provides endpoints for login, logout, token refresh, device management, and user registration.

 * This controller serves as the main entry point for all authentication-related operations
 * in the CBTS (Computer-Based Test System).
 *
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 */
@Slf4j
@CrossOrigin("http://localhost:4200") // Allow requests from Angular development server
@RestController
@RequestMapping("/api/auth") // Base path for authentication endpoints
@RequiredArgsConstructor // Lombok: generates constructor for final fields
public class AuthController {

    private final AuthenticationManagerImpl authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final Map<String, RegistrationService> registrationServices;
    private final EmailService emailService;
    private final EmailVariableService emailVariableService;
    //private final EmailQueueService emailQueueService; // Add this

    /**
     * Authenticates a user and returns JWT tokens.
     *
     * @param payload Map containing "username" and "password" fields
     * @param request HttpServletRequest for client information extraction
     * @return ResponseEntity containing accessToken and refreshToken
     *
     * @apiNote This endpoint validates credentials and issues new tokens upon successful authentication
     * @security Uses Spring Security AuthenticationManager for credential validation

     * Example Request:
     * POST /api/auth/login
     * {
     *   "username": "john_doe",
     *   "password": "SecurePass123!"
     * }

     * Example Response:
     * {
     *   "accessToken": "eyJhbGciOiJIUzI1NiIs...",
     *   "refreshToken": "a1b2c3d4-e5f6-7890..."
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String username = payload.get("username");
        String password = payload.get("password");

        // Authenticate user credentials
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        // Generate JWT access token
        String accessToken = jwtService.generateToken(auth.getName());

        // Create refresh token with client context information
        String refreshToken = refreshTokenService.createRefreshToken(
                username,
                getClientIp(request), // Client IP for security tracking
                request.getHeader("User-Agent") // Client device information
        );

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        ));
    }

    /**
     * Refreshes an access token using a valid refresh token.
     * Implements token rotation for enhanced security.
     *
     * @param payload Map containing "refreshToken" field
     * @param request HttpServletRequest for client validation
     * @return ResponseEntity with new accessToken and rotated refreshToken
     * @throws 401 Unauthorized if refresh token is invalid or expired
     *
     * @security Implements refresh token rotation to prevent token reuse
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String rawRefreshToken = payload.get("refreshToken");

        // Verify refresh token and rotate it (security best practice)
        Optional<RefreshToken> rotated = refreshTokenService.verifyAndRotate(
                rawRefreshToken,
                getClientIp(request), // Validate client consistency
                request.getHeader("User-Agent") // Validate device consistency
        );

        if (rotated.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired refresh token"));
        }

        RefreshToken newTokenEntity = rotated.get();
        String accessToken = jwtService.generateToken(newTokenEntity.getUsername());

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", newTokenEntity.getRawToken() // New rotated token
        ));
    }

    /**
     * Logs out a user by revoking the provided refresh token.
     *
     * @param payload Map containing "refreshToken" field
     * @return ResponseEntity indicating success or failure
     *
     * @security Revokes the refresh token to prevent further use
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> payload) {
        String rawRefreshToken = payload.get("refreshToken");
        boolean revoked = refreshTokenService.revokeByRawToken(rawRefreshToken);
        if (!revoked) {
            return ResponseEntity.status(400).body(Map.of("error", "Invalid token"));
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * Retrieves all active devices/sessions for a user.
     *
     * @param username the username to query active sessions for
     * @return ResponseEntity containing list of active refresh tokens (devices)
     *
     * @security Users can only view their own active sessions
     */
    @GetMapping("/devices")
    public ResponseEntity<?> listDevices(@RequestParam String username) {
        List<RefreshToken> devices = refreshTokenService.getActiveSessions(username);
        return ResponseEntity.ok(devices);
    }

    /**
     * Revokes a specific device session by ID.
     *
     * @param id the ID of the refresh token (device session) to revoke
     * @return ResponseEntity indicating success
     *
     * @security Users can only revoke their own sessions
     */
    @DeleteMapping("/devices/{id}")
    public ResponseEntity<?> revokeDevice(@PathVariable Long id) {
        refreshTokenService.revokeById(id);
        return ResponseEntity.ok(Map.of("message", "Device session revoked"));
    }

    /**
     * Registers a new user account with form data including file upload support.
     *
     * @param firstName User's first name
     * @param lastName User's last name
     * @param middleName User's middle name (optional)
     * @param email User email address
     * @param username Desired username
     * @param password User password
     * @param role User role (ROLE_STUDENT, ROLE_ADMIN, ROLE_EXAMINER, ROLE_PROCTOR)
     * @param department User's department
     * @param profileImage Profile image file (optional)
     * @return ResponseEntity indicating success or failure
     */
    @PostMapping(value = "/register", consumes = "multipart/form-data")
    public ResponseEntity<?> register(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam(value = "middleName", required = false) String middleName,
            @RequestParam("email") String email,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("role") String role,
            @RequestParam("department") String department,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {

        try {
            // Validate required fields
            if (firstName == null || firstName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "First name is required"));
            }
            if (lastName == null || lastName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Last name is required"));
            }
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
            }
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
            }
            if (role == null || role.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Role is required"));
            }
            if (department == null || department.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Department is required"));
            }

            // Normalize role to match service naming convention
            String normalizedRole = role.toLowerCase().replace("role_", "");

            // Find the appropriate registration service based on role
            RegistrationService service = registrationServices.get(normalizedRole + "RegistrationService");
            if (service == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid role: " + role));
            }

            // Validate profile image if provided
            if (profileImage != null && !profileImage.isEmpty()) {
                // Validate file type
                String contentType = profileImage.getContentType();
                if (contentType == null ||
                        (!contentType.equals("image/jpeg") &&
                                !contentType.equals("image/png") &&
                                !contentType.equals("image/gif"))) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Profile image must be JPEG, PNG, or GIF"));
                }

                // Validate file size (max 2MB)
                if (profileImage.getSize() > 2 * 1024 * 1024) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Profile image size must be less than 2MB"));
                }
            }

            // Create registration payload with all fields
            Map<String, Object> registrationPayload = new HashMap<>();
            registrationPayload.put("firstName", firstName.trim());
            registrationPayload.put("lastName", lastName.trim());
            registrationPayload.put("email", email.trim());
            registrationPayload.put("username", username.trim());
            registrationPayload.put("password", password);
            registrationPayload.put("role", role);
            registrationPayload.put("department", department.trim());

            if (middleName != null && !middleName.trim().isEmpty()) {
                registrationPayload.put("middleName", middleName.trim());
            }

            if (profileImage != null && !profileImage.isEmpty()) {
                registrationPayload.put("profileImage", profileImage);
            }

            // Register the user with enhanced payload
            service.register(registrationPayload);

            // Generate verification token and link
            String verifyToken = jwtService.generateToken(username.trim());
            String verifyLink = "http://localhost:8080/api/auth/verify?token=" + verifyToken;

            // Build email template data
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("firstName", firstName.trim());
            templateData.put("lastName", lastName.trim());
            templateData.put("email", email.trim());
            templateData.put("username", username.trim());
            templateData.put("role", role);
            templateData.put("department", department.trim());
            templateData.put("verifyLink", verifyLink);

            // Add middle name to template data if present
            if (middleName != null && !middleName.trim().isEmpty()) {
                templateData.put("middleName", middleName.trim());
            }

            // Add global email variables from database
            templateData.putAll(emailVariableService.getAllVariables());

            // Send verification email using template
            emailService.sendTemplate(
                    email.trim(),
                    "Verify Your Account - CBTS System",
                    "registration-confirmation",
                    templateData
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Registered successfully! Please check your email for verification instructions.",
                    "username", username.trim()
            ));

        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Registration failed: "
            ));
        }
    }



    /*
     * Registers a new user account with form data including file upload support.
     * Uses email queue for async verification email delivery.
     */
//    @PostMapping(value = "/register", consumes = "multipart/form-data")
//    public ResponseEntity<?> register(
//            @RequestParam("firstName") String firstName,
//            @RequestParam("lastName") String lastName,
//            @RequestParam(value = "middleName", required = false) String middleName,
//            @RequestParam("email") String email,
//            @RequestParam("username") String username,
//            @RequestParam("password") String password,
//            @RequestParam("role") String role,
//            @RequestParam("department") String department,
//            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {
//
//        try {
//            // Validate required fields
//            if (firstName == null || firstName.trim().isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("error", "First name is required"));
//            }
//            if (lastName == null || lastName.trim().isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("error", "Last name is required"));
//            }
//            if (email == null || email.trim().isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
//            }
//            if (username == null || username.trim().isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
//            }
//            if (password == null || password.trim().isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
//            }
//            if (role == null || role.trim().isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("error", "Role is required"));
//            }
//            if (department == null || department.trim().isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("error", "Department is required"));
//            }
//
//            // Normalize role to match service naming convention
//            String normalizedRole = role.toLowerCase().replace("role_", "");
//
//            // Find the appropriate registration service based on role
//            RegistrationService service = registrationServices.get(normalizedRole + "RegistrationService");
//            if (service == null) {
//                return ResponseEntity.badRequest().body(Map.of("error", "Invalid role: " + role));
//            }
//
//            // Validate profile image if provided
//            if (profileImage != null && !profileImage.isEmpty()) {
//                // Validate file type
//                String contentType = profileImage.getContentType();
//                if (contentType == null ||
//                        (!contentType.equals("image/jpeg") &&
//                                !contentType.equals("image/png") &&
//                                !contentType.equals("image/gif"))) {
//                    return ResponseEntity.badRequest().body(Map.of("error", "Profile image must be JPEG, PNG, or GIF"));
//                }
//
//                // Validate file size (max 2MB)
//                if (profileImage.getSize() > 2 * 1024 * 1024) {
//                    return ResponseEntity.badRequest().body(Map.of("error", "Profile image size must be less than 2MB"));
//                }
//            }
//
//            // Create registration payload with all fields
//            Map<String, Object> registrationPayload = new HashMap<>();
//            registrationPayload.put("firstName", firstName.trim());
//            registrationPayload.put("lastName", lastName.trim());
//            registrationPayload.put("email", email.trim());
//            registrationPayload.put("username", username.trim());
//            registrationPayload.put("password", password);
//            registrationPayload.put("role", role);
//            registrationPayload.put("department", department.trim());
//
//            if (middleName != null && !middleName.trim().isEmpty()) {
//                registrationPayload.put("middleName", middleName.trim());
//            }
//
//            if (profileImage != null && !profileImage.isEmpty()) {
//                registrationPayload.put("profileImage", profileImage);
//            }
//
//            // Register the user with enhanced payload - SYNCHRONOUS
//            service.register(registrationPayload);
//
//            // Generate verification token and link
//            String verifyToken = jwtService.generateToken(username.trim());
//            String verifyLink = "http://localhost:8080/api/auth/verify?token=" + verifyToken;
//
//            // Build email template data
//            Map<String, Object> templateData = new HashMap<>();
//            templateData.put("firstName", firstName.trim());
//            templateData.put("lastName", lastName.trim());
//            templateData.put("email", email.trim());
//            templateData.put("username", username.trim());
//            templateData.put("role", role);
//            templateData.put("department", department.trim());
//            templateData.put("verifyLink", verifyLink);
//
//            // Add middle name to template data if present
//            if (middleName != null && !middleName.trim().isEmpty()) {
//                templateData.put("middleName", middleName.trim());
//            }
//
//            // Add global email variables from database
//            templateData.putAll(emailVariableService.getAllVariables());
//
//            // QUEUE THE EMAIL INSTEAD OF SENDING DIRECTLY
//            emailQueueService.queueVerificationEmail(
//                    email.trim(),
//                    "Verify Your Account - CBTS System",
//                    "registration-confirmation",
//                    templateData,
//                    username.trim()
//            );
//
//            return ResponseEntity.ok(Map.of(
//                    "message", "Registered successfully! Please check your email for verification instructions.",
//                    "username", username.trim()
//            ));
//
//        } catch (Exception e) {
//            // Log the exception for debugging
//            log.error("Registration failed for user: {}", username, e);
//            return ResponseEntity.status(500).body(Map.of(
//                    "error", "Registration failed: " + e.getMessage()
//            ));
//        }
//    }
    /**
     * Verifies a user account using a verification token.
     *
     * @param token JWT verification token sent via email
     * @return ResponseEntity indicating verification success or failure
     *
     * @security Token must be valid and not expired
     */
    @GetMapping("/verify")
    public ResponseEntity<?> verifyAccount(@RequestParam("token") String token) {
        try {
            String username = jwtService.extractUsername(token);

            // Attempt verification with all registration services
            // (usernames are assumed unique across all roles)
            registrationServices.values().forEach(service -> {
                try {
                    service.verifyUser(username);
                } catch (Exception ignored) {
                    // Silently continue - the correct service will handle it
                }
            });

            return ResponseEntity.ok(Map.of("message", "Account verified successfully, you can login now."));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", "Invalid or expired verification link"));
        }
    }

    /**
     * Extracts client IP address from HttpServletRequest.
     * Handles X-Forwarded-For header for proxy scenarios.
     *
     * @param request the HttpServletRequest
     * @return String representing client IP address
     *
     * @security Important for security logging and refresh token validation
     */
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr(); // Direct client IP
        }
        return xfHeader.split(",")[0]; // First IP in chain (original client)
    }




    private final PasswordResetService passwordResetService;

    /**
     * Initiates password reset process
     *
     * @param payload Map containing "email" field
     * @return ResponseEntity indicating success
     *
     * @apiNote Always returns success to prevent email enumeration
     * @security Returns same response regardless of email existence
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");

        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
        }

        // Always return success to prevent email enumeration
        passwordResetService.initiatePasswordReset(email.trim());

        return ResponseEntity.ok(Map.of(
                "message", "If the email exists, a password reset link has been sent"
        ));
    }

    /**
     * Validates a password reset token
     *
     * @param token the reset token to validate
     * @return ResponseEntity indicating token validity
     */
    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        boolean isValid = passwordResetService.isValidToken(token);

        if (!isValid) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", "Invalid or expired reset token"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Token is valid",
                "email", passwordResetService.getEmailFromToken(token)
        ));
    }

    /**
     * Resets user password using valid token
     *
     * @param payload Map containing "token" and "newPassword" fields
     * @return ResponseEntity indicating success or failure
     *
     * @security Token must be valid and not expired
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        String newPassword = payload.get("newPassword");

        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token is required"));
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "New password is required"));
        }

        if (newPassword.length() < 8) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Password must be at least 8 characters long"
            ));
        }

        boolean success = passwordResetService.resetPassword(token, newPassword);

        if (!success) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", "Password reset failed. The link may have expired."
            ));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Password has been reset successfully"
        ));
    }


}