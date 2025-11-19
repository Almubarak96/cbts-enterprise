//package com.almubaraksuleiman.cbts.security.authentication;
//
//import com.almubaraksuleiman.cbts.security.authentication.service.JwtService;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//import java.io.IOException;
//
//@Component
//public class JwtAuthenticationFilter extends org.springframework.web.filter.OncePerRequestFilter {
//
//    private final JwtService jwtService;
//
//    public JwtAuthenticationFilter(JwtService jwtService) {
//        this.jwtService = jwtService;
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        String authHeader = request.getHeader("Authorization");
//
//        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
//            String token = authHeader.substring(7);
//
//            if (jwtService.isTokenValid(token)) {
//                String username = jwtService.extractUsername(token);
//                String role = jwtService.parseRole(token); // We'll add this helper in JwtService
//
//                UsernamePasswordAuthenticationToken auth =
//                        new UsernamePasswordAuthenticationToken(username, null,
//                                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(role)));
//
//                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(auth);
//            }
//        }
//
//        filterChain.doFilter(request, response);
//    }
//}


package com.almubaraksuleiman.cbts.security.authentication;

import com.almubaraksuleiman.cbts.security.authentication.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * A Spring Security filter that intercepts incoming HTTP requests to authenticate users
 * using a JSON Web Token (JWT) provided in the 'Authorization' header.
 * <p>
 * This filter is applied once per request and is responsible for:
 * <ul>
 *   <li>Extracting the JWT from the 'Authorization: Bearer <token>' header</li>
 *   <li>Validating the token's signature and expiration</li>
 *   <li>Extracting user identity (username) and authorities (roles) from a valid token</li>
 *   <li>Setting the authentication in the Spring Security context for the current request</li>
 * </ul>
 * If no token is present or validation fails, the request continues down the filter chain
 * without authentication and will be handled by downstream security rules.

 * @see OncePerRequestFilter
 * @see JwtService
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /**
     * The standard prefix for a JWT in the Authorization header, as per the Bearer Token specification.
     */
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    /**
     * Constructs a new JwtAuthenticationFilter with the required JwtService.
     *
     * @param jwtService the service responsible for JWT validation and parsing.
     */
    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Internal filter method called for every request. Extracts and validates a JWT token
     * from the Authorization header to authenticate the user.
     *
     * @param request     the incoming HTTP request.
     * @param response    the HTTP response.
     * @param filterChain the chain of filters to proceed with if authentication is successful or skipped.
     * @throws ServletException if a servlet-specific error occurs.
     * @throws IOException      if an I/O error occurs during request processing.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Extract the 'Authorization' header from the HTTP request
        String authHeader = request.getHeader("Authorization");

        // Check if the header contains text and starts with the expected "Bearer " prefix
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            // Extract the raw token by removing the "Bearer " prefix and any surrounding whitespace
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();

            // Proceed only if the extracted token is not empty
            if (StringUtils.hasText(token)) {
                try {
                    // Delegate token validation (signature, expiration) to the JwtService
                    if (jwtService.isTokenValid(token)) {
                        // Extract the username (subject) from the validated token
                        String username = jwtService.extractUsername(token);
                        // Extract the user's role from the token claims
                        String role = jwtService.parseRole(token);

                        /*
                         * Check if the SecurityContext is not already authenticated.
                         * This is an optimization to avoid unnecessary re-authentication.
                         */
                        if (SecurityContextHolder.getContext().getAuthentication() == null) {
                            logger.debug("Creating authentication context for user: '{}'", username);

                            // Create an authentication token with the user's identity and authorities
                            UsernamePasswordAuthenticationToken authenticationToken =
                                    new UsernamePasswordAuthenticationToken(
                                            username, // principal
                                            null,     // credentials are set to null as they are not needed after authentication
                                            List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(role)) // authorities
                                    );

                            // Add request details (like remote IP address and session ID) to the authentication object
                            authenticationToken.setDetails(
                                    new WebAuthenticationDetailsSource().buildDetails(request)
                            );

                            // Set the fully authenticated user into the SecurityContext for this thread
                            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                            logger.info("Successfully authenticated user: '{}' with role: '{}'", username, role);
                        }
                    } else {
                        // Token was present but is invalid (expired, malformed, etc.)
                        logger.warn("JWT validation failed for the provided token.");
                    }
                } catch (Exception e) {
                    // Log any unexpected exceptions during token validation or parsing
                    logger.error("An unexpected error occurred during JWT validation for token: {}", token, e);
                    // Note: We do not set the response status here to allow the request to proceed.
                    // Downstream security filters (e.g., AnonymousAuthenticationFilter) will handle it.
                }
            }
        } else {
            // Log at debug level as it's common for some endpoints to not have an Authorization header
            logger.debug("No Bearer token found in Authorization header. Proceeding anonymously.");
        }

        // It is CRUCIAL to continue the filter chain in all cases.
        // If authentication failed, subsequent filters will handle the unauthenticated request.
        filterChain.doFilter(request, response);
    }
}
