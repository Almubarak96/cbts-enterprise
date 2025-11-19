package com.almubaraksuleiman.cbts.api;

import com.almubaraksuleiman.cbts.dto.TestDto;
import com.almubaraksuleiman.cbts.student.repository.StudentRepository;
import com.almubaraksuleiman.cbts.student.service.impl.StudentEnrolledTestsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor

public class StudentEnrolledTestsController {

    private final StudentEnrolledTestsService enrolledTestsService;
    private final StudentRepository studentRepository;

    @GetMapping("/enrolled-tests")
    public ResponseEntity<List<TestDto>> getEnrolledTests(Authentication authentication) {

        String username = authentication.getName();
        Long studentId = studentRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Student not found for username: " + username))
                .getId();
        try {
            List<TestDto> tests = enrolledTestsService.getEnrolledTests(studentId);
            return ResponseEntity.ok(tests);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/enrolled-tests/{testId}/check")
    public ResponseEntity<Boolean> isStudentEnrolled(
            @PathVariable Long testId, Authentication authentication) {
        String username = authentication.getName();
        Long studentId = studentRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Student not found for username: " + username))
                .getId();
        try {
            boolean isEnrolled = enrolledTestsService.isStudentEnrolled(studentId, testId);
            return ResponseEntity.ok(isEnrolled);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}