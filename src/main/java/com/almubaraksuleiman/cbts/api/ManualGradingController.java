package com.almubaraksuleiman.cbts.api;


import com.almubaraksuleiman.cbts.examiner.dto.EssayGradingResponse;
import com.almubaraksuleiman.cbts.examiner.dto.ManualGradingRequest;
import com.almubaraksuleiman.cbts.examiner.dto.StudentEssayAnswerDto;
import com.almubaraksuleiman.cbts.examiner.dto.StudentEssayGroupDto;
import com.almubaraksuleiman.cbts.examiner.service.ManualGradingService;
import com.almubaraksuleiman.cbts.examiner.service.impl.ExamGradingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/grading")
@RequiredArgsConstructor
@Slf4j
public class ManualGradingController {

    private final ManualGradingService manualGradingService;
    private final ExamGradingService examGradingService;

    @PostMapping("/essay")
    public ResponseEntity<EssayGradingResponse> gradeEssay(@RequestBody ManualGradingRequest request) {
        log.info("Received manual grading request for session: {}, question: {}", 
                 request.getSessionId(), request.getQuestionId());
        
        EssayGradingResponse response = manualGradingService.gradeEssayAnswer(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/tests/{testId}/essays/ungraded")
    public ResponseEntity<Page<StudentEssayAnswerDto>> getUngradedEssays(
            @PathVariable Long testId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<StudentEssayAnswerDto> essays = manualGradingService.getUngradedEssays(testId, pageable);
        
        return ResponseEntity.ok(essays);
    }

    @GetMapping("/tests/{testId}/essays")
    public ResponseEntity<Page<StudentEssayAnswerDto>> getAllEssays(
            @PathVariable Long testId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<StudentEssayAnswerDto> essays = manualGradingService.getAllEssays(testId, pageable);
        
        return ResponseEntity.ok(essays);
    }

    @GetMapping("/sessions/{sessionId}/questions/{questionId}/essay")
    public ResponseEntity<StudentEssayAnswerDto> getEssayAnswer(
            @PathVariable Long sessionId,
            @PathVariable Long questionId) {
        
        StudentEssayAnswerDto essayAnswer = manualGradingService.getEssayAnswer(sessionId, questionId);
        return ResponseEntity.ok(essayAnswer);
    }

    @GetMapping("/tests/{testId}/essays/stats")
    public ResponseEntity<?> getEssayGradingStats(@PathVariable Long testId) {
        Long ungradedCount = manualGradingService.countUngradedEssays(testId);
        Long gradedCount = manualGradingService.countGradedEssays(testId);
        
        return ResponseEntity.ok(new EssayGradingStatsResponse(gradedCount, ungradedCount));
    }


    @GetMapping("/tests/{testId}/essays/grouped")
    public ResponseEntity<List<StudentEssayGroupDto>> getEssaysGroupedByStudent(@PathVariable Long testId) {
        log.info("Fetching essays grouped by student for test: {}", testId);

        try {
            List<StudentEssayGroupDto> groupedEssays = manualGradingService.getEssaysGroupedByStudent(testId);
            return ResponseEntity.ok(groupedEssays);
        } catch (Exception e) {
            log.error("Error fetching grouped essays for test {}: {}", testId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }


    // Add to ManualGradingController
    @GetMapping("/sessions/{sessionId}/grading-status")
    public ResponseEntity<?> getExamGradingStatus(@PathVariable Long sessionId) {
        try {
            Map<String, Object> status = examGradingService.getExamGradingStatus(sessionId);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error getting grading status for session {}: {}", sessionId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/sessions/{sessionId}/essays-graded")
    public ResponseEntity<Boolean> areAllEssaysGraded(@PathVariable Long sessionId) {
        try {
            boolean allGraded = examGradingService.areAllEssaysGraded(sessionId);
            return ResponseEntity.ok(allGraded);
        } catch (Exception e) {
            log.error("Error checking if all essays are graded for session {}: {}", sessionId, e.getMessage());
            return ResponseEntity.badRequest().body(false);
        }
    }

    // Helper class for stats response
    private static class EssayGradingStatsResponse {
        public Long gradedEssays;
        public Long ungradedEssays;
        public Long totalEssays;

        public EssayGradingStatsResponse(Long gradedEssays, Long ungradedEssays) {
            this.gradedEssays = gradedEssays;
            this.ungradedEssays = ungradedEssays;
            this.totalEssays = gradedEssays + ungradedEssays;
        }
    }
}