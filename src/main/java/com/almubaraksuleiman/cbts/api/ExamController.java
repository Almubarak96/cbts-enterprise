package com.almubaraksuleiman.cbts.api;

import com.almubaraksuleiman.cbts.dto.ExamSessionDto;
import com.almubaraksuleiman.cbts.dto.QuestionDto;
import com.almubaraksuleiman.cbts.dto.StudentAnswerDto;
import com.almubaraksuleiman.cbts.dto.StudentProfileDto;
import com.almubaraksuleiman.cbts.examiner.model.QuestionType;
import com.almubaraksuleiman.cbts.examiner.service.TestInstructionsService;
import com.almubaraksuleiman.cbts.examiner.service.UnifiedWebSocketService;
import com.almubaraksuleiman.cbts.student.model.Student;
import com.almubaraksuleiman.cbts.student.repository.StudentExamQuestionRepository;
import com.almubaraksuleiman.cbts.student.repository.StudentExamRepository;
import com.almubaraksuleiman.cbts.student.repository.StudentRepository;
import com.almubaraksuleiman.cbts.student.service.StudentExamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * ExamController with student profile support and better error handling
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 */
@RestController
@RequestMapping("/api/exam")
@RequiredArgsConstructor
@Slf4j
public class ExamController {

    private final StudentExamService examService;
    private final UnifiedWebSocketService unifiedWebSocketService;
    private final StudentExamQuestionRepository studentExamQuestionRepository;
    private final StudentRepository studentRepository;
    private final TestInstructionsService instructionsService;
    private final StudentExamRepository studentExamRepository;

