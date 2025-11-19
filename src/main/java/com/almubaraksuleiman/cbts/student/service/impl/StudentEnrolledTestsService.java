package com.almubaraksuleiman.cbts.student.service.impl;


import com.almubaraksuleiman.cbts.dto.TestDto;
import com.almubaraksuleiman.cbts.examiner.model.Enrollment;
import com.almubaraksuleiman.cbts.examiner.model.Test;
import com.almubaraksuleiman.cbts.examiner.repository.EnrollmentRepository;
import com.almubaraksuleiman.cbts.student.model.Student;
import com.almubaraksuleiman.cbts.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentEnrolledTestsService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;

    public List<TestDto> getEnrolledTests(Long studentId) {
        log.info("Fetching enrolled tests for student ID: {}", studentId);

        // Verify student exists
        if (!studentRepository.existsById(studentId)) {
            throw new RuntimeException("Student not found with ID: " + studentId);
        }

        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndStatus(studentId, Enrollment.EnrollmentStatus.ENROLLED);

        return enrollments.stream()
                .map(this::convertToTestDto)
               // .filter(Test::getPublished)
                //.filter(Test::getCreatedBy)// Only return published tests
                .collect(Collectors.toList());
    }


    private TestDto convertToTestDto(Enrollment enrollment) {
        Test test = enrollment.getTest();
        TestDto testDto = new TestDto();
        testDto.setId(test.getId());
        testDto.setDurationMinutes(test.getDurationMinutes());
        testDto.setTitle(test.getTitle());
                return testDto;

    }

    public boolean isStudentEnrolled(Long studentId, Long testId) {
        return enrollmentRepository.existsByTestIdAndStudentIdAndStatus(
                testId, studentId, Enrollment.EnrollmentStatus.ENROLLED);
    }

    public List<Test> getCompletedTests(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdAndStatus(
                studentId, Enrollment.EnrollmentStatus.COMPLETED);

        return enrollments.stream()
                .map(Enrollment::getTest)
                .collect(Collectors.toList());
    }
}