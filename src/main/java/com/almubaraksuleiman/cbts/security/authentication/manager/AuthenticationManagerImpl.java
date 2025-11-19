
package com.almubaraksuleiman.cbts.security.authentication.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.List;

/**
 * Composite AuthenticationManager implementation that delegates authentication
 * to multiple AuthenticationProviders in sequence.

 * This manager supports multiple user types (Admin, Examiner, Proctor, Student)
 * by trying each provider until one successfully authenticates the user or all fail.

 * Implements the composite pattern for authentication provider management.
 *
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 */
public class AuthenticationManagerImpl implements AuthenticationManager {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationManagerImpl.class);
    private final List<AuthenticationProvider> providers;

    /**
     * Constructs a new AuthenticationManagerImpl with the specified providers.
     *
     * @param providers the list of AuthenticationProviders to delegate to
     * @throws IllegalArgumentException if providers list is null or empty
     */
    public AuthenticationManagerImpl(List<AuthenticationProvider> providers) {
        if (providers == null || providers.isEmpty()) {
            throw new IllegalArgumentException("Providers list cannot be null or empty");
        }

        this.providers = providers;
        logger.info("AuthenticationManager initialized with {} providers: {}",
                providers.size(),
                getProviderNames());
    }

    /**
     * Attempts to authenticate the provided Authentication object by delegating
     * to each registered AuthenticationProvider in sequence.

     * The authentication process:
     * 1. Iterates through all providers that support the authentication type
     * 2. Stops at the first provider that successfully authenticates
     * 3. Returns the authenticated Authentication object
     * 4. If all providers fail, throws the last exception received
     *
     * @param authentication the authentication request object
     * @return Authentication the fully authenticated object
     * @throws AuthenticationException if authentication fails for all providers
     *
     * @security Implements fail-fast behavior for authentication attempts
     * @performance Providers are checked in registration order, so order matters
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        AuthenticationException lastException = null;
        String username = authentication.getName();

        logger.debug("Attempting authentication for user: {}", username);

        for (AuthenticationProvider provider : providers) {
            String providerName = provider.getClass().getSimpleName();

            if (provider.supports(authentication.getClass())) {
                logger.debug("Trying authentication with provider: {}", providerName);

                try {
                    Authentication result = provider.authenticate(authentication);

                    if (result != null && result.isAuthenticated()) {
                        logger.info("Authentication successful for user: {} using provider: {}",
                                username, providerName);
                        return result;
                    }
                } catch (AuthenticationException ex) {
                    logger.debug("Authentication failed with provider {}: {}",
                            providerName, ex.getMessage());
                    lastException = ex;
                    // Continue to next provider
                }
            } else {
                logger.debug("Provider {} does not support authentication type: {}",
                        providerName, authentication.getClass().getSimpleName());
            }
        }

        // All providers failed
        logger.warn("Authentication failed for user: {} - All providers failed", username);

        throw (lastException != null) ? lastException :
                new BadCredentialsException("Authentication failed for all providers");
    }

    /**
     * Returns the number of authentication providers registered.
     *
     * @return int the number of providers
     */
    public int getProviderCount() {
        return providers.size();
    }

    /**
     * Returns a list of provider class names for logging and debugging.
     *
     * @return List<String> the simple class names of all providers
     */
    private List<String> getProviderNames() {
        return providers.stream()
                .map(provider -> provider.getClass().getSimpleName())
                .toList();
    }

    /**
     * Optional: Check if a specific provider type is registered.
     *
     * @param providerClass the provider class to check for
     * @return boolean true if a provider of the specified type is registered
     */
    public boolean hasProvider(Class<?> providerClass) {
        return providers.stream()
                .anyMatch(providerClass::isInstance);
    }

    /**
     * Optional: Get a provider by its type.
     *
     * @param <T> the provider type
     * @param providerClass the provider class to retrieve
     * @return Optional<T> the provider if found, empty otherwise
     */
    @SuppressWarnings("unchecked")
    public <T extends AuthenticationProvider> java.util.Optional<T> getProvider(Class<T> providerClass) {
        return providers.stream()
                .filter(providerClass::isInstance)
                .map(provider -> (T) provider)
                .findFirst();
    }
}