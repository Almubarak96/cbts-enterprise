package com.almubaraksuleiman.cbts.examiner.service.impl;

import com.almubaraksuleiman.cbts.examiner.dto.*;

//import com.almubaraksuleiman.cbts.examiner.model.Enrollment;
import com.almubaraksuleiman.cbts.examiner.model.Enrollment;
import com.almubaraksuleiman.cbts.examiner.model.Test;

import com.almubaraksuleiman.cbts.examiner.repository.EnrollmentRepository;
import com.almubaraksuleiman.cbts.examiner.repository.TestRepository;
import com.almubaraksuleiman.cbts.student.model.Student;
import com.almubaraksuleiman.cbts.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentService {
    
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final TestRepository testRepository;
   // private final NotificationService notificationService;
    
    @Transactional
    public EnrollmentResponse enrollStudents(EnrollmentRequest request) {
        log.info("Enrolling {} students for test ID: {}", request.getStudentIds().size(), request.getTestId());
        
        Test test = testRepository.findById(request.getTestId())
            .orElseThrow(() -> new RuntimeException("Test not found with id: " + request.getTestId()));
        
        if (!test.getPublished()) {
            throw new RuntimeException("Cannot enroll students in an unpublished test");
        }
        
        List<Student> students = studentRepository.findByIdIn(request.getStudentIds());
        List<Long> failedEnrollments = new ArrayList<>();
        List<Enrollment> successfulEnrollments = new ArrayList<>();
        
        for (Student student : students) {
            try {
                if (enrollmentRepository.existsByTestIdAndStudentId(test.getId(), student.getId())) {
                    failedEnrollments.add(student.getId());
                    continue;
                }
                
                Enrollment enrollment = Enrollment.builder()
                    .test(test)
                    .student(student)
                    .status(Enrollment.EnrollmentStatus.ENROLLED)
                    .enrolledAt(LocalDateTime.now())
                    .notificationSent(false)
                    .build();
                
                successfulEnrollments.add(enrollmentRepository.save(enrollment));
                
            } catch (Exception e) {
                log.error("Failed to enroll student ID: {}", student.getId(), e);
                failedEnrollments.add(student.getId());
            }
        }
        
        // Send notifications if requested
        if (request.getSendNotification() && !successfulEnrollments.isEmpty()) {
            sendEnrollmentNotifications(successfulEnrollments, request.getNotificationMessage());
        }
        
        return EnrollmentResponse.builder()
            .success(!successfulEnrollments.isEmpty())
            .message(String.format("Successfully enrolled %d student(s). Failed: %d", 
                     successfulEnrollments.size(), failedEnrollments.size()))
            .enrolledCount(successfulEnrollments.size())
            .failedEnrollments(failedEnrollments)
            .build();
    }
    
    @Transactional
    public void unenrollStudent(Long testId, Long studentId) {
        log.info("Unenrolling student ID: {} from test ID: {}", studentId, testId);
        
        if (!enrollmentRepository.existsByTestIdAndStudentId(testId, studentId)) {
            throw new RuntimeException("Student is not enrolled in this test");
        }
        
        enrollmentRepository.deleteByTestIdAndStudentId(testId, studentId);
    }
    
    @Transactional
    public void unenrollAllStudents(Long testId) {
        log.info("Unenrolling all students from test ID: {}", testId);
        
        Long enrolledCount = enrollmentRepository.countByTestId(testId);
        if (enrolledCount == 0) {
            throw new RuntimeException("No students enrolled in this test");
        }
        
        enrollmentRepository.deleteByTestId(testId);
    }
    
    public List<StudentDTO> getEnrolledStudents(Long testId) {
        List<Student> students = enrollmentRepository.findEnrolledStudentsByTestId(testId);
        return students.stream()
            .map(this::convertToStudentDTO)
            .collect(Collectors.toList());
    }
    
    public List<StudentDTO> getAvailableStudents(Long testId) {
        List<Student> students = studentRepository.findAvailableStudentsForTest(testId);
        return students.stream()
            .map(this::convertToStudentDTO)
            .collect(Collectors.toList());
    }
    
    public List<StudentDTO> getAllStudents() {
        List<Student> students = studentRepository.findAll();
        return students.stream()
            .map(this::convertToStudentDTO)
            .collect(Collectors.toList());
    }
    
    public List<String> getDepartments() {
        return studentRepository.findDistinctDepartments();
    }
    
    @Transactional
    public void sendNotifications(Long testId, String message) {
        List<Enrollment> enrollments = enrollmentRepository.findByTestId(testId);
        
        if (enrollments.isEmpty()) {
            throw new RuntimeException("No enrolled students found for test ID: " + testId);
        }
        
        List<Student> students = enrollments.stream()
            .map(Enrollment::getStudent)
            .collect(Collectors.toList());
        
        //notificationService.sendBulkNotifications(students, message, testId);
        
        // Mark notifications as sent
        enrollments.forEach(enrollment -> enrollment.setNotificationSent(true));
        enrollmentRepository.saveAll(enrollments);
    }
    
    private void sendEnrollmentNotifications(List<Enrollment> enrollments, String message) {
        List<Student> students = enrollments.stream()
            .map(Enrollment::getStudent)
            .collect(Collectors.toList());
        
        //notificationService.sendBulkNotifications(students, message, enrollments.get(0).getTest().getId());
        
        // Mark notifications as sent
        enrollments.forEach(enrollment -> enrollment.setNotificationSent(true));
        enrollmentRepository.saveAll(enrollments);
    }
    
    private StudentDTO convertToStudentDTO(Student student) {
        return StudentDTO.builder()
            .id(student.getId())
            .name(student.getFirstName() + " " + 
                  (student.getMiddleName() != null ? student.getMiddleName() + " " : "") + 
                  student.getLastName())
            .email(student.getEmail())
            .studentId(student.getUsername()) // Using username as student ID
            .department(student.getDepartment())
            .enrolled(false) // This will be set by the frontend based on context
            .build();
    }
}