    /**
     * Starts an exam session for a student
     *
     * @param testId The test ID to start
     * @param authentication Spring Security authentication object
     * @return ResponseEntity with exam session details
     */
    @PostMapping("/start/{testId}")
    public ResponseEntity<?> startExam(
            @PathVariable Long testId,
            Authentication authentication) {

        String username = authentication.getName();
        Long studentId = getStudentId(username);

        log.info("Starting exam for student: {}, test: {}", username, testId);

        // Validate instructions acknowledgment
        boolean hasReadInstructions = instructionsService.hasUserReadInstructions(studentId, testId);
        if (!hasReadInstructions) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_REQUIRED)
                    .body(Map.of(
                            "error", "Instructions not acknowledged",
                            "message", "Please read and acknowledge the exam instructions before starting",
                            "requiresInstructions", true,
                            "instructionsUrl", "/api/instructions/" + testId
                    ));
        }

        try {
            ExamSessionDto session = examService.startExam(studentId, testId);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            log.error("Error starting exam for student {}: {}", studentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Gets student profile information for exam session display
     *
     * @param authentication Spring Security authentication object
     * @return StudentProfileDto with profile information
     */
    @GetMapping("/profile")
    public ResponseEntity<StudentProfileDto> getStudentProfile(Authentication authentication) {
        String username = authentication.getName();

        try {
            Student student = studentRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Student not found: " + username));

            StudentProfileDto profile = StudentProfileDto.builder()
                    .id(student.getId())
                    .username(student.getUsername())
                    .firstName(student.getFirstName())
                    .middleName(student.getMiddleName())
                    .lastName(student.getLastName())
                    .email(student.getEmail())
                    .department(student.getDepartment())
                    .profilePictureUrl(student.getProfilePictureUrl())
                    .profilePictureThumbnailUrl(student.getProfilePictureThumbnailUrl())
                    .verified(student.isVerified())
                    .status(student.getStatus().toString())
                    .build();

            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error fetching student profile for {}: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Gets paginated questions for an exam session
     *
     * @param testId The test ID
     * @param page Page number (0-based)
     * @param size Page size
     * @param authentication Spring Security authentication
     * @return Page of QuestionDto objects
     */
    @GetMapping("/questions")
    public ResponseEntity<Page<QuestionDto>> getQuestions(
            @RequestParam Long testId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            Long studentId = getStudentId(username);

            Page<QuestionDto> questions = examService.getQuestions(studentId, testId, page, size);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            log.error("Error fetching questions: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Gets all question IDs for an exam session
     *
     * @param testId The test ID
     * @param authentication Spring Security authentication
     * @return List of question IDs
     */
    @GetMapping("/question-ids")
    public ResponseEntity<List<Long>> getQuestionIds(
            @RequestParam Long testId,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            Long studentId = getStudentId(username);

            List<Long> questionIds = examService.getQuestionIds(studentId, testId);
            return ResponseEntity.ok(questionIds);
        } catch (Exception e) {
            log.error("Error fetching question IDs: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Saves student answers
     *
     * @param testId The test ID
     * @param answers List of StudentAnswerDto objects
     * @param authentication Spring Security authentication
     * @return ResponseEntity with success status
     */
    @PatchMapping("/answers")
    public ResponseEntity<?> saveAnswers(
            @RequestParam Long testId,
            @RequestBody List<StudentAnswerDto> answers,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            Long studentId = getStudentId(username);

            examService.saveAnswers(studentId, testId, answers);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error saving answers: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Completes the exam session
     *
     * @param testId The test ID
     * @param authentication Spring Security authentication
     * @return ResponseEntity with success status
     */
    @PostMapping("/complete")
    public ResponseEntity<?> completeExam(
            @RequestParam Long testId,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            Long studentId = getStudentId(username);

            examService.completeExam(studentId, testId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error completing exam: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Gets remaining time for the exam
     *
     * @param testId The test ID
     * @param authentication Spring Security authentication
     * @return Remaining time in seconds
     */
    @GetMapping("/time-left")
    public ResponseEntity<Integer> getTimeLeft(
            @RequestParam Long testId,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            Long studentId = getStudentId(username);

            int timeLeft = unifiedWebSocketService.getTimeLeft(studentId, testId);
            return ResponseEntity.ok(timeLeft);
        } catch (Exception e) {
            log.error("Error getting time left: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Gets questions filtered by type
     *
     * @param testId The test ID
     * @param type The question type
     * @param page Page number
     * @param size Page size
     * @param authentication Spring Security authentication
     * @return Page of QuestionDto objects
     */
    @GetMapping("/questions/by-type")
    public ResponseEntity<Page<QuestionDto>> getQuestionsByType(
            @RequestParam Long testId,
            @RequestParam QuestionType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            Long studentId = getStudentId(username);

            Page<QuestionDto> questions = examService.getQuestionsByType(studentId, testId, type, page, size);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            log.error("Error fetching questions by type: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Gets question map with grouping by type and numbering
     *
     * @param testId The test ID
     * @param authentication Spring Security authentication
     * @return Map of question types to question lists
     */
    @GetMapping("/question-map")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getQuestionMap(
            @RequestParam Long testId,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            Long studentId = getStudentId(username);

            List<Map<String, Object>> flatList = studentExamQuestionRepository
                    .findQuestionIdsAnsweredAndType(studentId, testId);

            // Group by type and add numbering
            Map<String, List<Map<String, Object>>> grouped = flatList.stream()
                    .collect(Collectors.groupingBy(
                            q -> q.get("type").toString()
                    ));

            grouped.forEach((type, questions) -> {
                AtomicInteger counter = new AtomicInteger(1);
                questions.forEach(q -> q.put("number", counter.getAndIncrement()));
            });

            return ResponseEntity.ok(grouped);
        } catch (Exception e) {
            log.error("Error fetching question map: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Gets session ID for a test
     *
     * @param testId The test ID
     * @param authentication Spring Security authentication
     * @return Session ID
     */
    @GetMapping("/session-id")
    public ResponseEntity<Long> getSessionId(
            @RequestParam Long testId,
            Authentication authentication) {

        try {
            String username = authentication.getName();
            Long studentId = getStudentId(username);

            Long sessionId = studentExamQuestionRepository.findSessionIdByStudentIdAndTestId(studentId, testId);
            return ResponseEntity.ok(sessionId);
        } catch (Exception e) {
            log.error("Error fetching session ID: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Helper method to extract student ID from authentication
     */
    private Long getStudentId(String username) {
        return studentRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Student not found for username: " + username))
                .getId();
    }
}