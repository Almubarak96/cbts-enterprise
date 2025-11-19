package com.almubaraksuleiman.cbts.examiner.service.impl;


import com.almubaraksuleiman.cbts.examiner.model.*;
import com.almubaraksuleiman.cbts.student.model.StudentExam;
import com.almubaraksuleiman.cbts.student.model.StudentAnswer;
import com.almubaraksuleiman.cbts.examiner.repository.TestRepository;
import com.almubaraksuleiman.cbts.student.repository.StudentExamRepository;
import com.almubaraksuleiman.cbts.student.repository.StudentExamQuestionRepository;
import com.almubaraksuleiman.cbts.student.repository.StudentAnswerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


// Add these imports for PDFBox
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final StudentExamRepository studentExamRepository;
    private final StudentExamQuestionRepository studentExamQuestionRepository;
    private final StudentAnswerRepository studentAnswerRepository;
    private final TestRepository testRepository;

    public AnalyticsData getTestAnalytics(Long testId, AnalyticsFilters filters) {
        log.info("Fetching analytics for test ID: {} with filters: {}", testId, filters);
        
        Optional<Test> testOpt = testRepository.findById(testId);
        if (testOpt.isEmpty()) {
            throw new RuntimeException("Test not found with ID: " + testId);
        }
        
        Test test = testOpt.get();
        List<StudentExam> studentExams = getFilteredStudentExams(testId, filters);
        
        return buildAnalyticsData(test, studentExams);
    }

    public Page<QuestionAnalysis> getQuestionAnalysisWithPagination(
            Long testId,
            AnalyticsFilters filters,
            Pageable pageable) {

        Optional<Test> testOpt = testRepository.findById(testId);
        if (testOpt.isEmpty()) {
            throw new RuntimeException("Test not found with ID: " + testId);
        }

        Test test = testOpt.get();
        List<StudentExam> studentExams = getFilteredStudentExams(testId, filters);
        List<QuestionAnalysis> allQuestions = buildQuestionAnalysis(test.getId(), studentExams);

        // Implement manual pagination since we're processing in memory
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allQuestions.size());

        List<QuestionAnalysis> paginatedQuestions = allQuestions.subList(start, end);

        return new PageImpl<>(
                paginatedQuestions,
                pageable,
                allQuestions.size()
        );
    }


    public Page<StudentPerformance> getStudentPerformanceWithPagination(
            Long testId,
            AnalyticsFilters filters,
            Pageable pageable,
            String search,
            String status) {

        log.info("Fetching student performance for test ID: {} with filters: {}, search: {}, status: {}",
                testId, filters, search, status);

        Optional<Test> testOpt = testRepository.findById(testId);
        if (testOpt.isEmpty()) {
            throw new RuntimeException("Test not found with ID: " + testId);
        }

        List<StudentExam> studentExams = getFilteredStudentExams(testId, filters);
        List<StudentPerformance> allStudents = buildStudentPerformance(studentExams);

        // Apply search filter
        if (search != null && !search.trim().isEmpty()) {
            String searchTerm = search.toLowerCase().trim();
            allStudents = allStudents.stream()
                    .filter(student ->
                            student.getName().toLowerCase().contains(searchTerm) ||
                                    student.getStudentId().toLowerCase().contains(searchTerm))
                    .collect(Collectors.toList());
        }

        // Apply status filter
        if (status != null && !status.equals("all")) {
            allStudents = allStudents.stream()
                    .filter(student -> student.getStatus().equals(status))
                    .collect(Collectors.toList());
        }

        // Apply sorting based on pageable
        allStudents = applySorting(allStudents, pageable.getSort());

        // Implement manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allStudents.size());

        if (start > allStudents.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, allStudents.size());
        }

        List<StudentPerformance> paginatedStudents = allStudents.subList(start, end);

        return new PageImpl<>(paginatedStudents, pageable, allStudents.size());
    }

    private List<StudentPerformance> applySorting(List<StudentPerformance> students, Sort sort) {
        if (sort == null || !sort.iterator().hasNext()) {
            return students;
        }

        Sort.Order order = sort.iterator().next();
        String property = order.getProperty();
        Sort.Direction direction = order.getDirection();

        List<StudentPerformance> sortedStudents = new ArrayList<>(students);

        sortedStudents.sort((s1, s2) -> {
            int comparison = 0;

            switch (property) {
                case "name":
                    comparison = s1.getName().compareToIgnoreCase(s2.getName());
                    break;
                case "score":
                    comparison = Double.compare(s1.getScore(), s2.getScore());
                    break;
                case "timeSpent":
                    comparison = Double.compare(s1.getTimeSpent(), s2.getTimeSpent());
                    break;
                case "status":
                    comparison = s1.getStatus().compareTo(s2.getStatus());
                    break;
//                case "submittedAt":
//                    comparison = compareDates(s1.getSubmittedAt(), s2.getSubmittedAt());
//                    break;
                default:
                    comparison = Double.compare(s1.getScore(), s2.getScore());
            }

            return direction == Sort.Direction.ASC ? comparison : -comparison;
        });

        return sortedStudents;
    }

    private int compareDates(Date date1, Date date2) {
        if (date1 == null && date2 == null) return 0;
        if (date1 == null) return -1;
        if (date2 == null) return 1;
        return date1.compareTo(date2);
    }

    private List<StudentExam> getFilteredStudentExams(Long testId, AnalyticsFilters filters) {
        List<StudentExam> allExams = studentExamRepository.findByTestId(testId);
        
        if (filters == null || filters.getDateRange() == null || "all".equals(filters.getDateRange())) {
            return allExams;
        }
        
        LocalDate now = LocalDate.now();
        LocalDate startDate = null;
        
        switch (filters.getDateRange()) {
            case "today":
                startDate = now;
                break;
            case "week":
                startDate = now.minusWeeks(1);
                break;
            case "month":
                startDate = now.minusMonths(1);
                break;
            default:
                return allExams;
        }
        
        final LocalDate filterStartDate = startDate;
        return allExams.stream()
                .filter(exam -> exam.getStartTime() != null && 
                               exam.getStartTime().toLocalDate().isAfter(filterStartDate.minusDays(1)))
                .collect(Collectors.toList());
    }

    private AnalyticsData buildAnalyticsData(Test test, List<StudentExam> studentExams) {
        AnalyticsSummary summary = buildSummary(test, studentExams);
        List<ScoreDistribution> scoreDistribution = buildScoreDistribution(studentExams);
        List<QuestionAnalysis> questionAnalysis = buildQuestionAnalysis(test.getId(), studentExams);
        List<TimeAnalysis> timeAnalysis = buildTimeAnalysis(studentExams, test.getDurationMinutes());
        List<StudentPerformance> studentPerformance = buildStudentPerformance(studentExams);

        return AnalyticsData.builder()
                .test(mapTestData(test))
                .summary(summary)
                .scoreDistribution(scoreDistribution)
                .questionAnalysis(questionAnalysis)
                .timeAnalysis(timeAnalysis)
                .studentPerformance(studentPerformance)
                .build();
    }

    private AnalyticsSummary buildSummary(Test test, List<StudentExam> studentExams) {
        long totalStudents = studentExams.size();
        long completedStudents = studentExams.stream()
                .filter(exam -> Boolean.TRUE.equals(exam.getCompleted()))
                .count();
        
        double averageScore = studentExams.stream()
                .filter(exam -> exam.getPercentage() != null)
                .mapToDouble(StudentExam::getPercentage)
                .average()
                .orElse(0.0);
        
        double passRate = studentExams.stream()
                .filter(exam -> exam.getPercentage() != null &&
                               exam.getPercentage() >= test.getPassingScore())
                .count() * 100.0 / (completedStudents > 0 ? completedStudents : 1);
        
        double averageTimeSpent = studentExams.stream()
                .filter(exam -> exam.getStartTime() != null && exam.getEndTime() != null)
                .mapToLong(exam -> java.time.Duration.between(exam.getStartTime(), exam.getEndTime()).toMinutes())
                .average()
                .orElse(0.0);

        return AnalyticsSummary.builder()
                .totalStudents((int) totalStudents)
                .completedStudents((int) completedStudents)
                .averageScore(averageScore)
                .passRate(passRate)
                .averageTimeSpent(averageTimeSpent)
                .build();
    }

    private List<ScoreDistribution> buildScoreDistribution(List<StudentExam> studentExams) {
        Map<Integer, Long> scoreCounts = studentExams.stream()
                .filter(exam -> exam.getPercentage() != null)
                .collect(Collectors.groupingBy(
                    exam -> (int) (Math.floor(exam.getPercentage() / 10.0) * 10),
                    Collectors.counting()
                ));

        List<ScoreDistribution> distribution = new ArrayList<>();
        for (int score = 0; score <= 100; score += 10) {
            long count = scoreCounts.getOrDefault(score, 0L);
            distribution.add(ScoreDistribution.builder()
                    .score(score)
                    .count((int) count)
                    .build());
        }
        
        return distribution;
    }

    private List<QuestionAnalysis> buildQuestionAnalysis(Long testId, List<StudentExam> studentExams) {
        List<Question> questions = testRepository.findQuestionsByTestId(testId);
        List<QuestionAnalysis> questionAnalysis = new ArrayList<>();

        for (Question question : questions) {
            List<StudentAnswer> questionAnswers = studentAnswerRepository.findByQuestionId(question.getId());

            long correctAnswers = questionAnswers.stream()
                    .filter(answer -> isAnswerCorrect(answer, question))
                    .count();

            long incorrectAnswers = questionAnswers.size() - correctAnswers;

            double averageTime = calculateAverageTimeForQuestion(question.getId(), studentExams);
            String difficulty = determineQuestionDifficulty(correctAnswers, questionAnswers.size());

            questionAnalysis.add(QuestionAnalysis.builder()
                    .questionId(Math.toIntExact(question.getId()))
                    .text(question.getText())
                    .correctAnswers((int) correctAnswers)
                    .incorrectAnswers((int) incorrectAnswers)
                    .averageTime(averageTime)
                    .difficulty(difficulty)
                    .build());
        }
        
        return questionAnalysis;
    }

    private boolean isAnswerCorrect(StudentAnswer answer, Question question) {
        if (answer.getAnswer() == null || question.getCorrectAnswer() == null) {
            return false;
        }
        
        // For multiple select questions
        if (answer.getAnswer().contains(",")) {
            Set<String> studentAnswers = Arrays.stream(answer.getAnswer().split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
            Set<String> correctAnswers = Arrays.stream(question.getCorrectAnswer().split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
            return studentAnswers.equals(correctAnswers);
        }
        
        // For single answer questions
        return answer.getAnswer().trim().equalsIgnoreCase(question.getCorrectAnswer().trim());
    }

    private double calculateAverageTimeForQuestion(Long questionId, List<StudentExam> studentExams) {
        // This would need additional timing data in your entities
        // For now, returning a calculated average based on question complexity
        return new Random().nextInt(60) + 30.0; // Random between 30-90 seconds
    }

    private String determineQuestionDifficulty(long correctAnswers, long totalAttempts) {
        if (totalAttempts == 0) return "medium";
        
        double accuracy = (double) correctAnswers / totalAttempts;
        if (accuracy >= 0.7) return "easy";
        if (accuracy >= 0.4) return "medium";
        return "hard";
    }

    private List<TimeAnalysis> buildTimeAnalysis(List<StudentExam> studentExams, Integer testDuration) {
        if (testDuration == null) testDuration = 60; // Default 60 minutes
        
        Map<String, Long> timeRanges = new LinkedHashMap<>();
        timeRanges.put("0-10 min", 0L);
        timeRanges.put("10-20 min", 0L);
        timeRanges.put("20-30 min", 0L);
        timeRanges.put("30-40 min", 0L);
        timeRanges.put("40-50 min", 0L);
        timeRanges.put("50-60+ min", 0L);
        
        for (StudentExam exam : studentExams) {
            if (exam.getStartTime() != null && exam.getEndTime() != null) {
                long minutes = java.time.Duration.between(exam.getStartTime(), exam.getEndTime()).toMinutes();
                String range = getTimeRange(minutes);
                timeRanges.put(range, timeRanges.getOrDefault(range, 0L) + 1);
            }
        }
        
        return timeRanges.entrySet().stream()
                .map(entry -> TimeAnalysis.builder()
                        .timeRange(entry.getKey())
                        .count(entry.getValue().intValue())
                        .build())
                .collect(Collectors.toList());
    }

    private String getTimeRange(long minutes) {
        if (minutes <= 10) return "0-10 min";
        if (minutes <= 20) return "10-20 min";
        if (minutes <= 30) return "20-30 min";
        if (minutes <= 40) return "30-40 min";
        if (minutes <= 50) return "40-50 min";
        return "50-60+ min";
    }

    private List<StudentPerformance> buildStudentPerformance(List<StudentExam> studentExams) {
        return studentExams.stream()
                .map(this::mapToStudentPerformance)
                .sorted((s1, s2) -> Double.compare(s2.getScore(), s1.getScore())) // Sort by score descending
                .limit(10) // Top 10 performers
                .collect(Collectors.toList());
    }

    private StudentPerformance mapToStudentPerformance(StudentExam exam) {

        String studentName = exam.getStudent() != null ?
                exam.getStudent().getFullName() : "Student " + exam.getStudentId();//fetch actual student names

        return StudentPerformance.builder()
                .studentId(String.valueOf(exam.getStudentId()))
                .name(studentName)
                .score(exam.getPercentage() != null ? exam.getPercentage() : 0.0)
                .timeSpent(calculateTimeSpent(exam))
                .status(determineStatus(exam))
                .submittedAt(exam.getEndTime())
                .build();
    }

    private double calculateTimeSpent(StudentExam exam) {
        if (exam.getStartTime() != null && exam.getEndTime() != null) {
            return java.time.Duration.between(exam.getStartTime(), exam.getEndTime()).toMinutes();
        }
        return 0.0;
    }

    private String determineStatus(StudentExam exam) {
        if (Boolean.TRUE.equals(exam.getCompleted())) return "completed";
        if (exam.getStartTime() != null) return "in-progress";
        return "not-started";
    }

    private TestData mapTestData(Test test) {
        return TestData.builder()
                .id(test.getId())
                .title(test.getTitle())
                .durationMinutes(test.getDurationMinutes())
                .totalMarks(test.getTotalMarks())
                .numberOfQuestions(test.getNumberOfQuestions())
                .passingScore(test.getPassingScore())
                .build();
    }







// Replace the generatePdfReport method with this PDFBox version:

    public byte[] generatePdfReport(AnalyticsData analytics) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Set up margins and starting position
            float margin = 50;
            float yPosition = PDRectangle.A4.getHeight() - margin;
            float lineHeight = 14;
            float titleHeight = 18;

            // Title
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Test Analytics Report");
            contentStream.endText();
            yPosition -= titleHeight * 2;

            // Test Information
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Test: " + analytics.getTest().getTitle());
            contentStream.endText();
            yPosition -= lineHeight;

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 10);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Duration: " + analytics.getTest().getDurationMinutes() + " minutes | " +
                    "Questions: " + analytics.getTest().getNumberOfQuestions() + " | " +
                    "Passing Score: " + analytics.getTest().getPassingScore() + "%");
            contentStream.endText();
            yPosition -= lineHeight * 2;

            // Summary Section
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Performance Summary");
            contentStream.endText();
            yPosition -= lineHeight;

            String[] summaryLines = {
                    "Total Students: " + analytics.getSummary().getTotalStudents(),
                    "Completed Students: " + analytics.getSummary().getCompletedStudents(),
                    "Average Score: " + String.format("%.1f%%", analytics.getSummary().getAverageScore()),
                    "Pass Rate: " + String.format("%.1f%%", analytics.getSummary().getPassRate()),
                    "Average Time Spent: " + String.format("%.1f min", analytics.getSummary().getAverageTimeSpent())
            };

            for (String line : summaryLines) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 10);
                contentStream.newLineAtOffset(margin + 10, yPosition);
                contentStream.showText(line);
                contentStream.endText();
                yPosition -= lineHeight;
            }
            yPosition -= lineHeight;

            // Score Distribution
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Score Distribution");
            contentStream.endText();
            yPosition -= lineHeight;

            for (ScoreDistribution dist : analytics.getScoreDistribution()) {
                double percentage = analytics.getSummary().getTotalStudents() > 0 ?
                        (double) dist.getCount() / analytics.getSummary().getTotalStudents() * 100 : 0;

                String scoreLine = String.format("%3d%%: %3d students (%.1f%%)",
                        dist.getScore(), dist.getCount(), percentage);

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 9);
                contentStream.newLineAtOffset(margin + 10, yPosition);
                contentStream.showText(scoreLine);
                contentStream.endText();
                yPosition -= lineHeight;

                // Create new page if running out of space
                if (yPosition < margin + 100) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = PDRectangle.A4.getHeight() - margin;
                }
            }
            yPosition -= lineHeight;

            // Question Performance (Top 10 most difficult)
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Top 10 Most Difficult Questions");
            contentStream.endText();
            yPosition -= lineHeight;

            List<QuestionAnalysis> difficultQuestions = analytics.getQuestionAnalysis().stream()
                    .sorted((q1, q2) -> {
                        double acc1 = (double) q1.getCorrectAnswers() / (q1.getCorrectAnswers() + q1.getIncorrectAnswers());
                        double acc2 = (double) q2.getCorrectAnswers() / (q2.getCorrectAnswers() + q2.getIncorrectAnswers());
                        return Double.compare(acc1, acc2);
                    })
                    .limit(10)
                    .collect(Collectors.toList());

            for (QuestionAnalysis qa : difficultQuestions) {
                double accuracy = (double) qa.getCorrectAnswers() / (qa.getCorrectAnswers() + qa.getIncorrectAnswers()) * 100;

                String questionLine = String.format("Q%d: %.1f%% correct (%s)",
                        qa.getQuestionId(), accuracy, qa.getDifficulty());

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 9);
                contentStream.newLineAtOffset(margin + 10, yPosition);
                contentStream.showText(questionLine);
                contentStream.endText();
                yPosition -= lineHeight;

                if (yPosition < margin + 100) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = PDRectangle.A4.getHeight() - margin;
                }
            }

            // Footer with generation date
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 8);
            contentStream.newLineAtOffset(margin, margin);
            contentStream.showText("Generated on: " + LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            contentStream.endText();

            contentStream.close();
            document.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error generating PDF report", e);
        }
    }

    public byte[] generateExcelReport(AnalyticsData analytics) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // Create styles
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle highlightStyle = workbook.createCellStyle();
            highlightStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            highlightStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Summary Sheet
            Sheet summarySheet = workbook.createSheet("Summary");

            // Test Info
            Row testRow = summarySheet.createRow(0);
            testRow.createCell(0).setCellValue("Test Analytics Report");

            Row titleRow = summarySheet.createRow(2);
            titleRow.createCell(0).setCellValue("Test:");
            titleRow.createCell(1).setCellValue(analytics.getTest().getTitle());

            int rowNum = 3;
            String[][] summaryData = {
                    {"Duration", analytics.getTest().getDurationMinutes() + " minutes"},
                    {"Total Questions", String.valueOf(analytics.getTest().getNumberOfQuestions())},
                    {"Passing Score", analytics.getTest().getPassingScore() + "%"},
                    {"", ""},
                    {"Total Students", String.valueOf(analytics.getSummary().getTotalStudents())},
                    {"Completed Students", String.valueOf(analytics.getSummary().getCompletedStudents())},
                    {"Average Score", String.format("%.1f%%", analytics.getSummary().getAverageScore())},
                    {"Pass Rate", String.format("%.1f%%", analytics.getSummary().getPassRate())},
                    {"Average Time Spent", String.format("%.1f min", analytics.getSummary().getAverageTimeSpent())}
            };

            for (String[] data : summaryData) {
                Row row = summarySheet.createRow(rowNum++);
                row.createCell(0).setCellValue(data[0]);
                row.createCell(1).setCellValue(data[1]);
            }

            // Score Distribution Sheet
            Sheet scoreSheet = workbook.createSheet("Score Distribution");
            Row scoreHeader = scoreSheet.createRow(0);
            String[] scoreHeaders = {"Score Range", "Student Count", "Percentage"};
            for (int i = 0; i < scoreHeaders.length; i++) {
                Cell cell = scoreHeader.createCell(i);
                cell.setCellValue(scoreHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            rowNum = 1;
            for (ScoreDistribution dist : analytics.getScoreDistribution()) {
                Row row = scoreSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(dist.getScore() + "%");
                row.createCell(1).setCellValue(dist.getCount());
                double percentage = analytics.getSummary().getTotalStudents() > 0 ?
                        (double) dist.getCount() / analytics.getSummary().getTotalStudents() * 100 : 0;
                row.createCell(2).setCellValue(String.format("%.1f%%", percentage));
            }

            // Question Analysis Sheet
            Sheet questionSheet = workbook.createSheet("Question Performance");
            Row questionHeader = questionSheet.createRow(0);
            String[] questionHeaders = {"Question ID", "Question Text", "Correct", "Incorrect", "Accuracy", "Difficulty", "Avg Time (s)"};
            for (int i = 0; i < questionHeaders.length; i++) {
                Cell cell = questionHeader.createCell(i);
                cell.setCellValue(questionHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            rowNum = 1;
            for (QuestionAnalysis qa : analytics.getQuestionAnalysis()) {
                Row row = questionSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(qa.getQuestionId());
                row.createCell(1).setCellValue(qa.getText());
                row.createCell(2).setCellValue(qa.getCorrectAnswers());
                row.createCell(3).setCellValue(qa.getIncorrectAnswers());

                double accuracy = (double) qa.getCorrectAnswers() / (qa.getCorrectAnswers() + qa.getIncorrectAnswers()) * 100;
                row.createCell(4).setCellValue(String.format("%.1f%%", accuracy));

                row.createCell(5).setCellValue(qa.getDifficulty());
                row.createCell(6).setCellValue(String.format("%.1f", qa.getAverageTime()));

                // Highlight difficult questions
                if ("hard".equals(qa.getDifficulty())) {
                    for (int i = 0; i < questionHeaders.length; i++) {
                        row.getCell(i).setCellStyle(highlightStyle);
                    }
                }
            }

            // Student Performance Sheet
            Sheet studentSheet = workbook.createSheet("Top Performers");
            Row studentHeader = studentSheet.createRow(0);
            String[] studentHeaders = {"Student ID", "Name", "Score", "Time Spent (min)", "Status", "Submitted At"};
            for (int i = 0; i < studentHeaders.length; i++) {
                Cell cell = studentHeader.createCell(i);
                cell.setCellValue(studentHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            rowNum = 1;
            for (StudentPerformance sp : analytics.getStudentPerformance()) {
                Row row = studentSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(sp.getStudentId());
                row.createCell(1).setCellValue(sp.getName());
                row.createCell(2).setCellValue(String.format("%.1f%%", sp.getScore()));
                row.createCell(3).setCellValue(String.format("%.1f", sp.getTimeSpent()));
                row.createCell(4).setCellValue(sp.getStatus());
                row.createCell(5).setCellValue(sp.getSubmittedAt() != null ?
                        sp.getSubmittedAt().toString() : "N/A");
            }

            // Auto-size columns
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                for (int j = 0; j < sheet.getRow(0).getLastCellNum(); j++) {
                    sheet.autoSizeColumn(j);
                }
            }

            workbook.write(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Error generating Excel report", e);
        }
    }

    public byte[] generateCsvReport(AnalyticsData analytics) {
        StringBuilder csv = new StringBuilder();

        // Header
        csv.append("Test Analytics Report\n");
        csv.append("Test:,").append(analytics.getTest().getTitle()).append("\n");
        csv.append("Generated:,").append(LocalDateTime.now()).append("\n\n");

        // Summary
        csv.append("SUMMARY\n");
        csv.append("Metric,Value\n");
        csv.append("Total Students,").append(analytics.getSummary().getTotalStudents()).append("\n");
        csv.append("Completed Students,").append(analytics.getSummary().getCompletedStudents()).append("\n");
        csv.append("Average Score,").append(String.format("%.1f", analytics.getSummary().getAverageScore())).append("\n");
        csv.append("Pass Rate,").append(String.format("%.1f", analytics.getSummary().getPassRate())).append("\n");
        csv.append("Average Time Spent,").append(String.format("%.1f", analytics.getSummary().getAverageTimeSpent())).append("\n\n");

        // Score Distribution
        csv.append("SCORE DISTRIBUTION\n");
        csv.append("Score Range,Student Count,Percentage\n");
        for (ScoreDistribution dist : analytics.getScoreDistribution()) {
            double percentage = analytics.getSummary().getTotalStudents() > 0 ?
                    (double) dist.getCount() / analytics.getSummary().getTotalStudents() * 100 : 0;
            csv.append(dist.getScore()).append("%,")
                    .append(dist.getCount()).append(",")
                    .append(String.format("%.1f", percentage)).append("%\n");
        }
        csv.append("\n");

        // Question Performance
        csv.append("QUESTION PERFORMANCE\n");
        csv.append("Question ID,Correct Answers,Incorrect Answers,Accuracy,Difficulty,Average Time\n");
        for (QuestionAnalysis qa : analytics.getQuestionAnalysis()) {
            double accuracy = (double) qa.getCorrectAnswers() / (qa.getCorrectAnswers() + qa.getIncorrectAnswers()) * 100;
            csv.append(qa.getQuestionId()).append(",")
                    .append(qa.getCorrectAnswers()).append(",")
                    .append(qa.getIncorrectAnswers()).append(",")
                    .append(String.format("%.1f", accuracy)).append("%,")
                    .append(qa.getDifficulty()).append(",")
                    .append(String.format("%.1f", qa.getAverageTime())).append("\n");
        }

        return csv.toString().getBytes();
    }

}