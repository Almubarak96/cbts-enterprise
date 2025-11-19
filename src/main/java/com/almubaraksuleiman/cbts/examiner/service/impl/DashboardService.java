//// DashboardService.java
//package com.almubaraksuleiman.cbts.examiner.service.impl;
//
//import com.almubaraksuleiman.cbts.examiner.SecurityUtils;
//import com.almubaraksuleiman.cbts.examiner.dto.*;
//import com.almubaraksuleiman.cbts.examiner.dto.AnalyticsDtos;
//import com.almubaraksuleiman.cbts.examiner.model.*;
//import com.almubaraksuleiman.cbts.examiner.repository.*;
//import com.almubaraksuleiman.cbts.student.model.StudentExam;
//import com.almubaraksuleiman.cbts.student.repository.StudentExamRepository;
//import com.almubaraksuleiman.cbts.student.repository.StudentRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.stereotype.Service;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class DashboardService {
//
//    private final TestRepository testRepository;
//    private final StudentRepository studentRepository;
//    private final ExaminerRepository examinerRepository;
//    private final StudentExamRepository studentExamRepository;
//    private final EnrollmentRepository enrollmentRepository;
//    private final QuestionRepository questionRepository;
//    private final SecurityUtils securityUtils;
//    private final AnalyticsService analyticsService;
//
//    /**
//     * Get comprehensive dashboard statistics
//     */
//    public DashboardStats getDashboardStats(String dateRange) {
//        log.info("Generating dashboard statistics for date range: {}", dateRange);
//
//        LocalDate[] dateRangeFilter = parseDateRange(dateRange);
//        boolean isAdmin = securityUtils.isAdmin();
//        Examiner currentExaminer = isAdmin ? null : securityUtils.getCurrentExaminer();
//
//        // Total counts
//        long totalTests = isAdmin ?
//            testRepository.count() :
//            testRepository.findByCreatedBy(currentExaminer, org.springframework.data.domain.Pageable.unpaged())
//                .getContent().size();
//
//        long totalStudents = studentRepository.count();
//        long totalExaminers = examinerRepository.count();
//
//        // Active tests (published)
//        long activeTests = isAdmin ?
//            testRepository.findByPublished(true, org.springframework.data.domain.Pageable.unpaged())
//                .getContent().size() :
//            testRepository.findByCreatedByAndPublished(currentExaminer, true,
//                org.springframework.data.domain.Pageable.unpaged()).getContent().size();
//
//        // Completed exams with date filtering
//        long completedExams = getCompletedExamsCount(dateRangeFilter, isAdmin, currentExaminer);
//
//        // Performance metrics
//        double averageScore = calculateAverageScore(dateRangeFilter, isAdmin, currentExaminer);
//        double passRate = calculatePassRate(dateRangeFilter, isAdmin, currentExaminer);
//        double enrollmentRate = calculateEnrollmentRate();
//
//        // Additional metrics
//        long pendingGrading = getPendingGradingCount(dateRangeFilter, isAdmin, currentExaminer);
//        long totalQuestions = questionRepository.count();
//
//        return DashboardStats.builder()
//                .totalTests((int) totalTests)
//                .totalStudents((int) totalStudents)
//                .totalExaminers((int) totalExaminers)
//                .activeTests((int) activeTests)
//                .completedExams((int) completedExams)
//                .averageScore(Math.round(averageScore * 100.0) / 100.0)
//                .passRate(Math.round(passRate * 100.0) / 100.0)
//                .enrollmentRate(Math.round(enrollmentRate * 100.0) / 100.0)
//                .pendingGrading((int) pendingGrading)
//                .totalQuestions((int) totalQuestions)
//                .build();
//    }
//
//    /**
//     * Get platform overview with charts data
//     */
//    public PlatformOverview getPlatformOverview(String dateRange) {
//        log.info("Generating platform overview for date range: {}", dateRange);
//
//        LocalDate[] dateRangeFilter = parseDateRange(dateRange);
//        boolean isAdmin = securityUtils.isAdmin();
//        Examiner currentExaminer = isAdmin ? null : securityUtils.getCurrentExaminer();
//
//        // Tests by status
//        List<Map<String, Object>> testsByStatus = getTestsByStatus(isAdmin, currentExaminer);
//
//        // Student performance distribution
//        List<Map<String, Object>> studentPerformance = getStudentPerformanceDistribution(dateRangeFilter, isAdmin, currentExaminer);
//
//        // Question type distribution
//        List<Map<String, Object>> questionDistribution = getQuestionTypeDistribution(isAdmin, currentExaminer);
//
//        // Enrollment trends
//        List<Map<String, Object>> enrollmentTrends = getEnrollmentTrends(dateRangeFilter);
//
//        // Recent activity
//        List<Map<String, Object>> recentActivity = getRecentActivity(10);
//
//        // Top performers
//        List<Map<String, Object>> topPerformers = getTopPerformers(dateRangeFilter, isAdmin, currentExaminer);
//
//        // Test performance
//        List<Map<String, Object>> testPerformance = getTestPerformance(dateRangeFilter, isAdmin, currentExaminer);
//
//        return PlatformOverview.builder()
//                .testsByStatus(testsByStatus)
//                .studentPerformance(studentPerformance)
//                .questionDistribution(questionDistribution)
//                .enrollmentTrends(enrollmentTrends)
//                .recentActivity(recentActivity)
//                .topPerformers(topPerformers)
//                .testPerformance(testPerformance)
//                .build();
//    }
//
//    /**
//     * Get detailed analytics data
//     */
//    public AnalyticsDtos getAnalyticsData(Long testId, String dateRange) {
//        log.info("Generating analytics data for test: {}, date range: {}", testId, dateRange);
//
//        if (testId != null) {
//            // Return test-specific analytics
//            AnalyticsFilters filters = AnalyticsFilters.builder()
//                    .dateRange(dateRange)
//                    .build();
//
//            com.almubaraksuleiman.cbts.examiner.model.AnalyticsData testAnalytics =
//                analyticsService.getTestAnalytics(testId, filters);
//
//            return convertToAnalyticsData(testAnalytics);
//        } else {
//            // Return platform-wide analytics
//            return getPlatformAnalytics(dateRange);
//        }
//    }
//
//    /**
//     * Get examiner-specific dashboard data
//     */
//    /**
//     * Get examiner-specific dashboard data
//     */
//    public Map<String, Object> getExaminerDashboardData() {
//        Examiner examiner = securityUtils.getCurrentExaminer();
//        log.info("Generating examiner dashboard data for: {}", examiner.getUsername());
//
//        // FIXED: Use the new repository methods
//        long myTests = testRepository.findByCreatedBy(examiner, org.springframework.data.domain.Pageable.unpaged())
//                .getContent().size();
//
//        long myPublishedTests = testRepository.findByCreatedByAndPublished(examiner, true,
//                org.springframework.data.domain.Pageable.unpaged()).getContent().size();
//
//        // FIXED: Use the new countDistinctStudentsByExaminer method
//        long myStudents = enrollmentRepository.countDistinctStudentsByExaminer(examiner);
//
//        // FIXED: Get enrollments count for examiner's tests
//        long totalEnrollments = enrollmentRepository.countByTestCreatedBy(examiner);
//
//        // FIXED: Get active enrollments (ENROLLED status)
//        long activeEnrollments = enrollmentRepository.findByTestCreatedByAndStatus(examiner,
//                Enrollment.EnrollmentStatus.ENROLLED).size();
//
//        // Recent activity for examiner's tests
//        List<Map<String, Object>> myRecentActivity = getExaminerRecentActivity(examiner, 5);
//
//        // Performance metrics for examiner's tests
//        Map<String, Object> performanceMetrics = getExaminerPerformanceMetrics(examiner);
//
//        Map<String, Object> examinerData = new HashMap<>();
//        examinerData.put("myTests", myTests);
//        examinerData.put("myPublishedTests", myPublishedTests);
//        examinerData.put("myStudents", myStudents);
//        examinerData.put("totalEnrollments", totalEnrollments);
//        examinerData.put("activeEnrollments", activeEnrollments);
//        examinerData.put("recentActivity", myRecentActivity);
//        examinerData.put("performanceMetrics", performanceMetrics);
//
//        return examinerData;
//    }
//
//    /**
//     * Get recent activity feed
//     */
//    public List<Map<String, Object>> getRecentActivity(int limit) {
//        List<Map<String, Object>> activities = new ArrayList<>();
//
//        // Add test creation activities
//        List<Test> recentTests = testRepository.findAll(org.springframework.data.domain.PageRequest.of(0, limit))
//                .getContent();
//
//        for (Test test : recentTests) {
//            Map<String, Object> activity = new HashMap<>();
//            activity.put("type", "test_created");
//            activity.put("description", String.format("New test created: %s", test.getTitle()));
//            activity.put("time", formatTimeAgo(test.getCreatedAt()));
//            activity.put("icon", "bi-card-checklist");
//            activity.put("examiner", test.getCreatedBy().getUsername());
//            activities.add(activity);
//        }
//
//        // Add exam completion activities
//        List<StudentExam> recentExams = studentExamRepository.findAll(org.springframework.data.domain.PageRequest.of(0, limit))
//                .getContent();
//
//        for (StudentExam exam : recentExams) {
//            if (Boolean.TRUE.equals(exam.getCompleted())) {
//                Map<String, Object> activity = new HashMap<>();
//                activity.put("type", "exam_completed");
//                activity.put("description", String.format("Exam completed for: %s", exam.getTest().getTitle()));
//                activity.put("time", formatTimeAgo(exam.getEndTime()));
//                activity.put("icon", "bi-check-circle");
//                activity.put("studentId", exam.getStudentId());
//                activities.add(activity);
//            }
//        }
//
//        // Sort by time and limit
//        return activities.stream()
//                .sorted((a1, a2) -> ((String) a2.get("time")).compareTo((String) a1.get("time")))
//                .limit(limit)
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * Export dashboard report
//     */
//    public byte[] exportDashboardReport(String format, String dateRange) {
//        log.info("Exporting dashboard report in {} format", format);
//
//        try {
//            switch (format.toLowerCase()) {
//                case "pdf":
//                    return generatePdfReport(dateRange);
//                case "excel":
//                    return generateExcelReport(dateRange);
//                case "csv":
//                    return generateCsvReport(dateRange);
//                default:
//                    throw new IllegalArgumentException("Unsupported format: " + format);
//            }
//        } catch (IOException e) {
//            log.error("Error generating dashboard report: {}", e.getMessage());
//            throw new RuntimeException("Failed to generate dashboard report", e);
//        }
//    }
//
//    // Private helper methods
//
//    private LocalDate[] parseDateRange(String dateRange) {
//        LocalDate now = LocalDate.now();
//        if (dateRange == null || "all".equals(dateRange)) {
//            return new LocalDate[]{null, null};
//        }
//
//        switch (dateRange) {
//            case "today":
//                return new LocalDate[]{now, now};
//            case "week":
//                return new LocalDate[]{now.minusWeeks(1), now};
//            case "month":
//                return new LocalDate[]{now.minusMonths(1), now};
//            case "year":
//                return new LocalDate[]{now.minusYears(1), now};
//            default:
//                return new LocalDate[]{null, null};
//        }
//    }
//
//    private long getCompletedExamsCount(LocalDate[] dateRange, boolean isAdmin, Examiner examiner) {
//        // Implementation for counting completed exams with date filtering
//        List<StudentExam> allExams = studentExamRepository.findAll();
//
//        return allExams.stream()
//                .filter(exam -> Boolean.TRUE.equals(exam.getCompleted()))
//                .filter(exam -> isAdmin || exam.getTest().getCreatedBy().equals(examiner))
//                .filter(exam -> filterByDateRange(exam.getEndTime(), dateRange))
//                .count();
//    }
//
//    private double calculateAverageScore(LocalDate[] dateRange, boolean isAdmin, Examiner examiner) {
//        List<StudentExam> completedExams = studentExamRepository.findAll().stream()
//                .filter(exam -> Boolean.TRUE.equals(exam.getCompleted()))
//                .filter(exam -> exam.getPercentage() != null)
//                .filter(exam -> isAdmin || exam.getTest().getCreatedBy().equals(examiner))
//                .filter(exam -> filterByDateRange(exam.getEndTime(), dateRange))
//                .collect(Collectors.toList());
//
//        return completedExams.stream()
//                .mapToDouble(StudentExam::getPercentage)
//                .average()
//                .orElse(0.0);
//    }
//
//    private double calculatePassRate(LocalDate[] dateRange, boolean isAdmin, Examiner examiner) {
//        List<StudentExam> completedExams = studentExamRepository.findAll().stream()
//                .filter(exam -> Boolean.TRUE.equals(exam.getCompleted()))
//                .filter(exam -> exam.getPercentage() != null)
//                .filter(exam -> isAdmin || exam.getTest().getCreatedBy().equals(examiner))
//                .filter(exam -> filterByDateRange(exam.getEndTime(), dateRange))
//                .collect(Collectors.toList());
//
//        if (completedExams.isEmpty()) return 0.0;
//
//        long passedExams = completedExams.stream()
//                .filter(exam -> exam.getPercentage() >= exam.getTest().getPassingScore())
//                .count();
//
//        return (double) passedExams / completedExams.size() * 100;
//    }
//
//    private double calculateEnrollmentRate() {
//        long totalStudents = studentRepository.count();
//        long enrolledStudents = enrollmentRepository.count();
//
//        return totalStudents > 0 ? (double) enrolledStudents / totalStudents * 100 : 0.0;
//    }
//
//    private long getPendingGradingCount(LocalDate[] dateRange, boolean isAdmin, Examiner examiner) {
//        return studentExamRepository.findAll().stream()
//                .filter(exam -> Boolean.TRUE.equals(exam.getCompleted()))
//                .filter(exam -> !Boolean.TRUE.equals(exam.getGraded()))
//                .filter(exam -> isAdmin || exam.getTest().getCreatedBy().equals(examiner))
//                .filter(exam -> filterByDateRange(exam.getEndTime(), dateRange))
//                .count();
//    }
//
//    private boolean filterByDateRange(LocalDateTime dateTime, LocalDate[] dateRange) {
//        if (dateTime == null || dateRange[0] == null) return true;
//
//        LocalDate date = dateTime.toLocalDate();
//        return !date.isBefore(dateRange[0]) && !date.isAfter(dateRange[1]);
//    }
//
//    private String formatTimeAgo(LocalDateTime dateTime) {
//        if (dateTime == null) return "Unknown time";
//
//        java.time.Duration duration = java.time.Duration.between(dateTime, LocalDateTime.now());
//
//        if (duration.toMinutes() < 1) {
//            return "Just now";
//        } else if (duration.toHours() < 1) {
//            return duration.toMinutes() + " minutes ago";
//        } else if (duration.toDays() < 1) {
//            return duration.toHours() + " hours ago";
//        } else if (duration.toDays() < 7) {
//            return duration.toDays() + " days ago";
//        } else {
//            return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
//        }
//    }
//
//    // Additional helper methods for charts data...
//    private List<Map<String, Object>> getTestsByStatus(boolean isAdmin, Examiner examiner) {
//        // Implementation for tests by status chart data
//        return new ArrayList<>();
//    }
//
//    private List<Map<String, Object>> getStudentPerformanceDistribution(LocalDate[] dateRange, boolean isAdmin, Examiner examiner) {
//        // Implementation for student performance distribution
//        return new ArrayList<>();
//    }
//
//    private List<Map<String, Object>> getQuestionTypeDistribution(boolean isAdmin, Examiner examiner) {
//        // Implementation for question type distribution
//        return new ArrayList<>();
//    }
//
//    private List<Map<String, Object>> getEnrollmentTrends(LocalDate[] dateRange) {
//        // Implementation for enrollment trends
//        return new ArrayList<>();
//    }
//
//    private List<Map<String, Object>> getTopPerformers(LocalDate[] dateRange, boolean isAdmin, Examiner examiner) {
//        // Implementation for top performers
//        return new ArrayList<>();
//    }
//
//    private List<Map<String, Object>> getTestPerformance(LocalDate[] dateRange, boolean isAdmin, Examiner examiner) {
//        // Implementation for test performance
//        return new ArrayList<>();
//    }
//
//    private List<Map<String, Object>> getExaminerRecentActivity(Examiner examiner, int limit) {
//        List<Map<String, Object>> activities = new ArrayList<>();
//
//        // Get examiner's recent tests
//        List<Test> recentTests = testRepository.findByCreatedBy(examiner,
//                org.springframework.data.domain.PageRequest.of(0, limit)).getContent();
//
//        for (Test test : recentTests) {
//            Map<String, Object> activity = new HashMap<>();
//            activity.put("type", "test_created");
//            activity.put("description", String.format("You created: %s", test.getTitle()));
//            activity.put("time", formatTimeAgo(test.getCreatedAt()));
//            activity.put("icon", "bi-card-checklist");
//            activity.put("testId", test.getId());
//            activities.add(activity);
//        }
//
//        // Get recent enrollments in examiner's tests
//        List<Enrollment> recentEnrollments = enrollmentRepository.findByTestCreatedBy(examiner)
//                .stream()
//                .sorted((e1, e2) -> e2.getEnrolledAt().compareTo(e1.getEnrolledAt()))
//                .limit(limit)
//                .collect(Collectors.toList());
//
//        for (Enrollment enrollment : recentEnrollments) {
//            Map<String, Object> activity = new HashMap<>();
//            activity.put("type", "student_enrolled");
//            activity.put("description", String.format("%s enrolled in %s",
//                    enrollment.getStudent().getFullName(), enrollment.getTest().getTitle()));
//            activity.put("time", formatTimeAgo(enrollment.getEnrolledAt()));
//            activity.put("icon", "bi-person-plus");
//            activity.put("studentId", enrollment.getStudent().getId());
//            activity.put("testId", enrollment.getTest().getId());
//            activities.add(activity);
//        }
//
//        // Sort by time and limit
//        return activities.stream()
//                .sorted((a1, a2) -> ((String) a2.get("time")).compareTo((String) a1.get("time")))
//                .limit(limit)
//                .collect(Collectors.toList());
//    }
//
//    private Map<String, Object> getExaminerPerformanceMetrics(Examiner examiner) {
//        Map<String, Object> metrics = new HashMap<>();
//
//        // Get examiner's tests
//        List<Test> examinerTests = testRepository.findByCreatedBy(examiner,
//                org.springframework.data.domain.Pageable.unpaged()).getContent();
//
//        // Calculate average score for examiner's tests
//        double averageScore = examinerTests.stream()
//                .flatMap(test -> studentExamRepository.findByTestId(test.getId()).stream())
//                .filter(exam -> Boolean.TRUE.equals(exam.getCompleted()) && exam.getPercentage() != null)
//                .mapToDouble(StudentExam::getPercentage)
//                .average()
//                .orElse(0.0);
//
//        // Calculate completion rate
//        long totalEnrollments = enrollmentRepository.countByTestCreatedBy(examiner);
//        long completedExams = examinerTests.stream()
//                .mapToLong(test -> studentExamRepository.findByTestId(test.getId()).stream()
//                        .filter(exam -> Boolean.TRUE.equals(exam.getCompleted()))
//                        .count())
//                .sum();
//
//        double completionRate = totalEnrollments > 0 ? (double) completedExams / totalEnrollments * 100 : 0.0;
//
//        // Calculate pass rate
//        long passedExams = examinerTests.stream()
//                .flatMap(test -> studentExamRepository.findByTestId(test.getId()).stream())
//                .filter(exam -> Boolean.TRUE.equals(exam.getCompleted()) && exam.getPercentage() != null)
//                .filter(exam -> exam.getPercentage() >= exam.getTest().getPassingScore())
//                .count();
//
//        double passRate = completedExams > 0 ? (double) passedExams / completedExams * 100 : 0.0;
//
//        metrics.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
//        metrics.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
//        metrics.put("passRate", Math.round(passRate * 100.0) / 100.0);
//        metrics.put("totalEnrollments", totalEnrollments);
//        metrics.put("completedExams", completedExams);
//
//        return metrics;
//    }
//
//    private AnalyticsDtos convertToAnalyticsData(com.almubaraksuleiman.cbts.examiner.model.AnalyticsData testAnalytics) {
//        // Convert test analytics to dashboard analytics format
//        return AnalyticsDtos.builder().build(); // Simplified implementation
//    }
//
//    private AnalyticsDtos getPlatformAnalytics(String dateRange) {
//        // Implementation for platform-wide analytics
//        return AnalyticsDtos.builder().build(); // Simplified implementation
//    }
//
//    private byte[] generatePdfReport(String dateRange) throws IOException {
//        // Implementation for PDF report generation
//        return new byte[0]; // Simplified implementation
//    }
//
//    private byte[] generateExcelReport(String dateRange) throws IOException {
//        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
//            Sheet sheet = workbook.createSheet("Dashboard Report");
//
//            // Create header row
//            Row headerRow = sheet.createRow(0);
//            String[] headers = {"Metric", "Value"};
//            for (int i = 0; i < headers.length; i++) {
//                Cell cell = headerRow.createCell(i);
//                cell.setCellValue(headers[i]);
//            }
//
//            // Add data rows
//            DashboardStats stats = getDashboardStats(dateRange);
//            int rowNum = 1;
//
//            addRow(sheet, rowNum++, "Total Tests", String.valueOf(stats.getTotalTests()));
//            addRow(sheet, rowNum++, "Total Students", String.valueOf(stats.getTotalStudents()));
//            addRow(sheet, rowNum++, "Average Score", String.valueOf(stats.getAverageScore()));
//            addRow(sheet, rowNum++, "Pass Rate", String.valueOf(stats.getPassRate()));
//
//            workbook.write(out);
//            return out.toByteArray();
//        }
//    }
//
//    private void addRow(Sheet sheet, int rowNum, String metric, String value) {
//        Row row = sheet.createRow(rowNum);
//        row.createCell(0).setCellValue(metric);
//        row.createCell(1).setCellValue(value);
//    }
//
//    private byte[] generateCsvReport(String dateRange) {
//        DashboardStats stats = getDashboardStats(dateRange);
//        String csv = String.format(
//            "Metric,Value\nTotal Tests,%d\nTotal Students,%d\nAverage Score,%.2f\nPass Rate,%.2f",
//            stats.getTotalTests(), stats.getTotalStudents(), stats.getAverageScore(), stats.getPassRate()
//        );
//        return csv.getBytes();
//    }
//}



