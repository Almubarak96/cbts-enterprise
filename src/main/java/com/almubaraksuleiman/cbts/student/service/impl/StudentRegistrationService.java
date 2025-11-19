package com.almubaraksuleiman.cbts.student.service.impl;

import com.almubaraksuleiman.cbts.security.authentication.service.RegistrationService;
import com.almubaraksuleiman.cbts.student.model.Student;
import com.almubaraksuleiman.cbts.student.repository.StudentRepository;
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
public class StudentRegistrationService implements RegistrationService {

    private final StudentRepository studentRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public StudentRegistrationService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(); // you can also configure as @Bean
    }

    @Override
    public void register(String username, String rawPassword, String email) {
        Student proctor = new Student();
        proctor.setUsername(username);
        proctor.setEmail(email);
        proctor.setPassword(passwordEncoder.encode(rawPassword));
        proctor.setVerified(false); // <--- important
        studentRepository.save(proctor);
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
        Student student = new Student();
        student.setFirstName(firstName);
        student.setMiddleName(middleName);
        student.setLastName(lastName);
        student.setUsername(username);
        student.setEmail(email);
        student.setPassword(passwordEncoder.encode(password));
        student.setDepartment(department);
        studentRepository.save(student);
    }

    private String saveProfileImage(MultipartFile file, String username) throws IOException {
        // Implement your file resource logic here
        // Return the file path or URL
        return "path/to/saved/image";
    }

    @Override
    public void verifyUser(String username) {
        Student proctor = studentRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        proctor.setVerified(true);
        studentRepository.save(proctor);
    }


    @Override
    public boolean updatePasswordByEmail(String email, String encodedPassword) {
        Optional<Student> student = studentRepository.findByEmail(email);
        if (student.isPresent()) {
            student.get().setPassword(encodedPassword);
            studentRepository.save(student.get());
            return true;
        }
        return false;
    }

}
