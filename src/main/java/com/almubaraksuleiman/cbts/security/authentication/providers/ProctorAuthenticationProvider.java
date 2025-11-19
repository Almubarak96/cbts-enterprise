package com.almubaraksuleiman.cbts.security.authentication.providers;

import com.almubaraksuleiman.cbts.proctor.model.Proctor;
import com.almubaraksuleiman.cbts.proctor.repository.ProctorRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * AuthenticationProvider implementation for Proctor users.
 * Extends BaseAuthenticationProvider to handle proctor-specific authentication logic.
 * <p>
 * This component is responsible for:
 * <ul>
 *   <li>Finding proctor users by username</li>
 *   <li>Verifying proctor account status</li>
 *   <li>Providing proctor-specific authorities (ROLE_PROCTOR)</li>
 * </ul>
 *
 * @author Almubarak Suleiman
 * @see BaseAuthenticationProvider
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/


@Component
public class ProctorAuthenticationProvider extends BaseAuthenticationProvider<Proctor> {

    private final ProctorRepository proctorRepository;

    /**
     * Constructs a new ProctorAuthenticationProvider with required dependencies.
     *
     * @param proctorRepository the repository for accessing proctor data
     * @param passwordEncoder the password encoder for verifying credentials
     */
    public ProctorAuthenticationProvider(ProctorRepository proctorRepository, PasswordEncoder passwordEncoder) {
        // Pass the password encoder to the parent base class
        super(passwordEncoder);
        this.proctorRepository = proctorRepository;
    }

    /**
     * Finds a proctor user by username using the ProctorRepository.
     *
     * @param username the username to search for
     * @return an Optional containing the Proctor if found, empty Optional otherwise
     */
    @Override
    protected Optional<Proctor> findUserByUsername(String username) {
        return proctorRepository.findByUsername(username);
    }

    /**
     * Extracts the encoded password from the Proctor entity.
     *
     * @param proctor the Proctor entity
     * @return the encoded password string
     */
    @Override
    protected String getPassword(Proctor proctor) {
        return proctor.getPassword();
    }

    /**
     * Checks if the proctor account has been verified and is active.
     * This implementation uses the isVerified() method from the Proctor entity.
     *
     * @param proctor the Proctor entity
     * @return true if the proctor account is verified, false otherwise
     */
    @Override
    protected boolean isVerified(Proctor proctor) {
        return proctor.isVerified();
    }

    /**
     * Provides the authorities granted to proctor users.
     * All proctor users receive the ROLE_PROCTOR authority.
     *
     * @param proctor the Proctor entity
     * @return a list containing a single GrantedAuthority: ROLE_PROCTOR
     */
    @Override
    protected List<GrantedAuthority> getAuthorities(Proctor proctor) {
        return List.of(new SimpleGrantedAuthority("ROLE_PROCTOR"));
    }
}