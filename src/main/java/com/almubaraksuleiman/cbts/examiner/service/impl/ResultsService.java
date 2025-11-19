////package com.almubaraksuleiman.cbts.examiner.service.impl;
////
////import com.almubaraksuleiman.cbts.examiner.dto.*;
////import com.almubaraksuleiman.cbts.examiner.model.Test;
////import com.almubaraksuleiman.cbts.student.model.StudentExam;
////import com.almubaraksuleiman.cbts.student.model.Student;
////import com.almubaraksuleiman.cbts.student.repository.StudentExamRepository;
////import com.almubaraksuleiman.cbts.examiner.repository.TestRepository;
////import lombok.RequiredArgsConstructor;
////import lombok.extern.slf4j.Slf4j;
////import org.springframework.data.domain.Page;
////import org.springframework.data.domain.PageImpl;
////import org.springframework.data.domain.PageRequest;
////import org.springframework.data.domain.Pageable;
////import org.springframework.data.jpa.domain.Specification;
////import org.springframework.stereotype.Service;
////
////import jakarta.persistence.criteria.Join;
////import jakarta.persistence.criteria.Predicate;
////import java.time.LocalDateTime;
////import java.util.ArrayList;
////import java.util.List;
////import java.util.stream.Collectors;
////
////@Service
////@RequiredArgsConstructor
////@Slf4j
////public class ResultsService {
////
////    private final StudentExamRepository studentExamRepository;
////    private final TestRepository testRepository;
////
////    public Page<TestResultSummary> getTestResultsSummary(TestResultsFilter filter, Pageable pageable) {
////        Specification<Test> spec = buildTestSpecification(filter);
////        Page<Test> testsPage = testRepository.findAll(spec, pageable);
////
////        List<TestResultSummary> summaries = testsPage.getContent().stream()
////                .map(this::buildTestResultSummary)
////                .collect(Collectors.toList());
////
////        return new PageImpl<>(summaries, pageable, testsPage.getTotalElements());
////    }
////
////    public Page<StudentExamResult> getStudentExamResults(Long testId, StudentResultsFilter filter, Pageable pageable) {
////        Specification<StudentExam> spec = buildStudentExamSpecification(testId, filter);
////        Page<StudentExam> examsPage = studentExamRepository.findAll(spec, pageable);
////
////        List<StudentExamResult> results = examsPage.getContent().stream()
////                .map(this::buildStudentExamResult)
////                .collect(Collectors.toList());
////
////        return new PageImpl<>(results, pageable, examsPage.getTotalElements());
////    }
////
////    public StudentDetailedResult getStudentDetailedResult(Long sessionId) {
////        StudentExam exam = studentExamRepository.findById(sessionId)
////                .orElseThrow(() -> new RuntimeException("Exam session not found"));
////
////        return buildStudentDetailedResult(exam);
////    }
////
////    private Specification<Test> buildTestSpecification(TestResultsFilter filter) {
////        return (root, query, cb) -> {
////            List<Predicate> predicates = new ArrayList<>();
////
////            if (filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty()) {
////                String searchPattern = "%" + filter.getSearchTerm().toLowerCase() + "%";
////                predicates.add(cb.or(
////                    cb.like(cb.lower(root.get("title")), searchPattern),
////                    cb.like(cb.lower(root.get("description")), searchPattern)
////                ));
////            }
////
////            if (filter.getStatus() != null) {
////                predicates.add(cb.equal(root.get("published"), "published".equals(filter.getStatus())));
////            }
////
////            if (filter.getStartDate() != null) {
////                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getStartDate()));
////            }
////
////            if (filter.getEndDate() != null) {
////                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getEndDate()));
////            }
////
////            return cb.and(predicates.toArray(new Predicate[0]));
////        };
////    }
////
////    private Specification<StudentExam> buildStudentExamSpecification(Long testId, StudentResultsFilter filter) {
////        return (root, query, cb) -> {
////            List<Predicate> predicates = new ArrayList<>();
////
////            // Filter by test ID
////            predicates.add(cb.equal(root.get("test").get("id"), testId));
////
////            if (filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty()) {
////                String searchPattern = "%" + filter.getSearchTerm().toLowerCase() + "%";
////
////                // Join with student entity for name/email search
////                Join<StudentExam, Student> studentJoin = root.join("student");
////                predicates.add(cb.or(
////                    cb.like(cb.lower(studentJoin.get("usename")), searchPattern),
////                    cb.like(cb.lower(studentJoin.get("email")), searchPattern),
////                    cb.like(cb.lower(studentJoin.getParent().get("studentId")), searchPattern)
////                ));
////            }
////
////            if (filter.getMinScore() != null) {
////                predicates.add(cb.greaterThanOrEqualTo(root.get("percentage"), filter.getMinScore()));
////            }
////
////            if (filter.getMaxScore() != null) {
////                predicates.add(cb.lessThanOrEqualTo(root.get("percentage"), filter.getMaxScore()));
////            }
////
////            if (filter.getStatus() != null) {
////                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
////            }
////
////            if (filter.getGraded() != null) {
////                predicates.add(cb.equal(root.get("graded"), filter.getGraded()));
////            }
////
////            if (filter.getStartDate() != null) {
////                predicates.add(cb.greaterThanOrEqualTo(root.get("endTime"), filter.getStartDate()));
////            }
////
////            if (filter.getEndDate() != null) {
////                predicates.add(cb.lessThanOrEqualTo(root.get("endTime"), filter.getEndDate()));
////            }
////
////            return cb.and(predicates.toArray(new Predicate[0]));
////        };
////    }
////
////    private TestResultSummary buildTestResultSummary(Test test) {
////        List<StudentExam> exams = studentExamRepository.findByTestId(test.getId());
////
////        long totalStudents = exams.size();
////        long completedStudents = exams.stream().filter(StudentExam::getCompleted).count();
////        long gradedStudents = exams.stream().filter(StudentExam::getGraded).count();
////
////        double averageScore = exams.stream()
////                .filter(exam -> exam.getPercentage() != null)
////                .mapToDouble(StudentExam::getPercentage)
////                .average()
////                .orElse(0.0);
////
////        long passedStudents = exams.stream()
////                .filter(exam -> exam.getPercentage() != null && exam.getPercentage() >= test.getPassingScore())
////                .count();
////
////        double passRate = totalStudents > 0 ? (double) passedStudents / totalStudents * 100 : 0.0;
////
////        return TestResultSummary.builder()
////                .testId(test.getId())
////                .testTitle(test.getTitle())
////                .totalStudents((int) totalStudents)
////                .completedStudents((int) completedStudents)
////                .gradedStudents((int) gradedStudents)
////                .averageScore(averageScore)
////                .passRate(passRate)
////                .durationMinutes(test.getDurationMinutes())
////                .totalMarks(test.getTotalMarks())
////                .passingScore(test.getPassingScore())
////                .published(test.getPublished())
////                .createdAt(test.getCreatedAt())
////                .build();
////    }
////
////    private StudentExamResult buildStudentExamResult(StudentExam exam) {
////        return StudentExamResult.builder()
////                .sessionId(exam.getSessionId())
////                .studentId(exam.getStudentId())
////                .studentName("Student " + exam.getStudentId()) // You would fetch actual student name
////                .studentEmail("student" + exam.getStudentId() + "@university.edu")
////                .score(exam.getScore())
////                .percentage(exam.getPercentage())
////                .timeSpent(calculateTimeSpent(exam))
////                .status(exam.getStatus())
////                .graded(exam.getGraded())
////                .completed(exam.getCompleted())
////                .startTime(exam.getStartTime())
////                .endTime(exam.getEndTime())
////                .grade(getGradeLetter(exam.getPercentage()))
////                .passed(isPassed(exam, exam.getTest()))
////                .build();
////    }
////
////    private StudentDetailedResult buildStudentDetailedResult(StudentExam exam) {
////
////        // This would include detailed question-by-question results
////        return StudentDetailedResult.builder()
////                .sessionId(exam.getSessionId())
////                .studentId(exam.getStudentId())
////                .studentName("Student " + exam.getStudentId())
////                .studentEmail("student" + exam.getStudentId() + "@university.edu")
////                .testTitle(exam.getTest().getTitle())
////                .score(exam.getScore())
////                .percentage(exam.getPercentage())
////                .totalMarks(exam.getTest().getTotalMarks())
////                .timeSpent(calculateTimeSpent(exam))
////                .grade(getGradeLetter(exam.getPercentage()))
////                .passed(isPassed(exam, exam.getTest()))
////                .startTime(exam.getStartTime())
////                .endTime(exam.getEndTime())
////                .status(exam.getStatus())
////                // Add question breakdown here
////                .build();
////    }
////
////    private Integer calculateTimeSpent(StudentExam exam) {
////        if (exam.getStartTime() != null && exam.getEndTime() != null) {
////            return (int) java.time.Duration.between(exam.getStartTime(), exam.getEndTime()).toMinutes();
////        }
////        return 0;
////    }
////
////    private String getGradeLetter(Double percentage) {
////        if (percentage == null) return "N/A";
////        if (percentage >= 90) return "A";
////        if (percentage >= 80) return "B";
////        if (percentage >= 70) return "C";
////        if (percentage >= 60) return "D";
////        return "F";
////    }
////
////    private boolean isPassed(StudentExam exam, Test test) {
////        return exam.getPercentage() != null && exam.getPercentage() >= test.getPassingScore();
////    }
////}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//package com.almubaraksuleiman.cbts.examiner.service.impl;
//
//import com.almubaraksuleiman.cbts.examiner.SecurityUtils;
//import com.almubaraksuleiman.cbts.examiner.dto.*;
//import com.almubaraksuleiman.cbts.examiner.model.Test;
//import com.almubaraksuleiman.cbts.examiner.model.Examiner;
//import com.almubaraksuleiman.cbts.student.model.StudentExam;
//import com.almubaraksuleiman.cbts.student.model.Student;
//import com.almubaraksuleiman.cbts.student.repository.StudentExamRepository;
//import com.almubaraksuleiman.cbts.examiner.repository.TestRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.stereotype.Service;
//
//import jakarta.persistence.criteria.Join;
//import jakarta.persistence.criteria.Predicate;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class ResultsService {
//
//    private final StudentExamRepository studentExamRepository;
//    private final TestRepository testRepository;
//    private final SecurityUtils securityUtils;
//
//    public Page<TestResultSummary> getTestResultsSummary(TestResultsFilter filter, Pageable pageable) {
//        securityUtils.validateExaminerOrAdmin();
//
//        Specification<Test> spec = buildTestSpecification(filter);
//        Page<Test> testsPage = testRepository.findAll(spec, pageable);
//
//        // Filter tests based on access control
//        List<TestResultSummary> summaries = testsPage.getContent().stream()
//                .filter(test -> securityUtils.isAdmin() || securityUtils.canAccessTest(test))
//                .map(this::buildTestResultSummary)
//                .collect(Collectors.toList());
//
//        return new PageImpl<>(summaries, pageable, summaries.size());
//    }
//
//    public Page<StudentExamResult> getStudentExamResults(Long testId, StudentResultsFilter filter, Pageable pageable) {
//        // First validate the examiner has access to this test
//        securityUtils.validateTestAccess(testId);
//
//        Specification<StudentExam> spec = buildStudentExamSpecification(testId, filter);
//        Page<StudentExam> examsPage = studentExamRepository.findAll(spec, pageable);
//
//        List<StudentExamResult> results = examsPage.getContent().stream()
//                .map(this::buildStudentExamResult)
//                .collect(Collectors.toList());
//
//        return new PageImpl<>(results, pageable, examsPage.getTotalElements());
//    }
//
//    public StudentDetailedResult getStudentDetailedResult(Long sessionId) {
//        StudentExam exam = studentExamRepository.findById(sessionId)
//                .orElseThrow(() -> new RuntimeException("Exam session not found"));
//
//        // Validate the examiner has access to the test associated with this exam
//        securityUtils.validateTestAccess(exam.getTest().getId());
//
//        return buildStudentDetailedResult(exam);
//    }
//
//    private Specification<Test> buildTestSpecification(TestResultsFilter filter) {
//        return (root, query, cb) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            // Add access control for examiners
//            if (!securityUtils.isAdmin()) {
//                Examiner examiner = securityUtils.getCurrentExaminer();
//                predicates.add(cb.equal(root.get("createdBy"), examiner));
//            }
//
//            if (filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty()) {
//                String searchPattern = "%" + filter.getSearchTerm().toLowerCase() + "%";
//                predicates.add(cb.or(
//                        cb.like(cb.lower(root.get("title")), searchPattern),
//                        cb.like(cb.lower(root.get("description")), searchPattern)
//                ));
//            }
//
//            if (filter.getStatus() != null) {
//                predicates.add(cb.equal(root.get("published"), "published".equals(filter.getStatus())));
//            }
//
//            if (filter.getStartDate() != null) {
//                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getStartDate()));
//            }
//
//            if (filter.getEndDate() != null) {
//                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getEndDate()));
//            }
//
//            return cb.and(predicates.toArray(new Predicate[0]));
//        };
//    }
//
//    private Specification<StudentExam> buildStudentExamSpecification(Long testId, StudentResultsFilter filter) {
//        return (root, query, cb) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            // Filter by test ID
//            predicates.add(cb.equal(root.get("test").get("id"), testId));
//
//            if (filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty()) {
//                String searchPattern = "%" + filter.getSearchTerm().toLowerCase() + "%";
//
//                // Join with student entity for name/email search
//                Join<StudentExam, Student> studentJoin = root.join("student");
//                predicates.add(cb.or(
//                        cb.like(cb.lower(studentJoin.get("username")), searchPattern), // Fixed typo: usename -> username
//                        cb.like(cb.lower(studentJoin.get("email")), searchPattern),
//                        cb.like(cb.lower(studentJoin.get("studentId")), searchPattern) // Fixed: removed getParent()
//                ));
//            }
//
//            if (filter.getMinScore() != null) {
//                predicates.add(cb.greaterThanOrEqualTo(root.get("percentage"), filter.getMinScore()));
//            }
//
//            if (filter.getMaxScore() != null) {
//                predicates.add(cb.lessThanOrEqualTo(root.get("percentage"), filter.getMaxScore()));
//            }
//
//            if (filter.getStatus() != null) {
//                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
//            }
//
//            if (filter.getGraded() != null) {
//                predicates.add(cb.equal(root.get("graded"), filter.getGraded()));
//            }
//
//            if (filter.getStartDate() != null) {
//                predicates.add(cb.greaterThanOrEqualTo(root.get("endTime"), filter.getStartDate()));
//            }
//
//            if (filter.getEndDate() != null) {
//                predicates.add(cb.lessThanOrEqualTo(root.get("endTime"), filter.getEndDate()));
//            }
//
//            return cb.and(predicates.toArray(new Predicate[0]));
//        };
//    }
//
//    private TestResultSummary buildTestResultSummary(Test test) {
//        List<StudentExam> exams = studentExamRepository.findByTestId(test.getId());
//
//        long totalStudents = exams.size();
//        long completedStudents = exams.stream().filter(StudentExam::getCompleted).count();
//        long gradedStudents = exams.stream().filter(StudentExam::getGraded).count();
//
//        double averageScore = exams.stream()
//                .filter(exam -> exam.getPercentage() != null)
//                .mapToDouble(StudentExam::getPercentage)
//                .average()
//                .orElse(0.0);
//
//        long passedStudents = exams.stream()
//                .filter(exam -> exam.getPercentage() != null && exam.getPercentage() >= test.getPassingScore())
//                .count();
//
//        double passRate = totalStudents > 0 ? (double) passedStudents / totalStudents * 100 : 0.0;
//
//        return TestResultSummary.builder()
//                .testId(test.getId())
//                .testTitle(test.getTitle())
//                .totalStudents((int) totalStudents)
//                .completedStudents((int) completedStudents)
//                .gradedStudents((int) gradedStudents)
//                .averageScore(Math.round(averageScore * 100.0) / 100.0) // Round to 2 decimal places
//                .passRate(Math.round(passRate * 100.0) / 100.0) // Round to 2 decimal places
//                .durationMinutes(test.getDurationMinutes())
//                .totalMarks(test.getTotalMarks())
//                .passingScore(test.getPassingScore())
//                .published(test.getPublished())
//                .createdAt(test.getCreatedAt())
//                .createdBy(test.getCreatedBy())
//                .build();
//    }
//
//    private StudentExamResult buildStudentExamResult(StudentExam exam) {
//        // Get actual student details if available
//        String studentName = exam.getStudent() != null ?
//                exam.getStudent().getFullName() : "Student " + exam.getStudentId();
//        String studentEmail = exam.getStudent() != null ?
//                exam.getStudent().getEmail() : "student" + exam.getStudentId() + "@university.edu";
//
//        return StudentExamResult.builder()
//                .sessionId(exam.getSessionId())
//                .studentId(exam.getStudentId())
//                .studentName(studentName)
//                .studentEmail(studentEmail)
//                .score(exam.getScore())
//                .percentage(exam.getPercentage())
//                .timeSpent(calculateTimeSpent(exam))
//                .status(exam.getStatus())
//                .graded(exam.getGraded())
//                .completed(exam.getCompleted())
//                .startTime(exam.getStartTime())
//                .endTime(exam.getEndTime())
//                .grade(getGradeLetter(exam.getPercentage()))
//                .passed(isPassed(exam, exam.getTest()))
//                .build();
//    }
//
//    private StudentDetailedResult buildStudentDetailedResult(StudentExam exam) {
//        // Get actual student details if available
//        String studentName = exam.getStudent() != null ?
//                exam.getStudent().getFullName() : "Student " + exam.getStudentId();
//        String studentEmail = exam.getStudent() != null ?
//                exam.getStudent().getEmail() : "student" + exam.getStudentId() + "@university.edu";
//
//        return StudentDetailedResult.builder()
//                .sessionId(exam.getSessionId())
//                .studentId(exam.getStudentId())
//                .studentName(studentName)
//                .studentEmail(studentEmail)
//                .testTitle(exam.getTest().getTitle())
//                .score(exam.getScore())
//                .percentage(exam.getPercentage())
//                .totalMarks(exam.getTest().getTotalMarks())
//                .timeSpent(calculateTimeSpent(exam))
//                .grade(getGradeLetter(exam.getPercentage()))
//                .passed(isPassed(exam, exam.getTest()))
//                .startTime(exam.getStartTime())
//                .endTime(exam.getEndTime())
//                .status(exam.getStatus())
//                // Add question breakdown here when implemented
//                //.questionResults(new ArrayList<>()) // Placeholder for question results
//                .build();
//    }
//
//    private Integer calculateTimeSpent(StudentExam exam) {
//        if (exam.getStartTime() != null && exam.getEndTime() != null) {
//            return (int) java.time.Duration.between(exam.getStartTime(), exam.getEndTime()).toMinutes();
//        }
//        return 0;
//    }
//
//    private String getGradeLetter(Double percentage) {
//        if (percentage == null) return "N/A";
//        if (percentage >= 90) return "A";
//        if (percentage >= 80) return "B";
//        if (percentage >= 70) return "C";
//        if (percentage >= 60) return "D";
//        return "F";
//    }
//
//    private boolean isPassed(StudentExam exam, Test test) {
//        return exam.getPercentage() != null && exam.getPercentage() >= test.getPassingScore();
//    }
//
//    // Additional method to get results for current examiner's tests only
//    public Page<TestResultSummary> getMyTestResultsSummary(TestResultsFilter filter, Pageable pageable) {
//        securityUtils.validateExaminerOrAdmin();
//
//        // For examiners, always filter to their tests only
//        if (!securityUtils.isAdmin()) {
//            Examiner examiner = securityUtils.getCurrentExaminer();
//            Specification<Test> examinerSpec = (root, query, cb) ->
//                    cb.equal(root.get("createdBy"), examiner);
//
//            Specification<Test> finalSpec = examinerSpec.and(buildTestSpecification(filter));
//            Page<Test> testsPage = testRepository.findAll(finalSpec, pageable);
//
//            List<TestResultSummary> summaries = testsPage.getContent().stream()
//                    .map(this::buildTestResultSummary)
//                    .collect(Collectors.toList());
//
//            return new PageImpl<>(summaries, pageable, testsPage.getTotalElements());
//        } else {
//            // For admin, use the regular method
//            return getTestResultsSummary(filter, pageable);
//        }
//    }
//}
























