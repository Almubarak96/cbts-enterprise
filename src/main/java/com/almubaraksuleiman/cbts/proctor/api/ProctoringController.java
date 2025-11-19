//// Proctoring Controller
//package com.almubaraksuleiman.cbts.proctor.api;
//
//
//import com.almubaraksuleiman.cbts.proctor.dto.ProctoringSessionDTO;
//import com.almubaraksuleiman.cbts.proctor.dto.ProctoringViolationDTO;
//import com.almubaraksuleiman.cbts.proctor.service.impl.ProctoringService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/proctoring")
//@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
//public class ProctoringController {
//
//    private final ProctoringService proctoringService;
//
//    @PostMapping("/sessions/initialize")
//    public ResponseEntity<ProctoringSessionDTO> initializeSession(@RequestParam Long studentExamId) {
//        ProctoringSessionDTO session = proctoringService.initializeSession(studentExamId);
//        return ResponseEntity.ok(session);
//    }
//
//    @PostMapping("/sessions/{sessionId}/start-monitoring")
//    public ResponseEntity<ProctoringSessionDTO> startMonitoring(@PathVariable Long sessionId) {
//        ProctoringSessionDTO session = proctoringService.startMonitoring(sessionId);
//        return ResponseEntity.ok(session);
//    }
//
//    @GetMapping("/sessions/active")
//    public ResponseEntity<List<ProctoringSessionDTO>> getActiveSessions() {
//        List<ProctoringSessionDTO> sessions = proctoringService.getActiveSessions();
//        return ResponseEntity.ok(sessions);
//    }
//
//    @GetMapping("/violations/recent")
//    public ResponseEntity<List<ProctoringViolationDTO>> getRecentViolations(
//            @RequestParam(defaultValue = "10") int count) {
//        List<ProctoringViolationDTO> violations = proctoringService.getRecentViolations(count);
//        return ResponseEntity.ok(violations);
//    }
//
//    @GetMapping("/sessions/{sessionId}/violations")
//    public ResponseEntity<List<ProctoringViolationDTO>> getSessionViolations(@PathVariable Long sessionId) {
//        List<ProctoringViolationDTO> violations = proctoringService.getSessionViolations(sessionId);
//        return ResponseEntity.ok(violations);
//    }
//
//    @PostMapping("/violations/{violationId}/review")
//    public ResponseEntity<Void> markViolationReviewed(
//            @PathVariable Long violationId,
//            @RequestParam String comments) {
//        proctoringService.markViolationReviewed(violationId, comments);
//        return ResponseEntity.ok().build();
//    }
//}