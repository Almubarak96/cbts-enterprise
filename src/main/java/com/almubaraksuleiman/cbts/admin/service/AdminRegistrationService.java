package com.almubaraksuleiman.cbts.admin.service;

import com.almubaraksuleiman.cbts.admin.model.Admin;
import com.almubaraksuleiman.cbts.admin.repository.AdminRepository;
import com.almubaraksuleiman.cbts.examiner.model.Examiner;
import com.almubaraksuleiman.cbts.security.authentication.service.RegistrationService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
**/
@Service
public class AdminRegistrationService implements RegistrationService{

    private final AdminRepository adminRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminRegistrationService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(); // you can also configure as @Bean
    }

    @Override
    public void register(String username, String rawPassword, String email) {
        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(rawPassword));
        admin.setVerified(false); // <--- important
        adminRepository.save(admin);
    }


    @Override
    public void register(Map<String, Object> registrationData) {
        // Extract data from the enhanced payload
        String firstName = (String) registrationData.get("firstName");
        String lastName = (String) registrationData.get("lastName");
        String middleName = (String) registrationData.get("middleName");
        String email = (String) registrationData.get("email");
        String username = (String) registrationData.get("username");
        String password = (String) registrationData.get("password");
        String department = (String) registrationData.get("department");
        MultipartFile profileImage = (MultipartFile) registrationData.get("profileImage");

        // Your enhanced registration logic here
        // Handle file upload, create user entity with all fields, etc.

        // Example file handling:
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                // Save profile image logic here
                String imagePath = saveProfileImage(profileImage, username);
                // Store imagePath in user entity
            } catch (IOException e) {
                throw new RuntimeException("Failed to save profile image", e);
            }
        }

        // Create and save user with all the additional fields
        Admin admin = new Admin();
        admin.setFirstName(firstName);
        admin.setMiddleName(middleName);
        admin.setLastName(lastName);
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setDepartment(department);
        adminRepository.save(admin);
    }

    private String saveProfileImage(MultipartFile file, String username) throws IOException {
        // Implement your file resource logic here
        // Return the file path or URL
        return "path/to/saved/image";
    }

    @Override
    public void verifyUser(String username) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        admin.setVerified(true);
        adminRepository.save(admin);
    }


    @Override
    public boolean updatePasswordByEmail(String email, String encodedPassword) {
        Optional<Admin> admin = adminRepository.findByEmail(email);
        if (admin.isPresent()) {
            admin.get().setPassword(encodedPassword);
            adminRepository.save(admin.get());
            return true;
        }
        return false;
    }
}
