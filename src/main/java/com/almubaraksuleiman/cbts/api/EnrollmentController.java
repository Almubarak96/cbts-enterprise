package com.almubaraksuleiman.cbts.api;

import com.almubaraksuleiman.cbts.examiner.dto.*;

import com.almubaraksuleiman.cbts.examiner.service.impl.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200") // Angular dev server
public class EnrollmentController {
    
    private final EnrollmentService enrollmentService;
    
    @GetMapping("/students")
    public ResponseEntity<List<StudentDTO>> getAllStudents() {
        List<StudentDTO> students = enrollmentService.getAllStudents();
        return ResponseEntity.ok(students);
    }
    
    @GetMapping("/tests/{testId}/enrollments")
    public ResponseEntity<List<StudentDTO>> getEnrolledStudents(@PathVariable Long testId) {
        List<StudentDTO> enrolledStudents = enrollmentService.getEnrolledStudents(testId);
        return ResponseEntity.ok(enrolledStudents);
    }
    
    @GetMapping("/departments")
    public ResponseEntity<List<String>> getDepartments() {
        List<String> departments = enrollmentService.getDepartments();
        return ResponseEntity.ok(departments);
    }
    
    @PostMapping("/enrollments")
    public ResponseEntity<EnrollmentResponse> enrollStudents(@RequestBody EnrollmentRequest request) {
        EnrollmentResponse response = enrollmentService.enrollStudents(request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/tests/{testId}/enrollments/{studentId}")
    public ResponseEntity<Void> unenrollStudent(
            @PathVariable Long testId, 
            @PathVariable Long studentId) {
        enrollmentService.unenrollStudent(testId, studentId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/tests/{testId}/enrollments")
    public ResponseEntity<Void> unenrollAllStudents(@PathVariable Long testId) {
        enrollmentService.unenrollAllStudents(testId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/tests/{testId}/notifications")
    public ResponseEntity<Void> sendNotifications(
            @PathVariable Long testId,
            @RequestBody NotificationRequest request) {
        enrollmentService.sendNotifications(testId, request.getMessage());
        return ResponseEntity.ok().build();
    }
}