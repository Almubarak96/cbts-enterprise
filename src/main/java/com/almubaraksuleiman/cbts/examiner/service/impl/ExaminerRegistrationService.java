package com.almubaraksuleiman.cbts.examiner.service.impl;

import com.almubaraksuleiman.cbts.examiner.model.Examiner;
import com.almubaraksuleiman.cbts.examiner.repository.ExaminerRepository;
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
public class ExaminerRegistrationService implements RegistrationService {

    private final ExaminerRepository examinerRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public ExaminerRegistrationService(ExaminerRepository examinerRepository) {
        this.examinerRepository = examinerRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public void register(String username, String rawPassword, String email) {
        Examiner examiner = new Examiner();
        examiner.setUsername(username);
        examiner.setEmail(email);
        examiner.setPassword(passwordEncoder.encode(rawPassword));
        examiner.setVerified(false); // <--- important
        examinerRepository.save(examiner);
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
        Examiner examiner = new Examiner();
        examiner.setFirstName(firstName);
        examiner.setMiddleName(middleName);
        examiner.setLastName(lastName);
        examiner.setUsername(username);
        examiner.setEmail(email);
        examiner.setPassword(passwordEncoder.encode(password));
        examiner.setDepartment(department);
        examinerRepository.save(examiner);
    }




    private String saveProfileImage(MultipartFile file, String username) throws IOException {
        // Implement your file resource logic here
        // Return the file path or URL
        return "path/to/saved/image";
    }

    @Override
    public void verifyUser(String username) {
        Examiner examiner = examinerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        examiner.setVerified(true);
        examinerRepository.save(examiner);
    }


    @Override
    public boolean updatePasswordByEmail(String email, String encodedPassword) {
        Optional<Examiner> examiner = examinerRepository.findByEmail(email);
        if (examiner.isPresent()) {
            examiner.get().setPassword(encodedPassword);
            examinerRepository.save(examiner.get());
            return true;
        }
        return false;
    }
}
