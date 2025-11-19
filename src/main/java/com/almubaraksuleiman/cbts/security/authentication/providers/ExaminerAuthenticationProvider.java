
package com.almubaraksuleiman.cbts.security.authentication.providers;

import com.almubaraksuleiman.cbts.examiner.model.Examiner;
import com.almubaraksuleiman.cbts.examiner.repository.ExaminerRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * AuthenticationProvider implementation for Examiner users.
 * Extends BaseAuthenticationProvider to handle examiner-specific authentication logic.
 * <p>
 * This component is responsible for:
 * <ul>
 *   <li>Finding examiner users by username</li>
 *   <li>Verifying examiner account status</li>
 *   <li>Providing examiner-specific authorities (ROLE_EXAMINER)</li>
 * </ul>
 *
 * @author Almubarak Suleiman
 * @see BaseAuthenticationProvider
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

@Component
public class ExaminerAuthenticationProvider extends BaseAuthenticationProvider<Examiner> {

    private final ExaminerRepository examinerRepository;

    /**
     * Constructs a new ExaminerAuthenticationProvider with required dependencies.
     *
     * @param examinerRepository the repository for accessing examiner data
     * @param passwordEncoder the password encoder for verifying credentials
     */
    public ExaminerAuthenticationProvider(ExaminerRepository examinerRepository, PasswordEncoder passwordEncoder) {
        // Pass the password encoder to the parent base class
        super(passwordEncoder);
        this.examinerRepository = examinerRepository;
    }

    /**
     * Finds an examiner user by username using the ExaminerRepository.
     *
     * @param username the username to search for
     * @return an Optional containing the Examiner if found, empty Optional otherwise
     */
    @Override
    protected Optional<Examiner> findUserByUsername(String username) {
        return examinerRepository.findByUsername(username);
    }

    /**
     * Extracts the encoded password from the Examiner entity.
     *
     * @param examiner the Examiner entity
     * @return the encoded password string
     */
    @Override
    protected String getPassword(Examiner examiner) {
        return examiner.getPassword();
    }

    /**
     * Checks if the examiner account has been verified and is active.
     * This implementation uses the isVerified() method from the Examiner entity.
     *
     * @param examiner the Examiner entity
     * @return true if the examiner account is verified, false otherwise
     */
    @Override
    protected boolean isVerified(Examiner examiner) {
        return examiner.isVerified();
    }

    /**
     * Provides the authorities granted to examiner users.
     * All examiner users receive the ROLE_EXAMINER authority.
     *
     * @param examiner the Examiner entity
     * @return a list containing a single GrantedAuthority: ROLE_EXAMINER
     */
    @Override
    protected List<GrantedAuthority> getAuthorities(Examiner examiner) {
        return List.of(new SimpleGrantedAuthority("ROLE_EXAMINER"));
    }
}