package com.almubaraksuleiman.cbts.examiner.service.impl;

import com.almubaraksuleiman.cbts.examiner.SecurityUtils;
import com.almubaraksuleiman.cbts.examiner.dto.*;
import com.almubaraksuleiman.cbts.examiner.model.Question;
import com.almubaraksuleiman.cbts.examiner.model.QuestionType;
import com.almubaraksuleiman.cbts.examiner.model.Test;
import com.almubaraksuleiman.cbts.examiner.model.Examiner;
import com.almubaraksuleiman.cbts.student.model.StudentAnswer;
import com.almubaraksuleiman.cbts.student.model.StudentExam;
import com.almubaraksuleiman.cbts.student.model.Student;
import com.almubaraksuleiman.cbts.student.repository.StudentAnswerRepository;
import com.almubaraksuleiman.cbts.student.repository.StudentExamRepository;
import com.almubaraksuleiman.cbts.examiner.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResultsService {

    private final StudentExamRepository studentExamRepository;
    private final TestRepository testRepository;
    private final SecurityUtils securityUtils;

    /**
     * Get paginated test results summary with search and filtering
     */
    public Page<TestResultSummary> getTestResultsSummary(TestResultsFilter filter, Pageable pageable) {
        securityUtils.validateExaminerOrAdmin();

        Specification<Test> spec = buildTestSpecification(filter);
        Page<Test> testsPage = testRepository.findAll(spec, pageable);

        // Convert to TestResultSummary with proper pagination
        List<TestResultSummary> summaries = testsPage.getContent().stream()
                .filter(test -> securityUtils.isAdmin() || securityUtils.canAccessTest(test))
                .map(this::buildTestResultSummary)
                .collect(Collectors.toList());

        return new PageImpl<>(summaries, pageable, testsPage.getTotalElements());
    }

    /**
     * Get paginated student exam results for a specific test
     */
    public Page<StudentExamResult> getStudentExamResults(Long testId, StudentResultsFilter filter, Pageable pageable) {
        securityUtils.validateTestAccess(testId);

        Specification<StudentExam> spec = buildStudentExamSpecification(testId, filter);
        Page<StudentExam> examsPage = studentExamRepository.findAll(spec, pageable);

        List<StudentExamResult> results = examsPage.getContent().stream()
                .map(this::buildStudentExamResult)
                .collect(Collectors.toList());

        return new PageImpl<>(results, pageable, examsPage.getTotalElements());
    }

    /**
     * Search test results with pagination
     */
    public Page<TestResultSummary> searchTestResults(String keyword, Pageable pageable) {
        securityUtils.validateExaminerOrAdmin();

        TestResultsFilter filter = TestResultsFilter.builder()
                .searchTerm(keyword)
                .build();

        return getTestResultsSummary(filter, pageable);
    }

    /**
     * Get test results by status with pagination
     */
    public Page<TestResultSummary> getTestResultsByStatus(String status, Pageable pageable) {
        securityUtils.validateExaminerOrAdmin();

        TestResultsFilter filter = TestResultsFilter.builder()
                .status(status)
                .build();

        return getTestResultsSummary(filter, pageable);
    }

    /**
     * Get test results by date range with pagination
     */
    public Page<TestResultSummary> getTestResultsByDateRange(java.time.LocalDateTime startDate,
                                                             java.time.LocalDateTime endDate,
                                                             Pageable pageable) {
        securityUtils.validateExaminerOrAdmin();

        TestResultsFilter filter = TestResultsFilter.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();

        return getTestResultsSummary(filter, pageable);
    }

    /**
     * Advanced search with multiple criteria and pagination
     */
    public Page<TestResultSummary> findTestResultsByAdvancedSearch(String keyword, String status,
                                                                   java.time.LocalDateTime startDate,
                                                                   java.time.LocalDateTime endDate,
                                                                   Pageable pageable) {
        securityUtils.validateExaminerOrAdmin();

        TestResultsFilter filter = TestResultsFilter.builder()
                .searchTerm(keyword)
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        return getTestResultsSummary(filter, pageable);
    }

    /**
     * Get results for current examiner's tests only with pagination
     */
    public Page<TestResultSummary> getMyTestResultsSummary(TestResultsFilter filter, Pageable pageable) {
        securityUtils.validateExaminerOrAdmin();

        if (!securityUtils.isAdmin()) {
            Examiner examiner = securityUtils.getCurrentExaminer();
            Specification<Test> examinerSpec = (root, query, cb) ->
                    cb.equal(root.get("createdBy"), examiner);

            Specification<Test> finalSpec = examinerSpec.and(buildTestSpecification(filter));
            Page<Test> testsPage = testRepository.findAll(finalSpec, pageable);

            List<TestResultSummary> summaries = testsPage.getContent().stream()
                    .map(this::buildTestResultSummary)
                    .collect(Collectors.toList());

            return new PageImpl<>(summaries, pageable, testsPage.getTotalElements());
        } else {
            return getTestResultsSummary(filter, pageable);
        }
    }

    /**
     * Get student detailed result (single record - no pagination needed)
     */
    public StudentDetailedResult getStudentDetailedResult(Long sessionId) {
        StudentExam exam = studentExamRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Exam session not found"));

        securityUtils.validateTestAccess(exam.getTest().getId());

        return buildStudentDetailedResult(exam);
    }

    /**
     * Build specification for test filtering
     */
    private Specification<Test> buildTestSpecification(TestResultsFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Add access control for examiners
            if (!securityUtils.isAdmin()) {
                Examiner examiner = securityUtils.getCurrentExaminer();
                predicates.add(cb.equal(root.get("createdBy"), examiner));
            }

            if (filter != null) {
                if (filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty()) {
                    String searchPattern = "%" + filter.getSearchTerm().toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(root.get("title")), searchPattern),
                            cb.like(cb.lower(root.get("description")), searchPattern)
                    ));
                }

                if (filter.getStatus() != null) {
                    predicates.add(cb.equal(root.get("published"), "published".equals(filter.getStatus())));
                }

                if (filter.getStartDate() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getStartDate()));
                }

                if (filter.getEndDate() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getEndDate()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Build specification for student exam filtering
     */
    private Specification<StudentExam> buildStudentExamSpecification(Long testId, StudentResultsFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by test ID
            predicates.add(cb.equal(root.get("test").get("id"), testId));

            if (filter != null) {
                if (filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty()) {
                    String searchPattern = "%" + filter.getSearchTerm().toLowerCase() + "%";

                    // Join with student entity for name/email search
                    Join<StudentExam, Student> studentJoin = root.join("student");
                    predicates.add(cb.or(
                            cb.like(cb.lower(studentJoin.get("username")), searchPattern),
                            cb.like(cb.lower(studentJoin.get("email")), searchPattern),
                            cb.like(cb.lower(studentJoin.get("studentId")), searchPattern)
                    ));
                }

                if (filter.getMinScore() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("percentage"), filter.getMinScore()));
                }

                if (filter.getMaxScore() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("percentage"), filter.getMaxScore()));
                }

                if (filter.getStatus() != null) {
                    predicates.add(cb.equal(root.get("status"), filter.getStatus()));
                }

                if (filter.getGraded() != null) {
                    predicates.add(cb.equal(root.get("graded"), filter.getGraded()));
                }

                if (filter.getStartDate() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("endTime"), filter.getStartDate()));
                }

                if (filter.getEndDate() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("endTime"), filter.getEndDate()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Build test result summary from Test entity
     */
    private TestResultSummary buildTestResultSummary(Test test) {
        List<StudentExam> exams = studentExamRepository.findByTestId(test.getId());

        long totalStudents = exams.size();
        long completedStudents = exams.stream().filter(StudentExam::getCompleted).count();
        long gradedStudents = exams.stream().filter(StudentExam::getGraded).count();

        double averageScore = exams.stream()
                .filter(exam -> exam.getPercentage() != null)
                .mapToDouble(StudentExam::getPercentage)
                .average()
                .orElse(0.0);

        long passedStudents = exams.stream()
                .filter(exam -> exam.getPercentage() != null && exam.getPercentage() >= test.getPassingScore())
                .count();

        double passRate = totalStudents > 0 ? (double) passedStudents / totalStudents * 100 : 0.0;

        return TestResultSummary.builder()
                .testId(test.getId())
                .testTitle(test.getTitle())
                .totalStudents((int) totalStudents)
                .completedStudents((int) completedStudents)
                .gradedStudents((int) gradedStudents)
                .averageScore(Math.round(averageScore * 100.0) / 100.0)
                .passRate(Math.round(passRate * 100.0) / 100.0)
                .durationMinutes(test.getDurationMinutes())
                .totalMarks(test.getTotalMarks())
                .passingScore(test.getPassingScore())
                .published(test.getPublished())
                .createdAt(test.getCreatedAt())
                .createdBy(test.getCreatedBy())
                .build();
    }

    /**
     * Build student exam result from StudentExam entity
     */
    private StudentExamResult buildStudentExamResult(StudentExam exam) {
        // Get actual student details if available
        String studentName = exam.getStudent() != null ?
                exam.getStudent().getFullName() : "Student " + exam.getStudentId();
        String studentEmail = exam.getStudent() != null ?
                exam.getStudent().getEmail() : "student" + exam.getStudentId() + "@university.edu";

        return StudentExamResult.builder()
                .sessionId(exam.getSessionId())
                .studentId(exam.getStudentId())
                .studentName(studentName)
                .studentEmail(studentEmail)
                .score(exam.getScore())
                .percentage(exam.getPercentage())
                .timeSpent(calculateTimeSpent(exam))
                .status(exam.getStatus().toString())
                .graded(exam.getGraded())
                .completed(exam.getCompleted())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .grade(getGradeLetter(exam.getPercentage()))
                .passed(isPassed(exam, exam.getTest()))
                .build();
    }

    /**
     * Build detailed student result
     */
//    private StudentDetailedResult buildStudentDetailedResult(StudentExam exam) {
//        String studentName = exam.getStudent() != null ?
//                exam.getStudent().getFullName() : "Student " + exam.getStudentId();
//        String studentEmail = exam.getStudent() != null ?
//                exam.getStudent().getEmail() : "student" + exam.getStudentId() + "@university.edu";
//
//        return StudentDetailedResult.builder()
//                .sessionId(exam.getSessionId())
//                .studentId(exam.getStudentId())
//                .studentName(studentName)
//                .studentEmail(studentEmail)
//                .testTitle(exam.getTest().getTitle())
//                .score(exam.getScore())
//                .percentage(exam.getPercentage())
//                .totalMarks(exam.getTest().getTotalMarks())
//                .timeSpent(calculateTimeSpent(exam))
//                .grade(getGradeLetter(exam.getPercentage()))
//                .passed(isPassed(exam, exam.getTest()))
//                .startTime(exam.getStartTime())
//                .endTime(exam.getEndTime())
//                .status(exam.getStatus().toString())
//                .build();
//    }

    // Utility methods (unchanged)
    private Integer calculateTimeSpent(StudentExam exam) {
        if (exam.getStartTime() != null && exam.getEndTime() != null) {
            return (int) java.time.Duration.between(exam.getStartTime(), exam.getEndTime()).toMinutes();
        }
        return 0;
    }

    private String getGradeLetter(Double percentage) {
        if (percentage == null) return "N/A";
        if (percentage >= 90) return "A";
        if (percentage >= 80) return "B";
        if (percentage >= 70) return "C";
        if (percentage >= 60) return "D";
        return "F";
    }

    private boolean isPassed(StudentExam exam, Test test) {
        return exam.getPercentage() != null && exam.getPercentage() >= test.getPassingScore();
    }


    /**
     * Build detailed student result WITH question results
     */
    private StudentDetailedResult buildStudentDetailedResult(StudentExam exam) {
        String studentName = exam.getStudent() != null ?
                exam.getStudent().getFullName() : "Student " + exam.getStudentId();
        String studentEmail = exam.getStudent() != null ?
                exam.getStudent().getEmail() : "student" + exam.getStudentId() + "@university.edu";

        // GET ALL ANSWERS FOR THIS EXAM to build question results
        List<StudentAnswer> studentAnswers = studentAnswerRepository.findByStudentExam(exam);
        List<QuestionResultDTO> questionResults = buildQuestionResults(studentAnswers);

        return StudentDetailedResult.builder()
                .sessionId(exam.getSessionId())
                .studentId(exam.getStudentId())
                .studentName(studentName)
                .studentEmail(studentEmail)
                .testTitle(exam.getTest().getTitle())
                .score(exam.getScore())
                .percentage(exam.getPercentage())
                .totalMarks(exam.getTest().getTotalMarks())
                .timeSpent(calculateTimeSpent(exam))
                .grade(getGradeLetter(exam.getPercentage()))
                .passed(isPassed(exam, exam.getTest()))
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .status(exam.getStatus().toString())
                .questionResults(questionResults) // THIS WAS MISSING
                .build();
    }

    /**
     * Build individual question results from student answers
     */
    private List<QuestionResultDTO> buildQuestionResults(List<StudentAnswer> studentAnswers) {
        return studentAnswers.stream()
                .map(this::convertToQuestionResultDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert StudentAnswer to QuestionResultDTO
     */
    private QuestionResultDTO convertToQuestionResultDTO(StudentAnswer studentAnswer) {
        Question question = studentAnswer.getQuestion();

        return QuestionResultDTO.builder()
                .questionId(question.getId())
                .questionText(question.getText())
                .questionType(question.getType().toString())
                .studentAnswer(studentAnswer.getAnswer())
                .correctAnswer(question.getCorrectAnswer())
                .score(studentAnswer.getScore() != null ? studentAnswer.getScore() : 0.0)
                .maxMarks(question.getMaxMarks())
                .isCorrect(studentAnswer.getScore() != null &&
                        studentAnswer.getScore().equals(question.getMaxMarks()))
                .build();
    }


    private final  ExamGradingService examGradingService;
    private final StudentAnswerRepository studentAnswerRepository;

    // Add these methods to your ResultsService

    /**
     * Get immediate results with grading status check
     */
//    public Map<String, Object> getImmediateResultsWithStatus(Long sessionId) {
//        StudentExam exam = studentExamRepository.findById(sessionId)
//                .orElseThrow(() -> new RuntimeException("Exam session not found"));
//
//        Map<String, Object> result = new HashMap<>();
//        result.put("sessionId", sessionId);
//        result.put("completed", exam.getCompleted());
//        result.put("graded", exam.getGraded());
//        result.put("status", exam.getStatus());
//
//        if (Boolean.TRUE.equals(exam.getGraded()) && "FULLY_GRADED".equals(exam.getStatus())) {
//            // Return full results if fully graded
//            StudentDetailedResult detailedResult = buildStudentDetailedResult(exam);
//            result.put("results", detailedResult);
//            result.put("resultsStatus", "READY");
//        } else {
//            // Return grading status
//            Map<String, Object> gradingStatus = getGradingStatus(sessionId);
//            result.put("gradingStatus", gradingStatus);
//            result.put("resultsStatus", "PENDING");
//        }
//
//        return result;
//    }

    /**
     * Get immediate results with grading status check - SIMPLIFIED FIX
     */
    public Map<String, Object> getImmediateResultsWithStatus(Long sessionId) {
        StudentExam exam = studentExamRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Exam session not found"));

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("completed", exam.getCompleted());
        result.put("graded", exam.getGraded());
        result.put("status", exam.getStatus());

        // SIMPLE FIX: If exam is graded, return results immediately
        if (Boolean.TRUE.equals(exam.getGraded())) {
            StudentDetailedResult detailedResult = buildStudentDetailedResult(exam);
            result.put("results", detailedResult);
            result.put("resultsStatus", "READY");
        } else {
            Map<String, Object> gradingStatus = getGradingStatus(sessionId);
            result.put("gradingStatus", gradingStatus);
            result.put("resultsStatus", "PENDING");

            // Auto-trigger grading for non-essay exams
            Long essayQuestions = (Long) gradingStatus.get("essayQuestions");
            if (essayQuestions == 0) {
                try {
                    triggerImmediateGrading(sessionId);
                } catch (Exception e) {
                    log.warn("Auto-grading failed for session {}: {}", sessionId, e.getMessage());
                }
            }
        }

        return result;
    }

    /**
     * Get detailed grading status
     */
//    public Map<String, Object> getGradingStatus(Long sessionId) {
//        StudentExam exam = studentExamRepository.findById(sessionId)
//                .orElseThrow(() -> new RuntimeException("Exam session not found"));
//
//        List<StudentAnswer> answers = studentAnswerRepository.findByStudentExam(exam);
//
//        long totalQuestions = answers.size();
//        long autoGradedQuestions = answers.stream()
//                .filter(answer -> answer.getQuestion().getType() != QuestionType.ESSAY)
//                .count();
//        long essayQuestions = answers.stream()
//                .filter(answer -> answer.getQuestion().getType() == QuestionType.ESSAY)
//                .count();
//        long gradedEssays = answers.stream()
//                .filter(answer -> answer.getQuestion().getType() == QuestionType.ESSAY)
//                .filter(answer -> answer.getScore() != null && answer.getScore() > 0)
//                .count();
//
//        Map<String, Object> status = new HashMap<>();
//        status.put("sessionId", sessionId);
//        status.put("examStatus", exam.getStatus());
//        status.put("graded", exam.getGraded());
//        status.put("totalQuestions", totalQuestions);
//        status.put("autoGradedQuestions", autoGradedQuestions);
//        status.put("essayQuestions", essayQuestions);
//        status.put("gradedEssays", gradedEssays);
//        status.put("pendingEssays", essayQuestions - gradedEssays);
//        status.put("allEssaysGraded", gradedEssays == essayQuestions);
//        status.put("completionPercentage", essayQuestions > 0 ?
//                (double) gradedEssays / essayQuestions * 100 : 100.0);
//
//        return status;
//    }


    /**
     * Get detailed grading status - ENHANCED
     */
    public Map<String, Object> getGradingStatus(Long sessionId) {
        StudentExam exam = studentExamRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Exam session not found"));

        List<StudentAnswer> answers = studentAnswerRepository.findByStudentExam(exam);

        long totalQuestions = answers.size();
        long autoGradedQuestions = answers.stream()
                .filter(answer -> answer.getQuestion().getType() != QuestionType.ESSAY)
                .count();
        long essayQuestions = answers.stream()
                .filter(answer -> answer.getQuestion().getType() == QuestionType.ESSAY)
                .count();
        long gradedEssays = answers.stream()
                .filter(answer -> answer.getQuestion().getType() == QuestionType.ESSAY)
                .filter(answer -> answer.getScore() != null && answer.getScore() > 0)
                .count();

        // FIXED: If no essays and exam is graded, consider it fully graded
        boolean allEssaysGraded = essayQuestions == 0 ? true : (gradedEssays == essayQuestions);
        boolean isFullyGraded = Boolean.TRUE.equals(exam.getGraded()) &&
                (essayQuestions == 0 || allEssaysGraded);

        Map<String, Object> status = new HashMap<>();
        status.put("sessionId", sessionId);
        status.put("examStatus", exam.getStatus());
        status.put("graded", exam.getGraded());
        status.put("totalQuestions", totalQuestions);
        status.put("autoGradedQuestions", autoGradedQuestions);
        status.put("essayQuestions", essayQuestions);
        status.put("gradedEssays", gradedEssays);
        status.put("pendingEssays", essayQuestions - gradedEssays);
        status.put("allEssaysGraded", allEssaysGraded);
        status.put("fullyGraded", isFullyGraded); // NEW FIELD
        status.put("completionPercentage", essayQuestions > 0 ?
                (double) gradedEssays / essayQuestions * 100 : 100.0);

        return status;
    }


    /**
     * Trigger automatic grading for immediate results
     */
    @Transactional
    public void triggerImmediateGrading(Long sessionId) {
        try {
            log.info("Triggering immediate grading for session: {}", sessionId);
            examGradingService.gradeExam(sessionId);
            log.info("Immediate grading completed for session: {}", sessionId);
        } catch (Exception e) {
            log.error("Error in immediate grading for session {}: {}", sessionId, e.getMessage());
            throw new RuntimeException("Failed to grade exam: " + e.getMessage());
        }
    }

}