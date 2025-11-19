//package com.almubaraksuleiman.cbts.examiner.service;
//
//
//import com.almubaraksuleiman.cbts.student.model.StudentExam;
//import com.almubaraksuleiman.cbts.student.repository.StudentExamRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
///**
// * @author Almubarak Suleiman
// * @version 1.0
// * @since 2025
// **/
//
//@Service
//@RequiredArgsConstructor
//public class TestAutoSubmitService {
//
//    private final StudentExamRepository studentExamRepository;
//
//    public void completeExam(Long sessionId) {
//        Optional<StudentExam> exam = studentExamRepository.findById(sessionId);
//        if (exam.isPresent()){
//            exam.get().setCompleted(true);
//            exam.get().setEndTime(LocalDateTime.now());
//            studentExamRepository.save(exam.get());
//        }
//
//    }
//
//
//
//
//
//
//}











package com.almubaraksuleiman.cbts.examiner.service;

import com.almubaraksuleiman.cbts.student.model.StudentExam;
import com.almubaraksuleiman.cbts.student.repository.StudentExamRepository;
import com.almubaraksuleiman.cbts.examiner.service.impl.ExamGradingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

@Service
@RequiredArgsConstructor
@Slf4j
public class TestAutoSubmitService {

    private final StudentExamRepository studentExamRepository;
    private final ExamGradingService examGradingService;

    // Track sessions being processed to prevent duplicate submissions
    private final ConcurrentHashMap<Long, Boolean> processingSessions = new ConcurrentHashMap<>();

    @Transactional
    public void completeExam(Long sessionId) {
        // Prevent duplicate processing
        if (processingSessions.putIfAbsent(sessionId, true) != null) {
            log.warn("Session {} is already being processed for auto-submit", sessionId);
            return;
        }

        try {
            Optional<StudentExam> examOpt = studentExamRepository.findById(sessionId);
            if (examOpt.isPresent()) {
                StudentExam exam = examOpt.get();

                // Check if already completed to avoid duplicate submission
                if (exam.getCompleted()) {
                    log.info("Session {} is already completed", sessionId);
                    return;
                }

                log.info("Auto-submitting exam for session: {}", sessionId);

                // Update exam state
                exam.setCompleted(true);
                exam.setEndTime(LocalDateTime.now());

                // Grade the exam
                try {
                    examGradingService.gradeExam(sessionId);
                    exam.setGraded(true);
                    log.info("Successfully graded auto-submitted exam for session: {}", sessionId);
                } catch (Exception gradingException) {
                    log.error("Failed to grade auto-submitted exam for session {}: {}",
                            sessionId, gradingException.getMessage());
                    // Continue with submission even if grading fails
                }

                studentExamRepository.save(exam);
                log.info("Successfully auto-submitted exam for session: {}", sessionId);
            } else {
                log.warn("Session {} not found for auto-submit", sessionId);
            }
        } catch (Exception e) {
            log.error("Error during auto-submit for session {}: {}", sessionId, e.getMessage());
            throw new RuntimeException("Auto-submit failed for session: " + sessionId, e);
        } finally {
            // Clean up processing state
            processingSessions.remove(sessionId);
        }
    }

    /**
     * Check if a session is currently being processed for auto-submit
     */
    public boolean isSessionBeingProcessed(Long sessionId) {
        return processingSessions.containsKey(sessionId);
    }
}
