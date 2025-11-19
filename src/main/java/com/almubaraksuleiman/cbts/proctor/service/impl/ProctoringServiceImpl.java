//// Proctoring Service Implementation
//package com.almubaraksuleiman.cbts.proctor.service.impl;
//
//
//import com.almubaraksuleiman.cbts.proctor.dto.ProctoringSessionDTO;
//import com.almubaraksuleiman.cbts.proctor.dto.ProctoringViolationDTO;
//import com.almubaraksuleiman.cbts.proctor.model.*;
//import com.almubaraksuleiman.cbts.student.model.StudentExam;
//import com.almubaraksuleiman.cbts.student.repository.StudentExamRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class ProctoringServiceImpl implements ProctoringService {
//
//    private final ProctoringSessionRepository proctoringSessionRepository;
//    private final ProctoringViolationRepository proctoringViolationRepository;
//    private final StudentExamRepository studentExamRepository;
//    private final AIViolationDetectionService aiViolationDetectionService;
//
//    @Override
//    @Transactional
//    public ProctoringSessionDTO initializeSession(Long studentExamId) {
//        StudentExam studentExam = studentExamRepository.findById(studentExamId)
//                .orElseThrow(() -> new IllegalArgumentException("Student exam not found: " + studentExamId));
//
//        // Check if session already exists
//        ProctoringSession existingSession = proctoringSessionRepository.findByStudentExam(studentExam);
//        if (existingSession != null) {
//            return convertToDTO(existingSession);
//        }
//
//        // Create new proctoring session
//        ProctoringSession session = ProctoringSession.builder()
//                .studentExam(studentExam)
//                .startTime(LocalDateTime.now())
//                .status(ProctoringStatus.INITIALIZING)
//                .screenRecordingActive(false)
//                .videoRecordingActive(false)
//                .audioRecordingActive(false)
//                .suspiciousActivities(0)
//                .connectionQuality(100)
//                .manuallyReviewed(false)
//                .build();
//
//        ProctoringSession savedSession = proctoringSessionRepository.save(session);
//        log.info("Initialized proctoring session for student exam: {}", studentExamId);
//
//        return convertToDTO(savedSession);
//    }
//
//    @Override
//    @Transactional
//    public ProctoringSessionDTO startMonitoring(Long sessionId) {
//        ProctoringSession session = proctoringSessionRepository.findById(sessionId)
//                .orElseThrow(() -> new IllegalArgumentException("Proctoring session not found: " + sessionId));
//
//        session.setStatus(ProctoringStatus.ACTIVE);
//        session.setScreenRecordingActive(true);
//        session.setVideoRecordingActive(true);
//        session.setAudioRecordingActive(true);
//
//        ProctoringSession updatedSession = proctoringSessionRepository.save(session);
//        log.info("Started monitoring for session: {}", sessionId);
//
//        return convertToDTO(updatedSession);
//    }
//
//    @Override
//    @Transactional
//    public ProctoringViolationDTO recordViolation(Long sessionId, ViolationType type,
//                                                  Severity severity, String description,
//                                                  Double confidence, String screenshotPath) {
//        ProctoringSession session = proctoringSessionRepository.findById(sessionId)
//                .orElseThrow(() -> new IllegalArgumentException("Proctoring session not found: " + sessionId));
//
//        // Create violation record
//        ProctoringViolation violation = ProctoringViolation.builder()
//                .proctoringSession(session)
//                .timestamp(LocalDateTime.now())
//                .violationType(type)
//                .severity(severity)
//                .confidence(confidence != null ? confidence : 0.8)
//                .description(description)
//                .screenshotPath(screenshotPath)
//                .reviewed(false)
//                .build();
//
//        ProctoringViolation savedViolation = proctoringViolationRepository.save(violation);
//
//        // Update session violation count
//        session.setSuspiciousActivities(session.getSuspiciousActivities() + 1);
//        proctoringSessionRepository.save(session);
//
//        log.info("Recorded violation for session {}: {} - {}", sessionId, type, severity);
//
//        return convertToViolationDTO(savedViolation);
//    }
//
//    @Override
//    public List<ProctoringSessionDTO> getActiveSessions() {
//        List<ProctoringSession> activeSessions = proctoringSessionRepository
//                .findByStatusIn(List.of(ProctoringStatus.ACTIVE, ProctoringStatus.INITIALIZING));
//
//        return activeSessions.stream()
//                .map(this::convertToDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<ProctoringViolationDTO> getRecentViolations(int count) {
//        List<ProctoringViolation> violations = proctoringViolationRepository
//                .findTopByOrderByTimestampDesc(count);
//
//        return violations.stream()
//                .map(this::convertToViolationDTO)
//                .collect(Collectors.toList());
//    }
//
//    // Helper methods for conversion
//    private ProctoringSessionDTO convertToDTO(ProctoringSession session) {
//        StudentExam studentExam = session.getStudentExam();
//
//        return ProctoringSessionDTO.builder()
//                .id(session.getId())
//                .studentExamId(studentExam.getSessionId())
//                .studentId(studentExam.getStudentId())
//                .studentName(studentExam.getStudent().getFullName())
//                .examName(studentExam.getTest().getTitle())
//                .startTime(session.getStartTime())
//                .endTime(session.getEndTime())
//                .status(session.getStatus().name())
//                .screenRecordingActive(session.getScreenRecordingActive())
//                .videoRecordingActive(session.getVideoRecordingActive())
//                .audioRecordingActive(session.getAudioRecordingActive())
//                .suspiciousActivities(session.getSuspiciousActivities())
//                .connectionQuality(session.getConnectionQuality())
//                .build();
//    }
//
//    private ProctoringViolationDTO convertToViolationDTO(ProctoringViolation violation) {
//        ProctoringSession session = violation.getProctoringSession();
//        StudentExam studentExam = session.getStudentExam();
//
//        return ProctoringViolationDTO.builder()
//                .id(violation.getId())
//                .proctoringSessionId(session.getId())
//                .studentName(studentExam.getStudent().getFullName())
//                .examName(studentExam.getTest().getTitle())
//                .timestamp(violation.getTimestamp())
//                .violationType(violation.getViolationType().name())
//                .severity(violation.getSeverity().name())
//                .confidence(violation.getConfidence())
//                .description(violation.getDescription())
//                .reviewed(violation.getReviewed())
//                .screenshotUrl(violation.getScreenshotPath())
//                .build();
//    }
//
//    // Other methods implementation...
//    @Override
//    public ProctoringSessionDTO completeSession(Long sessionId) {
//        // Implementation
//        return null;
//    }
//
//    @Override
//    public ProctoringSessionDTO terminateSession(Long sessionId, String reason) {
//        // Implementation
//        return null;
//    }
//
//    @Override
//    public List<ProctoringViolationDTO> getSessionViolations(Long sessionId) {
//        // Implementation
//        return null;
//    }
//
//    @Override
//    public void markViolationReviewed(Long violationId, String comments) {
//        // Implementation
//    }
//
//    @Override
//    public ProctoringStatsDTO getProctoringStats() {
//        // Implementation
//        return null;
//    }
//
//    @Override
//    public void updateConnectionQuality(Long sessionId, Integer quality) {
//        // Implementation
//    }
//
//    @Override
//    public void updateRecordingStatus(Long sessionId, Boolean screen, Boolean video, Boolean audio) {
//        // Implementation
//    }
//}