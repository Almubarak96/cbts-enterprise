package com.almubaraksuleiman.cbts.proctor.service.impl;

import com.almubaraksuleiman.cbts.examiner.model.Examiner;
import com.almubaraksuleiman.cbts.examiner.repository.ExaminerRepository;
import com.almubaraksuleiman.cbts.proctor.model.Proctor;
import com.almubaraksuleiman.cbts.proctor.repository.ProctorRepository;
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
public class ProctorRegistrationService implements RegistrationService {

    private final ProctorRepository proctorRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public ProctorRegistrationService(ProctorRepository proctorRepository) {
        this.proctorRepository = proctorRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(); // you can also configure as @Bean
    }

    @Override
    public void register(String username, String rawPassword, String email) {
        Proctor proctor = new Proctor();
        proctor.setUsername(username);
        proctor.setEmail(email);
        proctor.setPassword(passwordEncoder.encode(rawPassword));
        proctor.setVerified(false); // <--- important
        proctorRepository.save(proctor);
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
        // ... your user creation logic ...
        Proctor proctor = new Proctor();
        proctor.setFirstName(firstName);
        proctor.setMiddleName(middleName);
        proctor.setLastName(lastName);
        proctor.setUsername(username);
        proctor.setEmail(email);
        proctor.setPassword(passwordEncoder.encode(password));
        proctor.setDepartment(department);
        proctorRepository.save(proctor);
    }

    private String saveProfileImage(MultipartFile file, String username) throws IOException {
        // Implement your file resource logic here
        // Return the file path or URL
        return "path/to/saved/image";
    }

    @Override
    public void verifyUser(String username) {
        Proctor proctor = proctorRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        proctor.setVerified(true);
        proctorRepository.save(proctor);
    }


    @Override
    public boolean updatePasswordByEmail(String email, String encodedPassword) {
        Optional<Proctor> proctor = proctorRepository.findByEmail(email);
        if (proctor.isPresent()) {
            proctor.get().setPassword(encodedPassword);
            proctorRepository.save(proctor.get());
            return true;
        }
        return false;
    }
}
