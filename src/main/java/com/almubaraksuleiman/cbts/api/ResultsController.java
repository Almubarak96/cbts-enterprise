package com.almubaraksuleiman.cbts.api;

import com.almubaraksuleiman.cbts.examiner.dto.*;

import com.almubaraksuleiman.cbts.examiner.service.impl.ExamGradingService;
import com.almubaraksuleiman.cbts.examiner.service.impl.ResultsService;
import com.almubaraksuleiman.cbts.student.model.StudentExam;
import com.almubaraksuleiman.cbts.student.repository.StudentExamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:4200")
public class ResultsController {

    private final ResultsService resultsService;
    private final ExamGradingService examGradingService;
    private final StudentExamRepository studentExamRepository;

    @GetMapping("/tests")
    public ResponseEntity<Page<TestResultSummary>> getTestResults(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Pageable pageable = PageRequest.of(page, size, 
            Sort.by(Sort.Direction.fromString(sortDirection), sortBy));

        TestResultsFilter filter = new TestResultsFilter();
        filter.setSearchTerm(search);
        filter.setStatus(status);
        // Parse dates if provided

        Page<TestResultSummary> results = resultsService.getTestResultsSummary(filter, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/tests/{testId}/students")
    public ResponseEntity<Page<StudentExamResult>> getStudentResults(
            @PathVariable Long testId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "percentage") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Double minScore,
            @RequestParam(required = false) Double maxScore,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean graded) {

        Pageable pageable = PageRequest.of(page, size, 
            Sort.by(Sort.Direction.fromString(sortDirection), sortBy));

        StudentResultsFilter filter = new StudentResultsFilter();
        filter.setSearchTerm(search);
        filter.setMinScore(minScore);
        filter.setMaxScore(maxScore);
        filter.setStatus(status);
        filter.setGraded(graded);

        Page<StudentExamResult> results = resultsService.getStudentExamResults(testId, filter, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/students/{sessionId}")
    public ResponseEntity<StudentDetailedResult> getStudentDetailedResult(@PathVariable Long sessionId) {
        StudentDetailedResult result = resultsService.getStudentDetailedResult(sessionId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/tests/{testId}/stats")
    public ResponseEntity<Map<String, Object>> getTestStatistics(@PathVariable Long testId) {
        // Return test statistics for dashboard
        Map<String, Object> stats = new HashMap<>();
        // Add statistics data
        return ResponseEntity.ok(stats);
    }



    // Add these methods to your existing ResultsController
    @GetMapping("/students/immediate/{sessionId}")
    public ResponseEntity<?> getImmediateResult(@PathVariable Long sessionId) {
        try {
            StudentDetailedResult result = resultsService.getStudentDetailedResult(sessionId);

            // Check if exam is fully graded
            Map<String, Object> gradingStatus = examGradingService.getExamGradingStatus(sessionId);
            boolean fullyGraded = "FULLY_GRADED".equals(gradingStatus.get("status"));

            if (!fullyGraded) {
                // Return status information if not fully graded
                Map<String, Object> response = new HashMap<>();
                response.put("status", "PENDING");
                response.put("message", "Results are being processed");
                response.put("gradingStatus", gradingStatus);
                response.put("sessionId", sessionId);
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error fetching immediate results for session {}: {}", sessionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch results: " + e.getMessage()));
        }
    }

    @GetMapping("/students/{sessionId}/grading-status")
    public ResponseEntity<Map<String, Object>> getGradingStatus(@PathVariable Long sessionId) {
        try {
            Map<String, Object> gradingStatus = examGradingService.getExamGradingStatus(sessionId);
            return ResponseEntity.ok(gradingStatus);
        } catch (Exception e) {
            log.error("Error fetching grading status for session {}: {}", sessionId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch grading status"));
        }
    }

    @GetMapping("/students/{studentId}/test/{testId}/latest-result")
    public ResponseEntity<?> getLatestStudentResult(
            @PathVariable Long studentId,
            @PathVariable Long testId) {

        try {
            // Find the latest exam session for this student and test
            Optional<StudentExam> latestExam = studentExamRepository
                    .findByStudentIdAndTestId(studentId, testId);

            if (latestExam.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No exam session found"));
            }

            StudentExam exam = latestExam.get();
            Map<String, Object> gradingStatus = examGradingService.getExamGradingStatus(exam.getSessionId());

            if (!"FULLY_GRADED".equals(gradingStatus.get("status"))) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "PENDING");
                response.put("gradingStatus", gradingStatus);
                response.put("sessionId", exam.getSessionId());
                return ResponseEntity.ok(response);
            }

            StudentDetailedResult result = resultsService.getStudentDetailedResult(exam.getSessionId());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error fetching latest result for student {} test {}: {}",
                    studentId, testId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch results"));
        }
    }
}