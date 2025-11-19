
package com.almubaraksuleiman.cbts.security.authentication.providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

/**
 * An abstract base class for implementing Spring Security AuthenticationProviders
 * that handle username/password authentication for different user types.
 * <p>
 * This class provides a template method pattern for authentication, delegating
 * user-specific operations to concrete subclasses. It handles the common
 * authentication flow:
 * <ol>
 *   <li>Find user by username</li>
 *   <li>Check if user account is verified</li>
 *   <li>Validate password against stored hash</li>
 *   <li>Extract user authorities/roles</li>
 *   <li>Create authentication token upon success</li>
 * </ol>
 *
 * @param <T> the type of user entity this provider authenticates
 * @author Almubarak Suleiman
 * @see AuthenticationProvider
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

public abstract class BaseAuthenticationProvider<T> implements AuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(BaseAuthenticationProvider.class);

    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs a new BaseAuthenticationProvider with the required PasswordEncoder.
     *
     * @param passwordEncoder the password encoder used to verify password matches
     */
    protected BaseAuthenticationProvider(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Finds a user entity by username. Must be implemented by concrete subclasses.
     *
     * @param username the username to search for
     * @return an Optional containing the user if found, empty Optional otherwise
     */
    protected abstract Optional<T> findUserByUsername(String username);

    /**
     * Extracts the encoded password from the user entity. Must be implemented by concrete subclasses.
     *
     * @param user the user entity
     * @return the encoded password string stored for this user
     */
    protected abstract String getPassword(T user);

    /**
     * Checks if the user account has been verified (e.g., email verification).
     * Must be implemented by concrete subclasses.
     *
     * @param user the user entity
     * @return true if the user account is verified and active, false otherwise
     */
    protected abstract boolean isVerified(T user);

    /**
     * Extracts the authorities/roles granted to the user. Must be implemented by concrete subclasses.
     *
     * @param user the user entity
     * @return a list of GrantedAuthority objects representing the user's permissions
     */
    protected abstract List<GrantedAuthority> getAuthorities(T user);

    /**
     * Performs authentication of a username and password credential.
     * Implements the core authentication logic following the template method pattern.
     *
     * @param authentication the authentication request object containing credentials
     * @return a fully authenticated Authentication object with user authorities
     * @throws BadCredentialsException if authentication fails for any reason:
     *         - User not found
     *         - Account not verified
     *         - Password doesn't match
     */
    @Override
    public Authentication authenticate(Authentication authentication) {
        // Extract credentials from the authentication request
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        logger.debug("Attempting authentication for user: {}", username);

        // Step 1: Find user by username (delegated to subclass)
        T user = findUserByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Authentication failed: User not found - {}", username);
                    return new BadCredentialsException("Invalid credentials");
                });

        // Step 2: Check if user account is verified (delegated to subclass)
        if (!isVerified(user)) {
            logger.warn("Authentication failed: Account not verified - {}", username);
            throw new BadCredentialsException("Account not verified. Please check your email.");
        }

        // Step 3: Validate password against stored hash
        if (!passwordEncoder.matches(password, getPassword(user))) {
            logger.warn("Authentication failed: Invalid password for user - {}", username);
            throw new BadCredentialsException("Invalid credentials");
        }

        // Step 4: Extract authorities and create successful authentication token
        List<GrantedAuthority> authorities = getAuthorities(user);

        logger.info("Authentication successful for user: {} with roles: {}", username,
                authorities.stream().map(GrantedAuthority::getAuthority).toList());

        return new UsernamePasswordAuthenticationToken(
                username,           // principal
                null,               // credentials are null after authentication
                authorities         // user's granted authorities/roles
        );
    }

    /**
     * Indicates that this AuthenticationProvider supports UsernamePasswordAuthenticationToken
     * authentication requests.
     *
     * @param authentication the authentication class to check
     * @return true if this provider can authenticate the given authentication type
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
