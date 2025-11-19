package com.almubaraksuleiman.cbts.security.authentication.service;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

@Service
public interface RegistrationService {

    //void register(String username, String rawPassword);

    void register(String username, String rawPassword, String email);

    void register(Map<String, Object> registrationData);

    void verifyUser(String username);

    /**
     * Updates user password by email
     * @param email user email
     * @param encodedPassword encoded new password
     * @return true if update was successful
     */
    boolean updatePasswordByEmail(String email, String encodedPassword);



}
