//// Proctoring Service Interface
//package com.almubaraksuleiman.cbts.proctor.service.impl;
//
//
//import com.almubaraksuleiman.cbts.proctor.dto.ProctoringSessionDTO;
//import com.almubaraksuleiman.cbts.proctor.dto.ProctoringViolationDTO;
//import com.almubaraksuleiman.cbts.proctor.model.Severity;
//import com.almubaraksuleiman.cbts.proctor.model.ViolationType;
//
//import java.util.List;
//
//public interface ProctoringService {
//
//    // Session Management
//    ProctoringSessionDTO initializeSession(Long studentExamId);
//    ProctoringSessionDTO startMonitoring(Long sessionId);
//    ProctoringSessionDTO completeSession(Long sessionId);
//    ProctoringSessionDTO terminateSession(Long sessionId, String reason);
//
//    // Violation Management
//    ProctoringViolationDTO recordViolation(Long sessionId, ViolationType type,
//                                           Severity severity, String description,
//                                           Double confidence, String screenshotPath);
//    List<ProctoringViolationDTO> getSessionViolations(Long sessionId);
//    void markViolationReviewed(Long violationId, String comments);
//
//    // Dashboard Data
//    List<ProctoringSessionDTO> getActiveSessions();
//    List<ProctoringViolationDTO> getRecentViolations(int count);
//    ProctoringStatsDTO getProctoringStats();
//
//    // Real-time Monitoring
//    void updateConnectionQuality(Long sessionId, Integer quality);
//    void updateRecordingStatus(Long sessionId, Boolean screen, Boolean video, Boolean audio);
//}