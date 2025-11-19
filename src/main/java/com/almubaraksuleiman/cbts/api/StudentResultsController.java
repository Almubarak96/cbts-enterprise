package com.almubaraksuleiman.cbts.api;

import com.almubaraksuleiman.cbts.examiner.dto.StudentDetailedResult;
import com.almubaraksuleiman.cbts.examiner.service.impl.ResultsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/student/results")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin("http://localhost:4200")
public class StudentResultsController {

    private final ResultsService resultsService;

    @GetMapping("/immediate/{sessionId}")
    public ResponseEntity<?> getImmediateResults(@PathVariable Long sessionId) {
        try {
            Map<String, Object> resultsWithStatus = resultsService.getImmediateResultsWithStatus(sessionId);
            return ResponseEntity.ok(resultsWithStatus);
        } catch (Exception e) {
            log.error("Error fetching immediate results for session {}: {}", sessionId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to fetch results: " + e.getMessage()));
        }
    }

    @GetMapping("/{sessionId}/status")
    public ResponseEntity<?> getResultsStatus(@PathVariable Long sessionId) {
        try {
            Map<String, Object> gradingStatus = resultsService.getGradingStatus(sessionId);
            return ResponseEntity.ok(gradingStatus);
        } catch (Exception e) {
            log.error("Error fetching results status for session {}: {}", sessionId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to fetch status: " + e.getMessage()));
        }
    }

    @PostMapping("/{sessionId}/trigger-grading")
    public ResponseEntity<?> triggerGrading(@PathVariable Long sessionId) {
        try {
            resultsService.triggerImmediateGrading(sessionId);
            return ResponseEntity.ok(Map.of(
                    "message", "Grading triggered successfully",
                    "sessionId", sessionId
            ));
        } catch (Exception e) {
            log.error("Error triggering grading for session {}: {}", sessionId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Failed to trigger grading: " + e.getMessage()));
        }
    }
}