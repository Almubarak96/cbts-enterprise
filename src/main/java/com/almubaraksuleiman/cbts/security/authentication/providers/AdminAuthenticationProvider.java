
package com.almubaraksuleiman.cbts.security.authentication.providers;

import com.almubaraksuleiman.cbts.admin.model.Admin;
import com.almubaraksuleiman.cbts.admin.repository.AdminRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * AuthenticationProvider implementation for Admin users.
 * Extends BaseAuthenticationProvider to handle admin-specific authentication logic.
 * <p>
 * This component is responsible for:
 * <ul>
 *   <li>Finding admin users by username</li>
 *   <li>Verifying admin account status</li>
 *   <li>Providing admin-specific authorities (ROLE_ADMIN)</li>
 * </ul>
 *
 * @author Almubarak Suleiman
 * @see BaseAuthenticationProvider
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

@Component
public class AdminAuthenticationProvider extends BaseAuthenticationProvider<Admin> {

    private final AdminRepository adminRepository;

    /**
     * Constructs a new AdminAuthenticationProvider with required dependencies.
     *
     * @param adminRepository the repository for accessing admin data
     * @param passwordEncoder the password encoder for verifying credentials
     */
    public AdminAuthenticationProvider(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        // Pass the password encoder to the parent base class
        super(passwordEncoder);
        this.adminRepository = adminRepository;
    }

    /**
     * Finds an admin user by username using the AdminRepository.
     *
     * @param username the username to search for
     * @return an Optional containing the Admin if found, empty Optional otherwise
     */
    @Override
    protected Optional<Admin> findUserByUsername(String username) {
        return adminRepository.findByUsername(username);
    }

    /**
     * Extracts the encoded password from the Admin entity.
     *
     * @param admin the Admin entity
     * @return the encoded password string
     */
    @Override
    protected String getPassword(Admin admin) {
        return admin.getPassword();
    }

    /**
     * Checks if the admin account has been verified and is active.
     * This implementation uses the isVerified() method from the Admin entity.
     *
     * @param admin the Admin entity
     * @return true if the admin account is verified, false otherwise
     */
    @Override
    protected boolean isVerified(Admin admin) {
        return admin.isVerified();
    }

    /**
     * Provides the authorities granted to admin users.
     * All admin users receive the ROLE_ADMIN authority.
     *
     * @param admin the Admin entity
     * @return a list containing a single GrantedAuthority: ROLE_ADMIN
     */
    @Override
    protected List<GrantedAuthority> getAuthorities(Admin admin) {
        return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