package com.almubaraksuleiman.cbts.examiner.service.impl;

import com.almubaraksuleiman.cbts.examiner.SecurityUtils;
import com.almubaraksuleiman.cbts.examiner.dto.*;
import com.almubaraksuleiman.cbts.examiner.model.*;
import com.almubaraksuleiman.cbts.examiner.repository.*;
import com.almubaraksuleiman.cbts.student.model.StudentExam;
import com.almubaraksuleiman.cbts.student.repository.StudentExamRepository;
import com.almubaraksuleiman.cbts.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final TestRepository testRepository;
    private final StudentRepository studentRepository;
    private final ExaminerRepository examinerRepository;
    private final StudentExamRepository studentExamRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final QuestionRepository questionRepository;
    private final SecurityUtils securityUtils;

    /**
     * Get comprehensive dashboard statistics with role-based filtering
     */
    public DashboardStats getDashboardStats(String dateRange) {
        log.info("Generating dashboard statistics for date range: {}", dateRange);

        LocalDate[] dateRangeFilter = parseDateRange(dateRange);
        boolean isAdmin = securityUtils.isAdmin();
        Examiner currentExaminer = isAdmin ? null : securityUtils.getCurrentExaminer();

        try {
            // Role-based data calculation
            long totalTests = getTotalTests(isAdmin, currentExaminer);
            long totalStudents = getTotalStudents(isAdmin, currentExaminer);
            long totalExaminers = getTotalExaminers(isAdmin);
            long activeTests = getActiveTests(isAdmin, currentExaminer);
            long completedExams = getCompletedExamsCount(dateRangeFilter, isAdmin, currentExaminer);
            double averageScore = calculateAverageScore(dateRangeFilter, isAdmin, currentExaminer);
            double passRate = calculatePassRate(dateRangeFilter, isAdmin, currentExaminer);
            double enrollmentRate = calculateEnrollmentRate(isAdmin, currentExaminer);
            long pendingGrading = getPendingGradingCount(dateRangeFilter, isAdmin, currentExaminer);
            long totalQuestions = getTotalQuestions(isAdmin, currentExaminer);

            return DashboardStats.builder()
                    .totalTests((int) totalTests)
                    .totalStudents((int) totalStudents)
                    .totalExaminers((int) totalExaminers)
                    .activeTests((int) activeTests)
                    .completedExams((int) completedExams)
                    .averageScore(Math.round(averageScore * 100.0) / 100.0)
                    .passRate(Math.round(passRate * 100.0) / 100.0)
                    .enrollmentRate(Math.round(enrollmentRate * 100.0) / 100.0)
                    .pendingGrading((int) pendingGrading)
                    .totalQuestions((int) totalQuestions)
                    .build();

        } catch (Exception e) {
            log.error("Error generating dashboard stats: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate dashboard statistics", e);
        }
    }

    /**
     * Get platform overview with real charts data
     */
    public PlatformOverview getPlatformOverview(String dateRange) {
        log.info("Generating platform overview for date range: {}", dateRange);

        LocalDate[] dateRangeFilter = parseDateRange(dateRange);
        boolean isAdmin = securityUtils.isAdmin();
        Examiner currentExaminer = isAdmin ? null : securityUtils.getCurrentExaminer();

        try {
            // Real data for charts
            List<ChartData> testsByStatus = getTestsByStatusData(isAdmin, currentExaminer);
            List<ChartData> studentPerformance = getStudentPerformanceData(dateRangeFilter, isAdmin, currentExaminer);
            List<ChartData> questionDistribution = getQuestionTypeDistributionData(isAdmin, currentExaminer);
            List<TrendData> enrollmentTrends = getEnrollmentTrendsData(dateRangeFilter, isAdmin, currentExaminer);
            List<ActivityData> recentActivity = getRecentActivityData(10, isAdmin, currentExaminer);
            List<StudentPerformance> topPerformers = getTopPerformersData(dateRangeFilter, isAdmin, currentExaminer);
            List<ChartData> testPerformance = getTestPerformanceData(dateRangeFilter, isAdmin, currentExaminer);
            List<ChartData> scoreDistribution = getScoreDistributionData(dateRangeFilter, isAdmin, currentExaminer);

            return PlatformOverview.builder()
                    .testsByStatus(testsByStatus)
                    .studentPerformance(studentPerformance)
                    .questionDistribution(questionDistribution)
                    .enrollmentTrends(enrollmentTrends)
                    .recentActivity(recentActivity)
                    .topPerformers(topPerformers)
                    .testPerformance(testPerformance)
                    .scoreDistribution(scoreDistribution)
                    .build();

        } catch (Exception e) {
            log.error("Error generating platform overview: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate platform overview", e);
        }
    }

    /**
     * Get examiner-specific dashboard data
     */
    public Map<String, Object> getExaminerDashboardData() {
        Examiner examiner = securityUtils.getCurrentExaminer();
        log.info("Generating examiner dashboard data for: {}", examiner.getUsername());

        try {
            long myTests = testRepository.findByCreatedBy(examiner, org.springframework.data.domain.Pageable.unpaged())
                    .getContent().size();

            long myPublishedTests = testRepository.findByCreatedByAndPublished(examiner, true,
                    org.springframework.data.domain.Pageable.unpaged()).getContent().size();

            long myStudents = enrollmentRepository.countDistinctStudentsByExaminer(examiner);
            long totalEnrollments = enrollmentRepository.countByTestCreatedBy(examiner);
            long activeEnrollments = enrollmentRepository.findByTestCreatedByAndStatus(examiner,
                    Enrollment.EnrollmentStatus.ENROLLED).size();

            List<Map<String, Object>> myRecentActivity = getExaminerRecentActivity(examiner, 5);
            Map<String, Object> performanceMetrics = getExaminerPerformanceMetrics(examiner);

            Map<String, Object> examinerData = new HashMap<>();
            examinerData.put("myTests", myTests);
            examinerData.put("myPublishedTests", myPublishedTests);
            examinerData.put("myStudents", myStudents);
            examinerData.put("totalEnrollments", totalEnrollments);
            examinerData.put("activeEnrollments", activeEnrollments);
            examinerData.put("recentActivity", myRecentActivity);
            examinerData.put("performanceMetrics", performanceMetrics);

            return examinerData;

        } catch (Exception e) {
            log.error("Error generating examiner dashboard data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate examiner dashboard data", e);
        }
    }

    /**
     * Get recent activity for the dashboard (public method for controller)
     */
    public List<ActivityData> getRecentActivity(int limit) {
        log.info("Fetching recent activity with limit: {}", limit);
        boolean isAdmin = securityUtils.isAdmin();
        Examiner currentExaminer = isAdmin ? null : securityUtils.getCurrentExaminer();
        return getRecentActivityData(limit, isAdmin, currentExaminer);
    }

    /**
     * Export dashboard report (public method for controller)
     */
    public byte[] exportDashboardReport(String format, String dateRange) {
        log.info("Exporting dashboard report in {} format for date range: {}", format, dateRange);

        try {
            DashboardStats stats = getDashboardStats(dateRange);
            PlatformOverview overview = getPlatformOverview(dateRange);

            // Simple CSV export implementation - you can enhance this for PDF/Excel
            StringBuilder csv = new StringBuilder();
            csv.append("Dashboard Report\n");
            csv.append("Date Range:,").append(dateRange != null ? dateRange : "all").append("\n");
            csv.append("Generated:,").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");

            csv.append("Key Metrics\n");
            csv.append("Metric,Value\n");
            csv.append("Total Tests,").append(stats.getTotalTests()).append("\n");
            csv.append("Total Students,").append(stats.getTotalStudents()).append("\n");
            csv.append("Total Examiners,").append(stats.getTotalExaminers()).append("\n");
            csv.append("Active Tests,").append(stats.getActiveTests()).append("\n");
            csv.append("Completed Exams,").append(stats.getCompletedExams()).append("\n");
            csv.append("Average Score,").append(stats.getAverageScore()).append("%\n");
            csv.append("Pass Rate,").append(stats.getPassRate()).append("%\n");
            csv.append("Enrollment Rate,").append(stats.getEnrollmentRate()).append("%\n");
            csv.append("Pending Grading,").append(stats.getPendingGrading()).append("\n");
            csv.append("Total Questions,").append(stats.getTotalQuestions()).append("\n\n");

            csv.append("Test Status Distribution\n");
            csv.append("Status,Count\n");
            for (ChartData data : overview.getTestsByStatus()) {
                csv.append(data.getName()).append(",").append(data.getValue()).append("\n");
            }
            csv.append("\n");

            csv.append("Score Distribution\n");
            csv.append("Score Range,Student Count\n");
            for (ChartData data : overview.getScoreDistribution()) {
                csv.append(data.getName()).append(",").append(data.getValue()).append("\n");
            }
            csv.append("\n");

            csv.append("Recent Activity\n");
            csv.append("Type,Description,Time\n");
            for (ActivityData activity : overview.getRecentActivity()) {
                csv.append(activity.getType()).append(",")
                        .append(activity.getDescription().replace(",", ";")).append(",")
                        .append(activity.getTime()).append("\n");
            }

            return csv.toString().getBytes();

        } catch (Exception e) {
            log.error("Error exporting dashboard report: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to export dashboard report", e);
        }
    }

    // ========== PRIVATE HELPER METHODS ==========
    // ... (all your existing private helper methods remain the same)
    // Keep all the existing private methods like:
    // getTotalTests, getTotalStudents, getTestsByStatusData, etc.
    // They don't need to change

    private long getTotalTests(boolean isAdmin, Examiner examiner) {
        return isAdmin ?
                testRepository.count() :
                testRepository.findByCreatedBy(examiner, org.springframework.data.domain.Pageable.unpaged())
                        .getContent().size();
    }

    private long getTotalStudents(boolean isAdmin, Examiner examiner) {
        return isAdmin ?
                studentRepository.count() :
                enrollmentRepository.countDistinctStudentsByExaminer(examiner);
    }

    private long getTotalExaminers(boolean isAdmin) {
        return isAdmin ? examinerRepository.count() : 0;
    }

    private long getActiveTests(boolean isAdmin, Examiner examiner) {
        return isAdmin ?
                testRepository.findByPublished(true, org.springframework.data.domain.Pageable.unpaged())
                        .getContent().size() :
                testRepository.findByCreatedByAndPublished(examiner, true,
                        org.springframework.data.domain.Pageable.unpaged()).getContent().size();
    }

    private long getTotalQuestions(boolean isAdmin, Examiner examiner) {
        return isAdmin ?
                questionRepository.count() :
                getExaminerQuestionsCount(examiner);
    }

    private List<ChartData> getTestsByStatusData(boolean isAdmin, Examiner examiner) {
        List<Test> tests = isAdmin ?
                testRepository.findAll() :
                testRepository.findByCreatedBy(examiner, org.springframework.data.domain.Pageable.unpaged()).getContent();

        long published = tests.stream().filter(Test::getPublished).count();
        long draft = tests.stream().filter(t -> !t.getPublished()).count();
        long archived = tests.stream().filter(t -> t.getCreatedAt().isBefore(LocalDateTime.now().minusMonths(6))).count();

        return Arrays.asList(
                ChartData.builder().name("Published").value(published).color("#198754").build(),
                ChartData.builder().name("Draft").value(draft).color("#ffc107").build(),
                ChartData.builder().name("Archived").value(archived).color("#6c757d").build()
        );
    }

    private List<ChartData> getStudentPerformanceData(LocalDate[] dateRange, boolean isAdmin, Examiner examiner) {
        List<StudentExam> exams = getFilteredExams(dateRange, isAdmin, examiner);

        Map<String, Long> scoreRanges = exams.stream()
                .filter(exam -> exam.getPercentage() != null)
                .collect(Collectors.groupingBy(
                        exam -> {
                            double score = exam.getPercentage();
                            if (score <= 20) return "0-20%";
                            else if (score <= 40) return "21-40%";
                            else if (score <= 60) return "41-60%";
                            else if (score <= 80) return "61-80%";
                            else return "81-100%";
                        },
                        Collectors.counting()
                ));

        return Arrays.asList(
                ChartData.builder().name("0-20%").value(scoreRanges.getOrDefault("0-20%", 0L)).color("#dc3545").build(),
                ChartData.builder().name("21-40%").value(scoreRanges.getOrDefault("21-40%", 0L)).color("#fd7e14").build(),
                ChartData.builder().name("41-60%").value(scoreRanges.getOrDefault("41-60%", 0L)).color("#ffc107").build(),
                ChartData.builder().name("61-80%").value(scoreRanges.getOrDefault("61-80%", 0L)).color("#20c997").build(),
                ChartData.builder().name("81-100%").value(scoreRanges.getOrDefault("81-100%", 0L)).color("#198754").build()
        );
    }

    private List<ChartData> getQuestionTypeDistributionData(boolean isAdmin, Examiner examiner) {
        // Get real question type distribution from database
        List<Question> questions = isAdmin ?
                questionRepository.findAll() :
                getExaminerQuestions(examiner);

        Map<QuestionType, Long> typeCounts = questions.stream()
                .collect(Collectors.groupingBy(Question::getType, Collectors.counting()));

        return Arrays.asList(
                ChartData.builder().name("Multiple Choice").value(typeCounts.getOrDefault(QuestionType.MULTIPLE_CHOICE, 0L)).color("#0d6efd").build(),
                ChartData.builder().name("True/False").value(typeCounts.getOrDefault(QuestionType.TRUE_FALSE, 0L)).color("#198754").build(),
                ChartData.builder().name("Fill in Blank").value(typeCounts.getOrDefault(QuestionType.FILL_IN_THE_BLANK, 0L)).color("#ffc107").build(),
                ChartData.builder().name("Essay").value(typeCounts.getOrDefault(QuestionType.ESSAY, 0L)).color("#dc3545").build(),
                ChartData.builder().name("Multiple Select").value(typeCounts.getOrDefault(QuestionType.MULTIPLE_SELECT, 0L)).color("#6f42c1").build(),
                ChartData.builder().name("Matching").value(typeCounts.getOrDefault(QuestionType.MATCHING, 0L)).color("#fd7e14").build()
        );
    }

    private List<TrendData> getEnrollmentTrendsData(LocalDate[] dateRange, boolean isAdmin, Examiner examiner) {
        List<TrendData> trends = new ArrayList<>();
        LocalDate endDate = dateRange[1] != null ? dateRange[1] : LocalDate.now();
        LocalDate startDate = dateRange[0] != null ? dateRange[0] : endDate.minusDays(6);

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDate finalDate = date;
            long enrollments = enrollmentRepository.findAll().stream()
                    .filter(e -> e.getEnrolledAt() != null && e.getEnrolledAt().toLocalDate().equals(finalDate))
                    .filter(e -> isAdmin || e.getTest().getCreatedBy().equals(examiner))
                    .count();

            trends.add(TrendData.builder()
                    .date(date.format(DateTimeFormatter.ofPattern("MMM dd")))
                    .value(enrollments)
                    .build());
        }

        return trends;
    }

    private List<ActivityData> getRecentActivityData(int limit, boolean isAdmin, Examiner examiner) {
        List<ActivityData> activities = new ArrayList<>();

        // Test creation activities
        List<Test> recentTests = isAdmin ?
                testRepository.findAll(PageRequest.of(0, limit)).getContent() :
                testRepository.findByCreatedBy(examiner, PageRequest.of(0, limit)).getContent();

        for (Test test : recentTests) {
            activities.add(ActivityData.builder()
                    .type("test_created")
                    .description(String.format("New test created: %s", test.getTitle()))
                    .time(formatTimeAgo(test.getCreatedAt()))
                    .icon("bi-card-checklist")
                    .metadata(Map.of("examiner", test.getCreatedBy().getUsername(), "testId", test.getId()))
                    .build());
        }

        // Exam completion activities
        List<StudentExam> recentExams = studentExamRepository.findAll(PageRequest.of(0, limit)).getContent();
        for (StudentExam exam : recentExams) {
            if (Boolean.TRUE.equals(exam.getCompleted()) &&
                    (isAdmin || exam.getTest().getCreatedBy().equals(examiner))) {
                activities.add(ActivityData.builder()
                        .type("exam_completed")
                        .description(String.format("Exam completed for: %s", exam.getTest().getTitle()))
                        .time(formatTimeAgo(exam.getEndTime()))
                        .icon("bi-check-circle")
                        .metadata(Map.of("studentId", exam.getStudentId(), "testId", exam.getTest().getId()))
                        .build());
            }
        }

        return activities.stream()
                .sorted((a1, a2) -> a2.getTime().compareTo(a1.getTime()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<StudentPerformance> getTopPerformersData(LocalDate[] dateRange, boolean isAdmin, Examiner examiner) {
        List<StudentExam> exams = getFilteredExams(dateRange, isAdmin, examiner);

        return exams.stream()
                .filter(exam -> exam.getPercentage() != null)
                .sorted((e1, e2) -> Double.compare(e2.getPercentage(), e1.getPercentage()))
                .limit(10)
                .map(exam -> StudentPerformance.builder()
                        .studentId(String.valueOf(exam.getStudentId()))
                        .name("Student " + exam.getStudentId())
                        .score(exam.getPercentage())
                        .timeSpent(calculateTimeSpent(exam))
                        .status("completed")
                        .submittedAt(exam.getEndTime())
                        .build())
                .collect(Collectors.toList());
    }

    private List<ChartData> getTestPerformanceData(LocalDate[] dateRange, boolean isAdmin, Examiner examiner) {
        List<Test> tests = isAdmin ?
                testRepository.findAll() :
                testRepository.findByCreatedBy(examiner, org.springframework.data.domain.Pageable.unpaged()).getContent();

        return tests.stream()
                .map(test -> {
                    double avgScore = studentExamRepository.findByTestId(test.getId()).stream()
                            .filter(exam -> exam.getPercentage() != null)
                            .mapToDouble(StudentExam::getPercentage)
                            .average()
                            .orElse(0.0);
                    return ChartData.builder()
                            .name(test.getTitle())
                            .value(Math.round(avgScore * 10.0) / 10.0)
                            .color(getRandomColor())
                            .build();
                })
                .limit(8)
                .collect(Collectors.toList());
    }

    private List<ChartData> getScoreDistributionData(LocalDate[] dateRange, boolean isAdmin, Examiner examiner) {
        List<StudentExam> exams = getFilteredExams(dateRange, isAdmin, examiner);

        List<ChartData> distribution = new ArrayList<>();
        for (int i = 0; i <= 100; i += 10) {
            final int rangeStart = i;
            long count = exams.stream()
                    .filter(exam -> exam.getPercentage() != null)
                    .filter(exam -> exam.getPercentage() >= rangeStart && exam.getPercentage() < rangeStart + 10)
                    .count();
            distribution.add(ChartData.builder()
                    .name(rangeStart + "%")
                    .value(count)
                    .color(getColorForScore(rangeStart))
                    .build());
        }

        return distribution;
    }

    // ========== UTILITY METHODS ==========

    private List<StudentExam> getFilteredExams(LocalDate[] dateRange, boolean isAdmin, Examiner examiner) {
        return studentExamRepository.findAll().stream()
                .filter(exam -> isAdmin || (exam.getTest().getCreatedBy() != null && exam.getTest().getCreatedBy().equals(examiner)))
                .filter(exam -> filterByDateRange(exam.getEndTime(), dateRange))
                .collect(Collectors.toList());
    }

    private long getCompletedExamsCount(LocalDate[] dateRange, boolean isAdmin, Examiner examiner) {
        return getFilteredExams(dateRange, isAdmin, examiner).stream()
                .filter(exam -> Boolean.TRUE.equals(exam.getCompleted()))
                .count();
    }

    private double calculateAverageScore(LocalDate[] dateRange, boolean isAdmin, Examiner examiner) {
        List<StudentExam> filteredExams = getFilteredExams(dateRange, isAdmin, examiner);
        return filteredExams.stream()
                .filter(exam -> Boolean.TRUE.equals(exam.getCompleted()) && exam.getPercentage() != null)
                .mapToDouble(StudentExam::getPercentage)
                .average()
                .orElse(0.0);
    }

    private double calculatePassRate(LocalDate[] dateRange, boolean isAdmin, Examiner examiner) {
        List<StudentExam> completedExams = getFilteredExams(dateRange, isAdmin, examiner).stream()
                .filter(exam -> Boolean.TRUE.equals(exam.getCompleted()) && exam.getPercentage() != null)
                .collect(Collectors.toList());

        if (completedExams.isEmpty()) return 0.0;

        long passedExams = completedExams.stream()
                .filter(exam -> exam.getPercentage() >= exam.getTest().getPassingScore())
                .count();

        return (double) passedExams / completedExams.size() * 100;
    }

    private double calculateEnrollmentRate(boolean isAdmin, Examiner examiner) {
        long totalStudents = isAdmin ? studentRepository.count() :
                enrollmentRepository.countDistinctStudentsByExaminer(examiner);
        long enrolledStudents = isAdmin ? enrollmentRepository.count() :
                enrollmentRepository.countByTestCreatedBy(examiner);

        return totalStudents > 0 ? (double) enrolledStudents / totalStudents * 100 : 0.0;
    }

    private long getPendingGradingCount(LocalDate[] dateRange, boolean isAdmin, Examiner examiner) {
        return getFilteredExams(dateRange, isAdmin, examiner).stream()
                .filter(exam -> Boolean.TRUE.equals(exam.getCompleted()))
                .filter(exam -> !Boolean.TRUE.equals(exam.getGraded()))
                .count();
    }

    private long getExaminerQuestionsCount(Examiner examiner) {
        return testRepository.findByCreatedBy(examiner, org.springframework.data.domain.Pageable.unpaged())
                .getContent().stream()
                .mapToLong(test -> questionRepository.findByTestId(test.getId()).size())
                .sum();
    }

    private List<Question> getExaminerQuestions(Examiner examiner) {
        return testRepository.findByCreatedBy(examiner, org.springframework.data.domain.Pageable.unpaged())
                .getContent().stream()
                .flatMap(test -> questionRepository.findByTestId(test.getId()).stream())
                .collect(Collectors.toList());
    }

    private double calculateTimeSpent(StudentExam exam) {
        if (exam.getStartTime() != null && exam.getEndTime() != null) {
            return java.time.Duration.between(exam.getStartTime(), exam.getEndTime()).toMinutes();
        }
        return 0.0;
    }

    private boolean filterByDateRange(LocalDateTime dateTime, LocalDate[] dateRange) {
        if (dateTime == null || dateRange[0] == null) return true;
        LocalDate date = dateTime.toLocalDate();
        return !date.isBefore(dateRange[0]) && !date.isAfter(dateRange[1]);
    }

    private LocalDate[] parseDateRange(String dateRange) {
        LocalDate now = LocalDate.now();
        if (dateRange == null || "all".equals(dateRange)) {
            return new LocalDate[]{null, null};
        }

        switch (dateRange) {
            case "today": return new LocalDate[]{now, now};
            case "week": return new LocalDate[]{now.minusWeeks(1), now};
            case "month": return new LocalDate[]{now.minusMonths(1), now};
            case "year": return new LocalDate[]{now.minusYears(1), now};
            default: return new LocalDate[]{null, null};
        }
    }

    private String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "Unknown time";
        java.time.Duration duration = java.time.Duration.between(dateTime, LocalDateTime.now());

        if (duration.toMinutes() < 1) return "Just now";
        else if (duration.toHours() < 1) return duration.toMinutes() + " minutes ago";
        else if (duration.toDays() < 1) return duration.toHours() + " hours ago";
        else if (duration.toDays() < 7) return duration.toDays() + " days ago";
        else return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }

    private String getRandomColor() {
        String[] colors = {"#0d6efd", "#198754", "#ffc107", "#dc3545", "#6f42c1", "#fd7e14", "#20c997", "#6610f2"};
        return colors[new Random().nextInt(colors.length)];
    }

    private String getColorForScore(int score) {
        if (score >= 80) return "#198754";
        else if (score >= 60) return "#20c997";
        else if (score >= 40) return "#ffc107";
        else if (score >= 20) return "#fd7e14";
        else return "#dc3545";
    }

    // Existing methods from your original DashboardService
    private List<Map<String, Object>> getExaminerRecentActivity(Examiner examiner, int limit) {
        List<Map<String, Object>> activities = new ArrayList<>();

        List<Test> recentTests = testRepository.findByCreatedBy(examiner, PageRequest.of(0, limit)).getContent();
        for (Test test : recentTests) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", "test_created");
            activity.put("description", String.format("You created: %s", test.getTitle()));
            activity.put("time", formatTimeAgo(test.getCreatedAt()));
            activity.put("icon", "bi-card-checklist");
            activity.put("testId", test.getId());
            activities.add(activity);
        }

        List<Enrollment> recentEnrollments = enrollmentRepository.findByTestCreatedBy(examiner)
                .stream()
                .sorted((e1, e2) -> e2.getEnrolledAt().compareTo(e1.getEnrolledAt()))
                .limit(limit)
                .collect(Collectors.toList());

        for (Enrollment enrollment : recentEnrollments) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("type", "student_enrolled");
            activity.put("description", String.format("%s enrolled in %s",
                    enrollment.getStudent().getFullName(), enrollment.getTest().getTitle()));
            activity.put("time", formatTimeAgo(enrollment.getEnrolledAt()));
            activity.put("icon", "bi-person-plus");
            activity.put("studentId", enrollment.getStudent().getId());
            activity.put("testId", enrollment.getTest().getId());
            activities.add(activity);
        }

        return activities.stream()
                .sorted((a1, a2) -> ((String) a2.get("time")).compareTo((String) a1.get("time")))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private Map<String, Object> getExaminerPerformanceMetrics(Examiner examiner) {
        Map<String, Object> metrics = new HashMap<>();

        List<Test> examinerTests = testRepository.findByCreatedBy(examiner,
                org.springframework.data.domain.Pageable.unpaged()).getContent();

        double averageScore = examinerTests.stream()
                .flatMap(test -> studentExamRepository.findByTestId(test.getId()).stream())
                .filter(exam -> Boolean.TRUE.equals(exam.getCompleted()) && exam.getPercentage() != null)
                .mapToDouble(StudentExam::getPercentage)
                .average()
                .orElse(0.0);

        long totalEnrollments = enrollmentRepository.countByTestCreatedBy(examiner);
        long completedExams = examinerTests.stream()
                .mapToLong(test -> studentExamRepository.findByTestId(test.getId()).stream()
                        .filter(exam -> Boolean.TRUE.equals(exam.getCompleted()))
                        .count())
                .sum();

        double completionRate = totalEnrollments > 0 ? (double) completedExams / totalEnrollments * 100 : 0.0;

        long passedExams = examinerTests.stream()
                .flatMap(test -> studentExamRepository.findByTestId(test.getId()).stream())
                .filter(exam -> Boolean.TRUE.equals(exam.getCompleted()) && exam.getPercentage() != null)
                .filter(exam -> exam.getPercentage() >= exam.getTest().getPassingScore())
                .count();

        double passRate = completedExams > 0 ? (double) passedExams / completedExams * 100 : 0.0;

        metrics.put("averageScore", Math.round(averageScore * 100.0) / 100.0);
        metrics.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
        metrics.put("passRate", Math.round(passRate * 100.0) / 100.0);
        metrics.put("totalEnrollments", totalEnrollments);
        metrics.put("completedExams", completedExams);

        return metrics;
    }
}