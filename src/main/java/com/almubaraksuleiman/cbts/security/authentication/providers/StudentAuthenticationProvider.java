
package com.almubaraksuleiman.cbts.security.authentication.providers;


import com.almubaraksuleiman.cbts.student.model.Student;
import com.almubaraksuleiman.cbts.student.repository.StudentRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

@Component
public class StudentAuthenticationProvider extends BaseAuthenticationProvider<Student> {

    private final StudentRepository studentRepository;

    public StudentAuthenticationProvider(StudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        super(passwordEncoder);
        this.studentRepository = studentRepository;
    }

    @Override
    protected Optional<Student> findUserByUsername(String username) {
        return studentRepository.findByUsername(username);
    }

    @Override
    protected String getPassword(Student student) {
        return student.getPassword();
    }

    @Override
    protected boolean isVerified(Student student) {
        return student.isVerified();
    }

    @Override
    protected List<GrantedAuthority> getAuthorities(Student student) {
        return List.of(new SimpleGrantedAuthority("ROLE_STUDENT"));
    }
}

